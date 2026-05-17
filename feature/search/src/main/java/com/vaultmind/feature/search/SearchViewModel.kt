package com.vaultmind.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.SearchKnowledgeVaultUseCase
import com.vaultmind.core.domain.SearchRepository
import com.vaultmind.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val loading: Boolean = false,
    val query: String = "",
    val selectedTypes: Set<CardType> = emptySet(),
    val favoritesOnly: Boolean = false,
    val pinnedOnly: Boolean = false,
    val sortMode: SortMode = SortMode.RECENTLY_UPDATED,
    val results: List<KnowledgeCard> = emptyList(),
    val recentSearches: List<SearchHistory> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchKnowledgeVaultUseCase,
    searchRepository: SearchRepository,
    private val analytics: AnalyticsTracker
) : ViewModel() {
    private val form = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = combine(
        form.debounce(250),
        form.flatMapLatest { searchUseCase(SearchQuery(it.query, cardTypes = it.selectedTypes, favoritesOnly = it.favoritesOnly, pinnedOnly = it.pinnedOnly, sortMode = it.sortMode)) },
        searchRepository.observeRecentSearches()
    ) { current, results, history -> current.copy(loading = false, results = results, recentSearches = history) }
        .catch { emit(form.value.copy(loading = false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun query(value: String) { form.update { it.copy(query = value, loading = true) } }
    fun toggleType(type: CardType) { form.update { it.copy(selectedTypes = if (type in it.selectedTypes) it.selectedTypes - type else it.selectedTypes + type) } }
    fun favoritesOnly(value: Boolean) { form.update { it.copy(favoritesOnly = value) } }
    fun pinnedOnly(value: Boolean) { form.update { it.copy(pinnedOnly = value) } }
    fun sortMode(mode: SortMode) { form.update { it.copy(sortMode = mode) } }
    fun submitSearch() = viewModelScope.launch {
        val q = form.value.query.trim()
        if (q.isNotBlank()) {
            searchUseCase.recordSearch(q)
            analytics.track(AnalyticsEvent.searchPerformed(q.length))
        }
    }
    fun applyHistory(query: String) { query(query) }
}
