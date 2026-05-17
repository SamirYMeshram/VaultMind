package com.vaultmind.core.database

import androidx.room.*

@Entity(tableName = "knowledge_cards", indices = [
    Index("title"), Index("updated_at"), Index("card_type"), Index("is_pinned"),
    Index("is_favorite"), Index("folder_id"), Index("deleted_at")
])
data class KnowledgeCardEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    @ColumnInfo(name = "card_type") val cardType: String,
    @ColumnInfo(name = "folder_id") val folderId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "source_link") val sourceLink: String?,
    val summary: String?
)
@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(@PrimaryKey val id: String, val name: String, @ColumnInfo(name = "color_hex") val colorHex: String, @ColumnInfo(name = "created_at") val createdAt: Long)
@Entity(tableName = "card_tag_cross_refs", primaryKeys = ["card_id", "tag_id"], indices = [Index("card_id"), Index("tag_id")])
data class CardTagCrossRef(@ColumnInfo(name = "card_id") val cardId: String, @ColumnInfo(name = "tag_id") val tagId: String)
@Entity(tableName = "folders", indices = [Index(value = ["name"], unique = true)])
data class FolderEntity(@PrimaryKey val id: String, val name: String, val description: String?, @ColumnInfo(name = "created_at") val createdAt: Long)
@Entity(tableName = "collections", indices = [Index(value = ["name"], unique = true)])
data class CollectionEntity(@PrimaryKey val id: String, val name: String, val description: String?, @ColumnInfo(name = "created_at") val createdAt: Long)
@Entity(tableName = "card_collection_cross_refs", primaryKeys = ["card_id", "collection_id"], indices = [Index("card_id"), Index("collection_id")])
data class CardCollectionCrossRef(@ColumnInfo(name = "card_id") val cardId: String, @ColumnInfo(name = "collection_id") val collectionId: String)
@Entity(tableName = "attachments", indices = [Index("card_id"), Index("display_name"), Index("type")])
data class AttachmentEntity(@PrimaryKey val id: String, @ColumnInfo(name = "card_id") val cardId: String, @ColumnInfo(name = "display_name") val displayName: String, @ColumnInfo(name = "mime_type") val mimeType: String, val type: String, val uri: String, @ColumnInfo(name = "size_bytes") val sizeBytes: Long, @ColumnInfo(name = "created_at") val createdAt: Long)
@Entity(tableName = "recent_activity", indices = [Index("created_at"), Index("type")])
data class RecentActivityEntity(@PrimaryKey val id: String, val type: String, @ColumnInfo(name = "entity_id") val entityId: String?, val title: String, val description: String, @ColumnInfo(name = "created_at") val createdAt: Long)
@Entity(tableName = "search_history", indices = [Index("created_at"), Index("query")])
data class SearchHistoryEntity(@PrimaryKey val id: String, val query: String, @ColumnInfo(name = "created_at") val createdAt: Long)
@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(@PrimaryKey val id: String = "vault_backup_metadata", @ColumnInfo(name = "last_exported_at") val lastExportedAt: Long?, @ColumnInfo(name = "last_imported_at") val lastImportedAt: Long?, @ColumnInfo(name = "status_message") val statusMessage: String)

data class KnowledgeCardWithRelations(
    @Embedded val card: KnowledgeCardEntity,
    @Relation(parentColumn = "folder_id", entityColumn = "id") val folder: FolderEntity?,
    @Relation(parentColumn = "id", entityColumn = "id", associateBy = Junction(value = CardTagCrossRef::class, parentColumn = "card_id", entityColumn = "tag_id")) val tags: List<TagEntity>,
    @Relation(parentColumn = "id", entityColumn = "id", associateBy = Junction(value = CardCollectionCrossRef::class, parentColumn = "card_id", entityColumn = "collection_id")) val collections: List<CollectionEntity>,
    @Relation(parentColumn = "id", entityColumn = "card_id") val attachments: List<AttachmentEntity>
)
