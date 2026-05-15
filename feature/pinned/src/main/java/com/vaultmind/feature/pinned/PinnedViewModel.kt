package com.vaultmind.feature.pinned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.KnowledgeRepository
import com.vaultmind.core.model.KnowledgeCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class PinnedUiState(val loading: Boolean = true, val cards: List<KnowledgeCard> = emptyList(), val error: String? = null)
@HiltViewModel
class PinnedViewModel @Inject constructor(repository: KnowledgeRepository, private val analytics: AnalyticsTracker) : ViewModel() {
    val uiState = repository.observePinnedCards()
        .map { PinnedUiState(false, it) }
        .catch { emit(PinnedUiState(false, error = it.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PinnedUiState())
    fun cardOpened() = analytics.track(AnalyticsEvent.pinnedCardOpened())
}
