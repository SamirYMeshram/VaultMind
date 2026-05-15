package com.vaultmind.feature.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.KnowledgeRepository
import com.vaultmind.core.domain.OrganizationRepository
import com.vaultmind.core.model.KnowledgeCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionsUiState(val loading: Boolean = true, val collections: List<com.vaultmind.core.model.Collection> = emptyList(), val cards: List<KnowledgeCard> = emptyList(), val selectedCollectionId: String? = null, val error: String? = null) {
    val selectedCollection get() = collections.firstOrNull { it.id == selectedCollectionId }
    val selectedCards get() = selectedCollectionId?.let { id -> cards.filter { card -> card.collections.any { it.id == id } } }.orEmpty()
}
@HiltViewModel
class CollectionsViewModel @Inject constructor(private val org: OrganizationRepository, cards: KnowledgeRepository, private val analytics: AnalyticsTracker) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    val uiState = combine(org.observeCollections(), cards.observeAllCards(), selected) { collections, knowledge, selectedId -> CollectionsUiState(false, collections, knowledge, selectedId) }
        .catch { emit(CollectionsUiState(false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsUiState())
    fun select(id: String?) { selected.value = id }
    fun save(id: String?, name: String, description: String?) = viewModelScope.launch { if (name.isNotBlank()) { org.upsertCollection(id, name, description); analytics.track(AnalyticsEvent.collectionAdded()) } }
    fun delete(id: String) = viewModelScope.launch { org.deleteCollection(id); if (selected.value == id) selected.value = null }
}
