package com.vaultmind.feature.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.vaultmind.core.domain.ActivityRepository
import com.vaultmind.core.domain.BackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class BackupReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val backupRepository: BackupRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val metadata = backupRepository.exportBackup()
        return if (metadata.isSuccess) Result.success() else Result.retry()
    }
}

@HiltWorker
class RecentActivityCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val activityRepository: ActivityRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val cutoff = System.currentTimeMillis() - 120L * 24 * 60 * 60 * 1000
        activityRepository.cleanupOlderThan(cutoff)
        return Result.success()
    }
}

class BackupWorkScheduler @Inject constructor(private val workManager: WorkManager) {
    fun scheduleBackupReminder() {
        val request = PeriodicWorkRequestBuilder<BackupReminderWorker>(7, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .addTag("backup_reminder")
            .build()
        workManager.enqueueUniquePeriodicWork("vaultmind_backup_reminder", ExistingPeriodicWorkPolicy.UPDATE, request)
    }
    fun scheduleActivityCleanup() {
        val request = PeriodicWorkRequestBuilder<RecentActivityCleanupWorker>(30, TimeUnit.DAYS).addTag("activity_cleanup").build()
        workManager.enqueueUniquePeriodicWork("vaultmind_activity_cleanup", ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}

@dagger.Module
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
object WorkModule {
    @dagger.Provides fun workManager(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
