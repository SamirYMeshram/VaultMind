package com.vaultmind

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.vaultmind.core.domain.ActivityRepository
import com.vaultmind.core.domain.BackupRepository
import com.vaultmind.feature.backup.BackupReminderWorker
import com.vaultmind.feature.backup.RecentActivityCleanupWorker
import javax.inject.Inject
import javax.inject.Provider

class VaultMindWorkerFactory @Inject constructor(
    private val backupRepositoryProvider: Provider<BackupRepository>,
    private val activityRepositoryProvider: Provider<ActivityRepository>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        BackupReminderWorker::class.java.name -> BackupReminderWorker(
            appContext = appContext,
            params = workerParameters,
            backupRepository = backupRepositoryProvider.get()
        )

        RecentActivityCleanupWorker::class.java.name -> RecentActivityCleanupWorker(
            appContext = appContext,
            params = workerParameters,
            activityRepository = activityRepositoryProvider.get()
        )

        else -> null
    }
}
