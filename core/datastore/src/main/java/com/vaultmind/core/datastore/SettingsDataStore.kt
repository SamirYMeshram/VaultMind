package com.vaultmind.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.vaultmind.core.domain.SettingsRepository
import com.vaultmind.core.model.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.vaultMindDataStore by preferencesDataStore("vaultmind_settings")

class DataStoreSettingsRepository @Inject constructor(@param:ApplicationContext private val context: Context) : SettingsRepository {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC = booleanPreferencesKey("dynamic")
        val BACKUP = booleanPreferencesKey("backup")
        val SEARCH_HISTORY = booleanPreferencesKey("search_history")
        val VIEW = stringPreferencesKey("view")
        val SORT = stringPreferencesKey("sort")
        val ONBOARDING = booleanPreferencesKey("onboarding")
        val PRIVATE_PREVIEWS = booleanPreferencesKey("private_previews")
    }
    override val settings: Flow<UserSettings> = context.vaultMindDataStore.data.map {
        UserSettings(
            themeMode = it[Keys.THEME]?.let(ThemeMode::valueOf) ?: ThemeMode.SYSTEM,
            dynamicColorEnabled = it[Keys.DYNAMIC] ?: true,
            backupReminderEnabled = it[Keys.BACKUP] ?: true,
            searchHistoryEnabled = it[Keys.SEARCH_HISTORY] ?: true,
            defaultCardViewMode = it[Keys.VIEW]?.let(CardViewMode::valueOf) ?: CardViewMode.COMFORTABLE,
            defaultSortMode = it[Keys.SORT]?.let(SortMode::valueOf) ?: SortMode.RECENTLY_UPDATED,
            onboardingCompleted = it[Keys.ONBOARDING] ?: false,
            privatePreviewsEnabled = it[Keys.PRIVATE_PREVIEWS] ?: true
        )
    }
    private suspend fun edit(block: (MutablePreferences) -> Unit) { context.vaultMindDataStore.edit(block) }
    override suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    override suspend fun setDynamicColorEnabled(enabled: Boolean) = edit { it[Keys.DYNAMIC] = enabled }
    override suspend fun setBackupReminderEnabled(enabled: Boolean) = edit { it[Keys.BACKUP] = enabled }
    override suspend fun setSearchHistoryEnabled(enabled: Boolean) = edit { it[Keys.SEARCH_HISTORY] = enabled }
    override suspend fun setDefaultCardViewMode(mode: CardViewMode) = edit { it[Keys.VIEW] = mode.name }
    override suspend fun setDefaultSortMode(mode: SortMode) = edit { it[Keys.SORT] = mode.name }
    override suspend fun setOnboardingCompleted(completed: Boolean) = edit { it[Keys.ONBOARDING] = completed }
    override suspend fun setPrivatePreviewsEnabled(enabled: Boolean) = edit { it[Keys.PRIVATE_PREVIEWS] = enabled }
}
@Module @InstallIn(SingletonComponent::class)
abstract class DataStoreModule {
    @Binds @Singleton abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository
}
