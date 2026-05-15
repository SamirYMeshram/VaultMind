package com.vaultmind.feature.knowledge

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.vaultmind.core.domain.SaveKnowledgeCardUseCase
import com.vaultmind.core.model.CardType
import com.vaultmind.core.testing.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KnowledgeCardViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @Test fun savesValidCardAndEmitsSavedEvent() = runTest {
        val knowledge = FakeKnowledgeRepository()
        val activity = FakeActivityRepository()
        val viewModel = CardEditViewModel(SavedStateHandle(), knowledge, FakeOrganizationRepository(), SaveKnowledgeCardUseCase(knowledge, activity), FakeAnalyticsTracker())
        viewModel.title("VaultMind Architecture")
        viewModel.body("Offline-first Room and Compose project")
        viewModel.type(CardType.RESEARCH_NOTE)
        viewModel.save()
        viewModel.events.test {
            val event = awaitItem()
            assertTrue(event is CardEditEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
