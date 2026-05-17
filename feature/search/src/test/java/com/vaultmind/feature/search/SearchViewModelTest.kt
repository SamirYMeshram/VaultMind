package com.vaultmind.feature.search

import app.cash.turbine.test
import com.vaultmind.core.domain.SearchKnowledgeVaultUseCase
import com.vaultmind.core.model.CardType
import com.vaultmind.core.model.KnowledgeCard
import com.vaultmind.core.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @Test fun searchesOfflineCardsAndRecordsHistory() = runTest {
        val knowledge = FakeKnowledgeRepository(listOf(KnowledgeCard("1", "Kotlin Room", "offline database", CardType.TEXT_NOTE, createdAt = 1, updatedAt = 1)))
        val search = FakeSearchRepository(knowledge)
        val activity = FakeActivityRepository()
        val analytics = FakeAnalyticsTracker()
        val viewModel = SearchViewModel(SearchKnowledgeVaultUseCase(search, activity), search, analytics)
        viewModel.uiState.test {
            viewModel.query("room")
            viewModel.submitSearch()
            advanceTimeBy(300)
            advanceUntilIdle()
            val item = awaitItem()
            assertEquals("room", item.query)
            assertEquals(1, item.results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
