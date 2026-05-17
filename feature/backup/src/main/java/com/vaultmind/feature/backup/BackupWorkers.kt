package com.vaultmind.feature.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vaultmind.core.domain.ActivityRepository
import com.vaultmind.core.domain.BackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

class BackupReminderWorker(
    appContext: Context,
    params: WorkerParameters,
    private val backupRepository: BackupRepository
) : Worker(appContext, params) {
    override fun doWork(): Result = runBlocking {
        val metadata = backupRepository.exportBackup()
        if (metadata.isSuccess) Result.success() else Result.retry()
    }
}

class RecentActivityCleanupWorker(
    appContext: Context,
    params: WorkerParameters,
    private val activityRepository: ActivityRepository
) : Worker(appContext, params) {
    override fun doWork(): Result = runBlocking {
        val cutoff = System.currentTimeMillis() - 120L * 24 * 60 * 60 * 1000
        activityRepository.cleanupOlderThan(cutoff)
        Result.success()
    }
}

class BackupWorkScheduler @Inject constructor(private val workManager: WorkManager) {
    fun scheduleBackupReminder() {
        val request = PeriodicWorkRequestBuilder<BackupReminderWorker>(7, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .addTag("backup_reminder")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "vaultmind_backup_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleActivityCleanup() {
        val request = PeriodicWorkRequestBuilder<RecentActivityCleanupWorker>(30, TimeUnit.DAYS)
            .addTag("activity_cleanup")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "vaultmind_activity_cleanup",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object WorkModule {
    @Provides
    fun workManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
