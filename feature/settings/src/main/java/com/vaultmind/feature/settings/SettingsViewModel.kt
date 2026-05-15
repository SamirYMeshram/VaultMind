package com.vaultmind.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.domain.SettingsRepository
import com.vaultmind.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<UserSettings> = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())
    fun theme(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun dynamic(enabled: Boolean) = viewModelScope.launch { repository.setDynamicColorEnabled(enabled) }
    fun backup(enabled: Boolean) = viewModelScope.launch { repository.setBackupReminderEnabled(enabled) }
    fun history(enabled: Boolean) = viewModelScope.launch { repository.setSearchHistoryEnabled(enabled) }
    fun previews(enabled: Boolean) = viewModelScope.launch { repository.setPrivatePreviewsEnabled(enabled) }
    fun viewMode(mode: CardViewMode) = viewModelScope.launch { repository.setDefaultCardViewMode(mode) }
    fun sortMode(mode: SortMode) = viewModelScope.launch { repository.setDefaultSortMode(mode) }
}
