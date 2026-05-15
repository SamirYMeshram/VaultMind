package com.vaultmind.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
    fun setUserProperty(name: String, value: String?)
    fun recordNonFatal(throwable: Throwable)
}

data class AnalyticsEvent(val name: String, val params: Map<String, String> = emptyMap()) {
    companion object {
        fun cardCreated(type: String) = AnalyticsEvent("card_created", mapOf("card_type" to type))
        fun cardUpdated(type: String) = AnalyticsEvent("card_updated", mapOf("card_type" to type))
        fun cardDeleted() = AnalyticsEvent("card_deleted")
        fun searchPerformed(length: Int) = AnalyticsEvent("search_performed", mapOf("query_length" to length.toString()))
        fun tagAdded() = AnalyticsEvent("tag_added")
        fun folderAdded() = AnalyticsEvent("folder_added")
        fun collectionAdded() = AnalyticsEvent("collection_added")
        fun backupExported() = AnalyticsEvent("backup_exported")
        fun backupImported() = AnalyticsEvent("backup_imported")
        fun relatedCardOpened() = AnalyticsEvent("related_card_opened")
        fun pinnedCardOpened() = AnalyticsEvent("pinned_card_opened")
    }
}

class FirebaseAnalyticsTracker @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {
        analytics.logEvent(event.name, Bundle().apply { event.params.forEach { (k, v) -> putString(k, v) } })
    }
    override fun setUserProperty(name: String, value: String?) = analytics.setUserProperty(name, value)
    override fun recordNonFatal(throwable: Throwable) = crashlytics.recordException(throwable)
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseAnalyticsProviderModule {
    @Provides @Singleton fun firebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    @Provides @Singleton fun firebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindingModule {
    @Binds @Singleton abstract fun tracker(impl: FirebaseAnalyticsTracker): AnalyticsTracker
}
