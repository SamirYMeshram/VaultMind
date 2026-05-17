package com.vaultmind.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface KnowledgeCardDao {
    @Transaction @Query("SELECT * FROM knowledge_cards WHERE deleted_at IS NULL ORDER BY updated_at DESC")
    fun observeAllCards(): Flow<List<KnowledgeCardWithRelations>>
    @Transaction @Query("SELECT * FROM knowledge_cards WHERE deleted_at IS NULL AND is_pinned = 1 ORDER BY updated_at DESC")
    fun observePinnedCards(): Flow<List<KnowledgeCardWithRelations>>
    @Transaction @Query("SELECT * FROM knowledge_cards WHERE deleted_at IS NULL AND is_favorite = 1 ORDER BY updated_at DESC")
    fun observeFavoriteCards(): Flow<List<KnowledgeCardWithRelations>>
    @Transaction @Query("SELECT * FROM knowledge_cards WHERE id = :id AND deleted_at IS NULL")
    fun observeCard(id: String): Flow<KnowledgeCardWithRelations?>
    @Transaction @Query("SELECT * FROM knowledge_cards WHERE id = :id AND deleted_at IS NULL")
    suspend fun getCard(id: String): KnowledgeCardWithRelations?
    @Upsert suspend fun upsert(card: KnowledgeCardEntity)
    @Query("UPDATE knowledge_cards SET deleted_at = :deletedAt WHERE id = :id") suspend fun softDelete(id: String, deletedAt: Long)
    @Query("UPDATE knowledge_cards SET is_pinned = :pinned, updated_at = :updatedAt WHERE id = :id") suspend fun setPinned(id: String, pinned: Boolean, updatedAt: Long)
    @Query("UPDATE knowledge_cards SET is_favorite = :favorite, updated_at = :updatedAt WHERE id = :id") suspend fun setFavorite(id: String, favorite: Boolean, updatedAt: Long)
    @Query("UPDATE knowledge_cards SET updated_at = :viewedAt WHERE id = :id") suspend fun markViewed(id: String, viewedAt: Long)
}
@Dao interface TagDao {
    @Query("SELECT tags.*, COUNT(knowledge_cards.id) AS useCount FROM tags LEFT JOIN card_tag_cross_refs ON tags.id = card_tag_cross_refs.tag_id LEFT JOIN knowledge_cards ON knowledge_cards.id = card_tag_cross_refs.card_id AND knowledge_cards.deleted_at IS NULL GROUP BY tags.id ORDER BY useCount DESC, name ASC")
    fun observeTagsWithCount(): Flow<List<TagWithCount>>
    @Upsert suspend fun upsert(tag: TagEntity)
    @Query("DELETE FROM tags WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM card_tag_cross_refs WHERE card_id = :cardId") suspend fun clearForCard(cardId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCrossRefs(refs: List<CardTagCrossRef>)
}
data class TagWithCount(@Embedded val tag: TagEntity, val useCount: Int)
@Dao interface FolderDao {
    @Query("SELECT folders.*, COUNT(knowledge_cards.id) AS cardCount FROM folders LEFT JOIN knowledge_cards ON folders.id = knowledge_cards.folder_id AND knowledge_cards.deleted_at IS NULL GROUP BY folders.id ORDER BY name ASC")
    fun observeFoldersWithCount(): Flow<List<FolderWithCount>>
    @Upsert suspend fun upsert(folder: FolderEntity)
    @Query("DELETE FROM folders WHERE id = :id") suspend fun delete(id: String)
    @Query("UPDATE knowledge_cards SET folder_id = NULL WHERE folder_id = :id") suspend fun clearCards(id: String)
}
data class FolderWithCount(@Embedded val folder: FolderEntity, val cardCount: Int)
@Dao interface CollectionDao {
    @Query("SELECT collections.*, COUNT(knowledge_cards.id) AS cardCount FROM collections LEFT JOIN card_collection_cross_refs ON collections.id = card_collection_cross_refs.collection_id LEFT JOIN knowledge_cards ON knowledge_cards.id = card_collection_cross_refs.card_id AND knowledge_cards.deleted_at IS NULL GROUP BY collections.id ORDER BY name ASC")
    fun observeCollectionsWithCount(): Flow<List<CollectionWithCount>>
    @Upsert suspend fun upsert(collection: CollectionEntity)
    @Query("DELETE FROM collections WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM card_collection_cross_refs WHERE collection_id = :id") suspend fun clearCollectionRefs(id: String)
    @Query("DELETE FROM card_collection_cross_refs WHERE card_id = :cardId") suspend fun clearForCard(cardId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCrossRefs(refs: List<CardCollectionCrossRef>)
}
data class CollectionWithCount(@Embedded val collection: CollectionEntity, val cardCount: Int)
@Dao interface AttachmentDao {
    @Query("DELETE FROM attachments WHERE card_id = :cardId") suspend fun clearForCard(cardId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(attachments: List<AttachmentEntity>)
}
@Dao interface RecentActivityDao {
    @Query("SELECT * FROM recent_activity ORDER BY created_at DESC LIMIT :limit") fun observeRecent(limit: Int): Flow<List<RecentActivityEntity>>
    @Insert suspend fun insert(activity: RecentActivityEntity)
    @Query("DELETE FROM recent_activity WHERE created_at < :cutoffMillis") suspend fun cleanup(cutoffMillis: Long)
}
@Dao interface SearchDao {
    @Query("SELECT * FROM search_history ORDER BY created_at DESC LIMIT 20") fun observeRecentSearches(): Flow<List<SearchHistoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(history: SearchHistoryEntity)
    @Query("DELETE FROM search_history") suspend fun clear()
}
@Dao interface BackupDao {
    @Query("SELECT * FROM backup_metadata WHERE id = 'vault_backup_metadata'") fun observeMetadata(): Flow<BackupMetadataEntity?>
    @Upsert suspend fun upsert(metadata: BackupMetadataEntity)
}
