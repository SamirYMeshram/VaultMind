package com.vaultmind.feature.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.BackupRepository
import com.vaultmind.core.domain.ExportBackupUseCase
import com.vaultmind.core.domain.ImportBackupUseCase
import com.vaultmind.core.model.BackupMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(val loading: Boolean = true, val metadata: BackupMetadata? = null, val busy: Boolean = false, val message: String? = null, val error: String? = null)
@HiltViewModel
class BackupViewModel @Inject constructor(
    repository: BackupRepository,
    private val exportBackup: ExportBackupUseCase,
    private val importBackup: ImportBackupUseCase,
    private val scheduler: BackupWorkScheduler,
    private val analytics: AnalyticsTracker
) : ViewModel() {
    private val transient = MutableStateFlow(BackupUiState())
    val uiState = combine(repository.observeBackupMetadata(), transient) { metadata, t -> t.copy(loading = false, metadata = metadata) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BackupUiState())
    init { scheduler.scheduleActivityCleanup(); scheduler.scheduleBackupReminder() }
    fun export() = viewModelScope.launch {
        transient.value = uiState.value.copy(busy = true, message = null, error = null)
        exportBackup().onSuccess { uri -> analytics.track(AnalyticsEvent.backupExported()); transient.value = uiState.value.copy(busy = false, message = "Backup prepared: $uri") }
            .onFailure { transient.value = uiState.value.copy(busy = false, error = it.message) }
    }
    fun importSample() = viewModelScope.launch {
        transient.value = uiState.value.copy(busy = true, message = null, error = null)
        importBackup("content://vaultmind/import-placeholder.json").onSuccess { analytics.track(AnalyticsEvent.backupImported()); transient.value = uiState.value.copy(busy = false, message = "Import placeholder completed") }
            .onFailure { transient.value = uiState.value.copy(busy = false, error = it.message) }
    }
}
