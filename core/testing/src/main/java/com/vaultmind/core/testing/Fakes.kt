package com.vaultmind.core.testing

import com.vaultmind.core.analytics.AnalyticsEvent
import com.vaultmind.core.analytics.AnalyticsTracker
import com.vaultmind.core.domain.*
import com.vaultmind.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FakeAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    override fun track(event: AnalyticsEvent) { events += event }
    override fun setUserProperty(name: String, value: String?) = Unit
    override fun recordNonFatal(throwable: Throwable) = Unit
}

class FakeKnowledgeRepository(seed: List<KnowledgeCard> = emptyList()) : KnowledgeRepository {
    private val cards = MutableStateFlow(seed)
    override fun observeAllCards(): Flow<List<KnowledgeCard>> = cards
    override fun observePinnedCards(): Flow<List<KnowledgeCard>> = cards.map { it.filter(KnowledgeCard::isPinned) }
    override fun observeFavoriteCards(): Flow<List<KnowledgeCard>> = cards.map { it.filter(KnowledgeCard::isFavorite) }
    override fun observeCard(cardId: String): Flow<KnowledgeCard?> = cards.map { it.firstOrNull { card -> card.id == cardId } }
    override suspend fun getCard(cardId: String): KnowledgeCard? = cards.value.firstOrNull { it.id == cardId }
    override suspend fun upsertCard(draft: CardDraft): String {
        val id = draft.id ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val existing = cards.value.firstOrNull { it.id == id }
        val card = KnowledgeCard(id, draft.title, draft.body, draft.type, createdAt = existing?.createdAt ?: now, updatedAt = now, isPinned = draft.isPinned, isFavorite = draft.isFavorite, sourceLink = draft.sourceLink, summary = draft.summary)
        cards.value = cards.value.filterNot { it.id == id } + card
        return id
    }
    override suspend fun deleteCard(cardId: String) { cards.value = cards.value.filterNot { it.id == cardId } }
    override suspend fun setPinned(cardId: String, pinned: Boolean) { cards.value = cards.value.map { if (it.id == cardId) it.copy(isPinned = pinned) else it } }
    override suspend fun setFavorite(cardId: String, favorite: Boolean) { cards.value = cards.value.map { if (it.id == cardId) it.copy(isFavorite = favorite) else it } }
    override suspend fun markViewed(cardId: String) = Unit
}

class FakeOrganizationRepository : OrganizationRepository {
    private val tags = MutableStateFlow<List<Tag>>(emptyList())
    private val folders = MutableStateFlow<List<Folder>>(emptyList())
    private val collections = MutableStateFlow<List<com.vaultmind.core.model.Collection>>(emptyList())
    override fun observeTags(): Flow<List<Tag>> = tags
    override fun observeFolders(): Flow<List<Folder>> = folders
    override fun observeCollections(): Flow<List<com.vaultmind.core.model.Collection>> = collections
    override suspend fun upsertTag(id: String?, name: String, colorHex: String): String { val x = id ?: UUID.randomUUID().toString(); tags.value = tags.value.filterNot { it.id == x } + Tag(x, name, colorHex, System.currentTimeMillis()); return x }
    override suspend fun upsertFolder(id: String?, name: String, description: String?): String { val x = id ?: UUID.randomUUID().toString(); folders.value = folders.value.filterNot { it.id == x } + Folder(x, name, description, System.currentTimeMillis()); return x }
    override suspend fun upsertCollection(id: String?, name: String, description: String?): String { val x = id ?: UUID.randomUUID().toString(); collections.value = collections.value.filterNot { it.id == x } + com.vaultmind.core.model.Collection(x, name, description, System.currentTimeMillis()); return x }
    override suspend fun deleteTag(id: String) { tags.value = tags.value.filterNot { it.id == id } }
    override suspend fun deleteFolder(id: String) { folders.value = folders.value.filterNot { it.id == id } }
    override suspend fun deleteCollection(id: String) { collections.value = collections.value.filterNot { it.id == id } }
}

class FakeSearchRepository(private val knowledge: FakeKnowledgeRepository) : SearchRepository {
    private val history = MutableStateFlow<List<SearchHistory>>(emptyList())
    override fun search(query: SearchQuery): Flow<List<KnowledgeCard>> = knowledge.observeAllCards().map { cards -> cards.filter { query.text.isBlank() || it.title.contains(query.text, true) || it.body.contains(query.text, true) } }
    override fun observeRecentSearches(): Flow<List<SearchHistory>> = history
    override suspend fun recordSearch(query: String) { history.value = listOf(SearchHistory(UUID.randomUUID().toString(), query, System.currentTimeMillis())) + history.value }
    override suspend fun clearSearchHistory() { history.value = emptyList() }
}

class FakeActivityRepository : ActivityRepository {
    private val activity = MutableStateFlow<List<RecentActivity>>(emptyList())
    override fun observeRecentActivity(limit: Int): Flow<List<RecentActivity>> = activity.map { it.take(limit) }
    override suspend fun record(type: ActivityType, entityId: String?, title: String, description: String) { activity.value = listOf(RecentActivity(UUID.randomUUID().toString(), type, entityId, title, description, System.currentTimeMillis())) + activity.value }
    override suspend fun cleanupOlderThan(cutoffMillis: Long) { activity.value = activity.value.filter { it.createdAt >= cutoffMillis } }
}

class FakeBackupRepository : BackupRepository {
    private val metadata = MutableStateFlow<BackupMetadata?>(null)
    override fun observeBackupMetadata(): Flow<BackupMetadata?> = metadata
    override suspend fun exportBackup(): Result<String> { metadata.value = BackupMetadata("default", System.currentTimeMillis(), null, "Fake backup exported"); return Result.success("content://fake") }
    override suspend fun importBackup(uri: String): Result<Unit> { metadata.value = BackupMetadata("default", null, System.currentTimeMillis(), "Fake backup imported"); return Result.success(Unit) }
}

class FakeSettingsRepository : SettingsRepository {
    private val mutable = MutableStateFlow(UserSettings())
    override val settings: Flow<UserSettings> = mutable
    override suspend fun setThemeMode(mode: ThemeMode) { mutable.value = mutable.value.copy(themeMode = mode) }
    override suspend fun setDynamicColorEnabled(enabled: Boolean) { mutable.value = mutable.value.copy(dynamicColorEnabled = enabled) }
    override suspend fun setBackupReminderEnabled(enabled: Boolean) { mutable.value = mutable.value.copy(backupReminderEnabled = enabled) }
    override suspend fun setSearchHistoryEnabled(enabled: Boolean) { mutable.value = mutable.value.copy(searchHistoryEnabled = enabled) }
    override suspend fun setDefaultCardViewMode(mode: CardViewMode) { mutable.value = mutable.value.copy(defaultCardViewMode = mode) }
    override suspend fun setDefaultSortMode(mode: SortMode) { mutable.value = mutable.value.copy(defaultSortMode = mode) }
    override suspend fun setOnboardingCompleted(completed: Boolean) { mutable.value = mutable.value.copy(onboardingCompleted = completed) }
    override suspend fun setPrivatePreviewsEnabled(enabled: Boolean) { mutable.value = mutable.value.copy(privatePreviewsEnabled = enabled) }
}
