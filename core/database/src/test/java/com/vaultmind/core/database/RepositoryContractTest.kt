package com.vaultmind.core.database

import com.vaultmind.core.model.CardDraft
import com.vaultmind.core.model.CardType
import com.vaultmind.core.testing.FakeKnowledgeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryContractTest {
    @Test fun repositoryUpsertAndDeleteContractWorks() = runTest {
        val repository = FakeKnowledgeRepository()
        val id = repository.upsertCard(CardDraft(title = "Repository card", body = "Saved offline", type = CardType.TEXT_NOTE))
        assertEquals(1, repository.observeAllCards().first().size)
        repository.deleteCard(id)
        assertEquals(0, repository.observeAllCards().first().size)
    }
}
