package com.vaultmind.core.database

import com.vaultmind.core.model.*
import com.vaultmind.core.model.Collection

fun KnowledgeCardWithRelations.toDomain(): KnowledgeCard = KnowledgeCard(
    id = card.id, title = card.title, body = card.body, type = CardType.valueOf(card.cardType),
    tags = tags.map { it.toDomain() }, folder = folder?.toDomain(), collections = collections.map { it.toDomain() },
    createdAt = card.createdAt, updatedAt = card.updatedAt, isPinned = card.isPinned, isFavorite = card.isFavorite,
    sourceLink = card.sourceLink, summary = card.summary, attachments = attachments.map { it.toDomain() }
)
fun TagEntity.toDomain(useCount: Int = 0) = Tag(id, name, colorHex, createdAt, useCount)
fun TagWithCount.toDomain() = tag.toDomain(useCount)
fun FolderEntity.toDomain(cardCount: Int = 0) = Folder(id, name, description, createdAt, cardCount)
fun FolderWithCount.toDomain() = folder.toDomain(cardCount)
fun CollectionEntity.toDomain(cardCount: Int = 0) = Collection(id, name, description, createdAt, cardCount)
fun CollectionWithCount.toDomain() = collection.toDomain(cardCount)
fun AttachmentEntity.toDomain() = Attachment(id, cardId, displayName, mimeType, AttachmentType.valueOf(type), uri, sizeBytes, createdAt)
fun RecentActivityEntity.toDomain() = RecentActivity(id, ActivityType.valueOf(type), entityId, title, description, createdAt)
fun SearchHistoryEntity.toDomain() = SearchHistory(id, query, createdAt)
fun BackupMetadataEntity.toDomain() = BackupMetadata(id, lastExportedAt, lastImportedAt, statusMessage)
fun CardDraft.toEntity(id: String, createdAt: Long, updatedAt: Long) = KnowledgeCardEntity(id, title.trim(), body.trim(), type.name, folderId, createdAt, updatedAt, null, isPinned, isFavorite, sourceLink?.trim()?.takeIf { it.isNotBlank() }, summary?.trim()?.takeIf { it.isNotBlank() })
