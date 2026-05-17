package com.vaultmind.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.vaultmind.core.common.IoDispatcher
import com.vaultmind.core.domain.*
import com.vaultmind.core.model.*
import com.vaultmind.core.model.Collection
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

class DefaultKnowledgeRepository @Inject constructor(
    private val db: VaultMindDatabase,
    private val cardDao: KnowledgeCardDao,
    private val tagDao: TagDao,
    private val collectionDao: CollectionDao,
    private val attachmentDao: AttachmentDao,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : KnowledgeRepository {
    override fun observeAllCards(): Flow<List<KnowledgeCard>> = cardDao.observeAllCards().map { it.map(KnowledgeCardWithRelations::toDomain) }
    override fun observePinnedCards(): Flow<List<KnowledgeCard>> = cardDao.observePinnedCards().map { it.map(KnowledgeCardWithRelations::toDomain) }
    override fun observeFavoriteCards(): Flow<List<KnowledgeCard>> = cardDao.observeFavoriteCards().map { it.map(KnowledgeCardWithRelations::toDomain) }
    override fun observeCard(cardId: String): Flow<KnowledgeCard?> = cardDao.observeCard(cardId).map { it?.toDomain() }
    override suspend fun getCard(cardId: String): KnowledgeCard? = withContext(dispatcher) { cardDao.getCard(cardId)?.toDomain() }

    override suspend fun upsertCard(draft: CardDraft): String = withContext(dispatcher) {
        val now = System.currentTimeMillis()
        val id = draft.id ?: UUID.randomUUID().toString()
        val createdAt = draft.id?.let { cardDao.getCard(it)?.card?.createdAt } ?: now
        db.withTransaction {
            cardDao.upsert(draft.toEntity(id, createdAt, now))
            tagDao.clearForCard(id)
            if (draft.tagIds.isNotEmpty()) tagDao.insertCrossRefs(draft.tagIds.distinct().map { CardTagCrossRef(id, it) })
            collectionDao.clearForCard(id)
            if (draft.collectionIds.isNotEmpty()) collectionDao.insertCrossRefs(draft.collectionIds.distinct().map { CardCollectionCrossRef(id, it) })
            attachmentDao.clearForCard(id)
            if (draft.attachments.isNotEmpty()) attachmentDao.insertAll(draft.attachments.map {
                AttachmentEntity(UUID.randomUUID().toString(), id, it.displayName, it.mimeType, it.type.name, it.uri, it.sizeBytes, now)
            })
        }
        id
    }
    override suspend fun deleteCard(cardId: String) = withContext(dispatcher) { cardDao.softDelete(cardId, System.currentTimeMillis()) }
    override suspend fun setPinned(cardId: String, pinned: Boolean) = withContext(dispatcher) { cardDao.setPinned(cardId, pinned, System.currentTimeMillis()) }
    override suspend fun setFavorite(cardId: String, favorite: Boolean) = withContext(dispatcher) { cardDao.setFavorite(cardId, favorite, System.currentTimeMillis()) }
    override suspend fun markViewed(cardId: String) = withContext(dispatcher) { cardDao.markViewed(cardId, System.currentTimeMillis()) }
}

class DefaultOrganizationRepository @Inject constructor(
    private val tagDao: TagDao,
    private val folderDao: FolderDao,
    private val collectionDao: CollectionDao,
    private val activityRepository: ActivityRepository,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : OrganizationRepository {
    override fun observeTags(): Flow<List<Tag>> = tagDao.observeTagsWithCount().map { it.map(TagWithCount::toDomain) }
    override fun observeFolders(): Flow<List<Folder>> = folderDao.observeFoldersWithCount().map { it.map(FolderWithCount::toDomain) }
    override fun observeCollections(): Flow<List<Collection>> = collectionDao.observeCollectionsWithCount().map { it.map(CollectionWithCount::toDomain) }
    override suspend fun upsertTag(id: String?, name: String, colorHex: String): String = withContext(dispatcher) {
        val tagId = id ?: UUID.randomUUID().toString()
        tagDao.upsert(TagEntity(tagId, name.trim(), colorHex, System.currentTimeMillis()))
        activityRepository.record(ActivityType.TAG_CREATED, tagId, name.trim(), "Tag saved")
        tagId
    }
    override suspend fun upsertFolder(id: String?, name: String, description: String?): String = withContext(dispatcher) {
        val folderId = id ?: UUID.randomUUID().toString()
        folderDao.upsert(FolderEntity(folderId, name.trim(), description?.trim(), System.currentTimeMillis()))
        activityRepository.record(ActivityType.FOLDER_CREATED, folderId, name.trim(), "Folder saved")
        folderId
    }
    override suspend fun upsertCollection(id: String?, name: String, description: String?): String = withContext(dispatcher) {
        val collectionId = id ?: UUID.randomUUID().toString()
        collectionDao.upsert(CollectionEntity(collectionId, name.trim(), description?.trim(), System.currentTimeMillis()))
        activityRepository.record(ActivityType.COLLECTION_CREATED, collectionId, name.trim(), "Collection saved")
        collectionId
    }
    override suspend fun deleteTag(id: String) = withContext(dispatcher) { tagDao.delete(id) }
    override suspend fun deleteFolder(id: String) = withContext(dispatcher) { folderDao.clearCards(id); folderDao.delete(id) }
    override suspend fun deleteCollection(id: String) = withContext(dispatcher) { collectionDao.clearCollectionRefs(id); collectionDao.delete(id) }
}

class DefaultSearchRepository @Inject constructor(
    private val cardDao: KnowledgeCardDao,
    private val searchDao: SearchDao,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) : SearchRepository {
    override fun search(query: SearchQuery): Flow<List<KnowledgeCard>> = cardDao.observeAllCards().map { rows ->
        val text = query.text.trim().lowercase()
        rows.map(KnowledgeCardWithRelations::toDomain)
            .filter { matchesFilters(it, query) && (text.isBlank() || matchesText(it, text)) }
            .map { it.copy(relatedScore = rank(it, text)) }
            .let { sortResults(it, query.sortMode) }
    }
    override fun observeRecentSearches(): Flow<List<SearchHistory>> = searchDao.observeRecentSearches().map { it.map(SearchHistoryEntity::toDomain) }
    override suspend fun recordSearch(query: String) = withContext(dispatcher) { searchDao.insert(SearchHistoryEntity(UUID.randomUUID().toString(), query, System.currentTimeMillis())) }
    override suspend fun clearSearchHistory() = withContext(dispatcher) { searchDao.clear() }
    private fun matchesFilters(card: KnowledgeCard, query: SearchQuery): Boolean =
        (query.cardTypes.isEmpty() || card.type in query.cardTypes) &&
        (query.tagIds.isEmpty() || card.tags.any { it.id in query.tagIds }) &&
        (query.folderId == null || card.folder?.id == query.folderId) &&
        (query.collectionIds.isEmpty() || card.collections.any { it.id in query.collectionIds }) &&
        (!query.favoritesOnly || card.isFavorite) &&
        (!query.pinnedOnly || card.isPinned)
    private fun matchesText(card: KnowledgeCard, text: String): Boolean =
        listOfNotNull(card.title, card.body, card.summary, card.sourceLink, card.folder?.name)
            .plus(card.tags.map { it.name }).plus(card.collections.map { it.name }).plus(card.attachments.map { it.displayName })
            .any { it.lowercase().contains(text) }
    private fun sortResults(cards: List<KnowledgeCard>, sortMode: SortMode): List<KnowledgeCard> = when (sortMode) {
        SortMode.RECENTLY_UPDATED -> cards.sortedWith(compareByDescending<KnowledgeCard> { it.relatedScore }.thenByDescending { it.updatedAt })
        SortMode.RECENTLY_CREATED -> cards.sortedWith(compareByDescending<KnowledgeCard> { it.relatedScore }.thenByDescending { it.createdAt })
        SortMode.TITLE_ASC -> cards.sortedWith(compareByDescending<KnowledgeCard> { it.relatedScore }.thenBy { it.title.lowercase() })
        SortMode.TITLE_DESC -> cards.sortedWith(compareByDescending<KnowledgeCard> { it.relatedScore }.thenByDescending { it.title.lowercase() })
        SortMode.FAVORITES_FIRST -> cards.sortedWith(compareByDescending<KnowledgeCard> { it.isFavorite }.thenByDescending { it.relatedScore }.thenByDescending { it.updatedAt })
        SortMode.PINNED_FIRST -> cards.sortedWith(compareByDescending<KnowledgeCard> { it.isPinned }.thenByDescending { it.relatedScore }.thenByDescending { it.updatedAt })
    }
    private fun rank(card: KnowledgeCard, text: String): Int {
        if (text.isBlank()) return (if (card.isPinned) 20 else 0) + if (card.updatedAt > System.currentTimeMillis() - 604800000L) 5 else 0
        var score = 0
        if (card.title.lowercase().contains(text)) score += 100
        if (card.tags.any { it.name.lowercase().contains(text) }) score += 80
        if (card.collections.any { it.name.lowercase().contains(text) }) score += 60
        if (card.folder?.name?.lowercase()?.contains(text) == true) score += 50
        if (card.body.lowercase().contains(text)) score += 30
        if (card.summary?.lowercase()?.contains(text) == true) score += 25
        if (card.sourceLink?.lowercase()?.contains(text) == true) score += 15
        if (card.attachments.any { it.displayName.lowercase().contains(text) }) score += 12
        if (card.isPinned) score += 20
        if (card.updatedAt > System.currentTimeMillis() - 604800000L) score += 5
        return score
    }
}

class DefaultActivityRepository @Inject constructor(private val dao: RecentActivityDao, @param:IoDispatcher private val dispatcher: CoroutineDispatcher) : ActivityRepository {
    override fun observeRecentActivity(limit: Int): Flow<List<RecentActivity>> = dao.observeRecent(limit).map { it.map(RecentActivityEntity::toDomain) }
    override suspend fun record(type: ActivityType, entityId: String?, title: String, description: String) = withContext(dispatcher) {
        dao.insert(RecentActivityEntity(UUID.randomUUID().toString(), type.name, entityId, title, description, System.currentTimeMillis()))
    }
    override suspend fun cleanupOlderThan(cutoffMillis: Long) = withContext(dispatcher) { dao.cleanup(cutoffMillis) }
}

class DefaultBackupRepository @Inject constructor(private val backupDao: BackupDao, private val activityRepository: ActivityRepository, @param:IoDispatcher private val dispatcher: CoroutineDispatcher) : BackupRepository {
    override fun observeBackupMetadata(): Flow<BackupMetadata?> = backupDao.observeMetadata().map { it?.toDomain() }
    override suspend fun exportBackup(): Result<String> = withContext(dispatcher) {
        val now = System.currentTimeMillis()
        backupDao.upsert(BackupMetadataEntity(lastExportedAt = now, lastImportedAt = null, statusMessage = "Backup export prepared"))
        activityRepository.record(ActivityType.BACKUP_EXPORTED, null, "Backup exported", "Placeholder export completed")
        Result.success("content://vaultmind/backup/vaultmind-$now.json")
    }
    override suspend fun importBackup(uri: String): Result<Unit> = withContext(dispatcher) {
        backupDao.upsert(BackupMetadataEntity(lastExportedAt = null, lastImportedAt = System.currentTimeMillis(), statusMessage = "Imported from $uri"))
        activityRepository.record(ActivityType.BACKUP_IMPORTED, null, "Backup imported", "Placeholder import completed")
        Result.success(Unit)
    }
}

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): VaultMindDatabase =
        Room.databaseBuilder(context, VaultMindDatabase::class.java, "vaultmind.db").build()
    @Provides fun cards(db: VaultMindDatabase) = db.knowledgeCardDao()
    @Provides fun tags(db: VaultMindDatabase) = db.tagDao()
    @Provides fun folders(db: VaultMindDatabase) = db.folderDao()
    @Provides fun collections(db: VaultMindDatabase) = db.collectionDao()
    @Provides fun attachments(db: VaultMindDatabase) = db.attachmentDao()
    @Provides fun activities(db: VaultMindDatabase) = db.recentActivityDao()
    @Provides fun search(db: VaultMindDatabase) = db.searchDao()
    @Provides fun backups(db: VaultMindDatabase) = db.backupDao()
}
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds @Singleton abstract fun knowledge(impl: DefaultKnowledgeRepository): KnowledgeRepository
    @Binds @Singleton abstract fun organization(impl: DefaultOrganizationRepository): OrganizationRepository
    @Binds @Singleton abstract fun search(impl: DefaultSearchRepository): SearchRepository
    @Binds @Singleton abstract fun activity(impl: DefaultActivityRepository): ActivityRepository
    @Binds @Singleton abstract fun backup(impl: DefaultBackupRepository): BackupRepository
}
