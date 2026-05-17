package com.vaultmind.core.domain

import com.vaultmind.core.model.ActivityType
import com.vaultmind.core.model.BackupMetadata
import com.vaultmind.core.model.CardDraft
import com.vaultmind.core.model.CardViewMode
import com.vaultmind.core.model.Collection
import com.vaultmind.core.model.Folder
import com.vaultmind.core.model.KnowledgeCard
import com.vaultmind.core.model.RecentActivity
import com.vaultmind.core.model.SearchHistory
import com.vaultmind.core.model.SearchQuery
import com.vaultmind.core.model.SortMode
import com.vaultmind.core.model.Tag
import com.vaultmind.core.model.ThemeMode
import com.vaultmind.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface KnowledgeRepository {
    fun observeAllCards(): Flow<List<KnowledgeCard>>
    fun observePinnedCards(): Flow<List<KnowledgeCard>>
    fun observeFavoriteCards(): Flow<List<KnowledgeCard>>
    fun observeCard(cardId: String): Flow<KnowledgeCard?>
    suspend fun getCard(cardId: String): KnowledgeCard?
    suspend fun upsertCard(draft: CardDraft): String
    suspend fun deleteCard(cardId: String)
    suspend fun setPinned(cardId: String, pinned: Boolean)
    suspend fun setFavorite(cardId: String, favorite: Boolean)
    suspend fun markViewed(cardId: String)
}
interface OrganizationRepository {
    fun observeTags(): Flow<List<Tag>>
    fun observeFolders(): Flow<List<Folder>>
    fun observeCollections(): Flow<List<Collection>>
    suspend fun upsertTag(id: String?, name: String, colorHex: String): String
    suspend fun upsertFolder(id: String?, name: String, description: String?): String
    suspend fun upsertCollection(id: String?, name: String, description: String?): String
    suspend fun deleteTag(id: String)
    suspend fun deleteFolder(id: String)
    suspend fun deleteCollection(id: String)
}
interface SearchRepository {
    fun search(query: SearchQuery): Flow<List<KnowledgeCard>>
    fun observeRecentSearches(): Flow<List<SearchHistory>>
    suspend fun recordSearch(query: String)
    suspend fun clearSearchHistory()
}
interface ActivityRepository {
    fun observeRecentActivity(limit: Int = 50): Flow<List<RecentActivity>>
    suspend fun record(type: ActivityType, entityId: String?, title: String, description: String)
    suspend fun cleanupOlderThan(cutoffMillis: Long)
}
interface BackupRepository {
    fun observeBackupMetadata(): Flow<BackupMetadata?>
    suspend fun exportBackup(): Result<String>
    suspend fun importBackup(uri: String): Result<Unit>
}
interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setBackupReminderEnabled(enabled: Boolean)
    suspend fun setSearchHistoryEnabled(enabled: Boolean)
    suspend fun setDefaultCardViewMode(mode: CardViewMode)
    suspend fun setDefaultSortMode(mode: SortMode)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setPrivatePreviewsEnabled(enabled: Boolean)
}
