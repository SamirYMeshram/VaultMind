package com.vaultmind.feature.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.KnowledgeRepository
import com.vaultmind.core.domain.OrganizationRepository
import com.vaultmind.core.model.Folder
import com.vaultmind.core.model.KnowledgeCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoldersUiState(val loading: Boolean = true, val folders: List<Folder> = emptyList(), val cards: List<KnowledgeCard> = emptyList(), val selectedFolderId: String? = null, val error: String? = null) {
    val selectedFolder get() = folders.firstOrNull { it.id == selectedFolderId }
    val selectedCards get() = selectedFolderId?.let { id -> cards.filter { it.folder?.id == id } }.orEmpty()
}
@HiltViewModel
class FoldersViewModel @Inject constructor(private val org: OrganizationRepository, cards: KnowledgeRepository, private val analytics: AnalyticsTracker) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    val uiState = combine(org.observeFolders(), cards.observeAllCards(), selected) { folders, knowledge, selectedId -> FoldersUiState(false, folders, knowledge, selectedId) }
        .catch { emit(FoldersUiState(false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoldersUiState())
    fun select(id: String?) { selected.value = id }
    fun save(id: String?, name: String, description: String?) = viewModelScope.launch { if (name.isNotBlank()) { org.upsertFolder(id, name, description); analytics.track(AnalyticsEvent.folderAdded()) } }
    fun delete(id: String) = viewModelScope.launch { org.deleteFolder(id); if (selected.value == id) selected.value = null }
}
