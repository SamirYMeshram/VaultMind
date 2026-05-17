package com.vaultmind.core.domain

import com.vaultmind.core.common.DefaultDispatcher
import com.vaultmind.core.model.ActivityType
import com.vaultmind.core.model.BackupMetadata
import com.vaultmind.core.model.CardDraft
import com.vaultmind.core.model.Collection
import com.vaultmind.core.model.Folder
import com.vaultmind.core.model.KnowledgeCard
import com.vaultmind.core.model.RecentActivity
import com.vaultmind.core.model.SearchQuery
import com.vaultmind.core.model.Tag
import com.vaultmind.core.model.VaultStats
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveKnowledgeCardUseCase @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val activityRepository: ActivityRepository
) {
    suspend operator fun invoke(draft: CardDraft): Result<String> {
        if (draft.title.isBlank()) return Result.failure(IllegalArgumentException("Title cannot be empty"))
        if (draft.body.isBlank() && draft.sourceLink.isNullOrBlank() && draft.attachments.isEmpty()) {
            return Result.failure(IllegalArgumentException("Add content, source link, or attachment"))
        }
        val id = knowledgeRepository.upsertCard(draft)
        activityRepository.record(if (draft.id == null) ActivityType.CARD_CREATED else ActivityType.CARD_EDITED, id, draft.title.trim(), "Card saved")
        return Result.success(id)
    }
}
class DeleteKnowledgeCardUseCase @Inject constructor(private val repository: KnowledgeRepository, private val activityRepository: ActivityRepository) {
    suspend operator fun invoke(card: KnowledgeCard) {
        repository.deleteCard(card.id)
        activityRepository.record(ActivityType.CARD_DELETED, card.id, card.title, "Card deleted")
    }
}
class TogglePinnedUseCase @Inject constructor(private val repository: KnowledgeRepository) { suspend operator fun invoke(card: KnowledgeCard) = repository.setPinned(card.id, !card.isPinned) }
class ToggleFavoriteUseCase @Inject constructor(private val repository: KnowledgeRepository) { suspend operator fun invoke(card: KnowledgeCard) = repository.setFavorite(card.id, !card.isFavorite) }
class SearchKnowledgeVaultUseCase @Inject constructor(private val searchRepository: SearchRepository, private val activityRepository: ActivityRepository) {
    operator fun invoke(query: SearchQuery): Flow<List<KnowledgeCard>> = searchRepository.search(query)
    suspend fun recordSearch(query: String) {
        if (query.isNotBlank()) {
            searchRepository.recordSearch(query.trim())
            activityRepository.record(ActivityType.SEARCH_PERFORMED, null, query.trim(), "Search performed")
        }
    }
}
data class DashboardData(
    val stats: VaultStats,
    val recentCards: List<KnowledgeCard>,
    val pinnedCards: List<KnowledgeCard>,
    val topTags: List<Tag>,
    val activeCollections: List<Collection>,
    val recentActivity: List<RecentActivity>
)

private data class DashboardCoreData(
    val cards: List<KnowledgeCard>,
    val tags: List<Tag>,
    val folders: List<Folder>,
    val collections: List<Collection>,
    val activities: List<RecentActivity>
)

class GetDashboardUseCase @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository,
    private val organizationRepository: OrganizationRepository,
    private val activityRepository: ActivityRepository,
    private val backupRepository: BackupRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        val coreData = combine(
            knowledgeRepository.observeAllCards(),
            organizationRepository.observeTags(),
            organizationRepository.observeFolders(),
            organizationRepository.observeCollections(),
            activityRepository.observeRecentActivity(8)
        ) { cards, tags, folders, collections, activities ->
            DashboardCoreData(
                cards = cards,
                tags = tags,
                folders = folders,
                collections = collections,
                activities = activities
            )
        }

        return combine(coreData, backupRepository.observeBackupMetadata()) { data, backup ->
            DashboardData(
                stats = data.toStats(backup),
                recentCards = data.cards.sortedByDescending { it.updatedAt }.take(8),
                pinnedCards = data.cards.filter { it.isPinned }.take(8),
                topTags = data.tags.sortedByDescending { it.useCount }.take(12),
                activeCollections = data.collections.sortedByDescending { it.cardCount }.take(6),
                recentActivity = data.activities
            )
        }
    }

    private fun DashboardCoreData.toStats(backup: BackupMetadata?): VaultStats = VaultStats(
        totalCards = cards.size,
        pinnedCards = cards.count { it.isPinned },
        favoriteCards = cards.count { it.isFavorite },
        totalTags = tags.size,
        totalFolders = folders.size,
        totalCollections = collections.size,
        totalAttachments = cards.sumOf { it.attachments.size },
        lastBackupAt = backup?.lastExportedAt
    )
}
class GetRelatedKnowledgeCardsUseCase @Inject constructor(
    private val repository: KnowledgeRepository,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(cardId: String, limit: Int = 12): List<KnowledgeCard> = withContext(dispatcher) {
        val base = repository.getCard(cardId) ?: return@withContext emptyList()
        repository.observeAllCards().first()
            .filterNot { it.id == cardId }
            .map { it.copy(relatedScore = score(base, it)) }
            .filter { it.relatedScore > 0 }
            .sortedWith(compareByDescending<KnowledgeCard> { it.relatedScore }.thenByDescending { it.updatedAt })
            .take(limit)
    }
    private fun score(base: KnowledgeCard, candidate: KnowledgeCard): Int {
        val sharedTags = base.tags.map { it.id }.toSet().intersect(candidate.tags.map { it.id }.toSet()).size * 8
        val title = overlap(base.title, candidate.title) * 3
        val body = overlap(base.body, candidate.body)
        val folder = if (base.folder?.id != null && base.folder?.id == candidate.folder?.id) 4 else 0
        val collections = base.collections.map { it.id }.toSet().intersect(candidate.collections.map { it.id }.toSet()).size * 5
        val type = if (base.type == candidate.type) 2 else 0
        val recent = if (candidate.updatedAt > System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) 1 else 0
        return sharedTags + title + body + folder + collections + type + recent
    }
    private fun overlap(a: String, b: String): Int {
        val x = a.lowercase().split(Regex("\\W+")).filter { it.length > 3 }.toSet()
        val y = b.lowercase().split(Regex("\\W+")).filter { it.length > 3 }.toSet()
        return x.intersect(y).size
    }
}
class ExportBackupUseCase @Inject constructor(private val repository: BackupRepository) { suspend operator fun invoke() = repository.exportBackup() }
class ImportBackupUseCase @Inject constructor(private val repository: BackupRepository) { suspend operator fun invoke(uri: String) = repository.importBackup(uri) }
