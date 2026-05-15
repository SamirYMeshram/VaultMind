package com.vaultmind.core.model

enum class CardType(val label: String) {
    TEXT_NOTE("Text Note"), PDF("PDF"), IMAGE("Image"), SCREENSHOT("Screenshot"), LINK("Link"),
    FILE("File"), IDEA("Idea"), CODE_SNIPPET("Code Snippet"), RESEARCH_NOTE("Research Note"), MIXED("Mixed")
}
enum class AttachmentType { IMAGE, SCREENSHOT, PDF, FILE }
enum class ActivityType {
    CARD_CREATED, CARD_EDITED, CARD_VIEWED, CARD_DELETED, SEARCH_PERFORMED,
    TAG_CREATED, FOLDER_CREATED, COLLECTION_CREATED, BACKUP_EXPORTED, BACKUP_IMPORTED,
    RELATED_CARD_OPENED, PINNED_CARD_OPENED
}
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CardViewMode { COMPACT, COMFORTABLE, GRID }
enum class SortMode { RECENTLY_UPDATED, RECENTLY_CREATED, TITLE_ASC, TITLE_DESC, FAVORITES_FIRST, PINNED_FIRST }

data class KnowledgeCard(
    val id: String,
    val title: String,
    val body: String,
    val type: CardType,
    val tags: List<Tag> = emptyList(),
    val folder: Folder? = null,
    val collections: List<Collection> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val sourceLink: String? = null,
    val summary: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val relatedScore: Int = 0
)

data class CardDraft(
    val id: String? = null,
    val title: String,
    val body: String,
    val type: CardType,
    val tagIds: List<String> = emptyList(),
    val folderId: String? = null,
    val collectionIds: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val sourceLink: String? = null,
    val summary: String? = null,
    val attachments: List<AttachmentDraft> = emptyList()
)
data class Tag(val id: String, val name: String, val colorHex: String, val createdAt: Long, val useCount: Int = 0)
data class Folder(val id: String, val name: String, val description: String? = null, val createdAt: Long, val cardCount: Int = 0)
data class Collection(val id: String, val name: String, val description: String? = null, val createdAt: Long, val cardCount: Int = 0)
data class Attachment(val id: String, val cardId: String, val displayName: String, val mimeType: String, val type: AttachmentType, val uri: String, val sizeBytes: Long, val createdAt: Long)
data class AttachmentDraft(val displayName: String, val mimeType: String, val type: AttachmentType, val uri: String, val sizeBytes: Long)
data class RecentActivity(val id: String, val type: ActivityType, val entityId: String?, val title: String, val description: String, val createdAt: Long)
data class SearchHistory(val id: String, val query: String, val createdAt: Long)
data class BackupMetadata(val id: String, val lastExportedAt: Long?, val lastImportedAt: Long?, val statusMessage: String)
data class VaultStats(
    val totalCards: Int = 0, val pinnedCards: Int = 0, val favoriteCards: Int = 0,
    val totalTags: Int = 0, val totalFolders: Int = 0, val totalCollections: Int = 0,
    val totalAttachments: Int = 0, val lastBackupAt: Long? = null
)
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val backupReminderEnabled: Boolean = true,
    val searchHistoryEnabled: Boolean = true,
    val defaultCardViewMode: CardViewMode = CardViewMode.COMFORTABLE,
    val defaultSortMode: SortMode = SortMode.RECENTLY_UPDATED,
    val onboardingCompleted: Boolean = false,
    val privatePreviewsEnabled: Boolean = true
)
data class SearchQuery(
    val text: String,
    val cardTypes: Set<CardType> = emptySet(),
    val tagIds: Set<String> = emptySet(),
    val folderId: String? = null,
    val collectionIds: Set<String> = emptySet(),
    val favoritesOnly: Boolean = false,
    val pinnedOnly: Boolean = false,
    val sortMode: SortMode = SortMode.RECENTLY_UPDATED
)
