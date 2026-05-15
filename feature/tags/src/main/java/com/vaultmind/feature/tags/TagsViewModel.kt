package com.vaultmind.feature.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.KnowledgeRepository
import com.vaultmind.core.domain.OrganizationRepository
import com.vaultmind.core.model.KnowledgeCard
import com.vaultmind.core.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagsUiState(val loading: Boolean = true, val tags: List<Tag> = emptyList(), val cards: List<KnowledgeCard> = emptyList(), val selectedTagId: String? = null, val error: String? = null) {
    val selectedTag get() = tags.firstOrNull { it.id == selectedTagId }
    val selectedCards get() = selectedTagId?.let { id -> cards.filter { c -> c.tags.any { it.id == id } } }.orEmpty()
}
@HiltViewModel
class TagsViewModel @Inject constructor(private val org: OrganizationRepository, cards: KnowledgeRepository, private val analytics: AnalyticsTracker) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    val uiState = combine(org.observeTags(), cards.observeAllCards(), selected) { tags, knowledge, selectedId -> TagsUiState(false, tags, knowledge, selectedId) }
        .catch { emit(TagsUiState(false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TagsUiState())
    fun select(id: String?) { selected.value = id }
    fun save(id: String?, name: String, colorHex: String = "#6C63FF") = viewModelScope.launch { if (name.isNotBlank()) { org.upsertTag(id, name, colorHex); analytics.track(AnalyticsEvent.tagAdded()) } }
    fun delete(id: String) = viewModelScope.launch { org.deleteTag(id); if (selected.value == id) selected.value = null }
}
