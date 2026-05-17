package com.vaultmind.core.domain

import com.vaultmind.core.model.CardDraft
import com.vaultmind.core.model.CardType
import com.vaultmind.core.model.KnowledgeCard
import com.vaultmind.core.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRelatedKnowledgeCardsUseCaseTest {
    @Test fun ranksSharedTagsAndSimilarWordsHigher() = runTest {
        val tag = Tag("tag", "Android", "#000000", 1)
        val base = KnowledgeCard("base", "Room search architecture", "offline android search", CardType.RESEARCH_NOTE, tags = listOf(tag), createdAt = 1, updatedAt = 1)
        val related = KnowledgeCard("related", "Room search notes", "android database search", CardType.RESEARCH_NOTE, tags = listOf(tag), createdAt = 2, updatedAt = 2)
        val weak = KnowledgeCard("weak", "Cooking", "recipe", CardType.TEXT_NOTE, createdAt = 3, updatedAt = 3)
        val useCase = GetRelatedKnowledgeCardsUseCase(LocalKnowledgeRepository(listOf(base, weak, related)), StandardTestDispatcher(testScheduler))
        val result = useCase("base")
        assertEquals("related", result.first().id)
        assertTrue(result.none { it.id == "weak" })
    }
}

private class LocalKnowledgeRepository(seed: List<KnowledgeCard>) : KnowledgeRepository {
    private val cards = MutableStateFlow(seed)
    override fun observeAllCards(): Flow<List<KnowledgeCard>> = cards
    override fun observePinnedCards(): Flow<List<KnowledgeCard>> = cards.map { it.filter(KnowledgeCard::isPinned) }
    override fun observeFavoriteCards(): Flow<List<KnowledgeCard>> = cards.map { it.filter(KnowledgeCard::isFavorite) }
    override fun observeCard(cardId: String): Flow<KnowledgeCard?> = cards.map { it.firstOrNull { c -> c.id == cardId } }
    override suspend fun getCard(cardId: String): KnowledgeCard? = cards.value.firstOrNull { it.id == cardId }
    override suspend fun upsertCard(draft: CardDraft): String = error("Not needed")
    override suspend fun deleteCard(cardId: String) = Unit
    override suspend fun setPinned(cardId: String, pinned: Boolean) = Unit
    override suspend fun setFavorite(cardId: String, favorite: Boolean) = Unit
    override suspend fun markViewed(cardId: String) = Unit
}
