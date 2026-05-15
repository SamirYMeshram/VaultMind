package com.vaultmind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        KnowledgeCardEntity::class, TagEntity::class, CardTagCrossRef::class, FolderEntity::class,
        CollectionEntity::class, CardCollectionCrossRef::class, AttachmentEntity::class,
        RecentActivityEntity::class, SearchHistoryEntity::class, BackupMetadataEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VaultMindDatabase : RoomDatabase() {
    abstract fun knowledgeCardDao(): KnowledgeCardDao
    abstract fun tagDao(): TagDao
    abstract fun folderDao(): FolderDao
    abstract fun collectionDao(): CollectionDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun recentActivityDao(): RecentActivityDao
    abstract fun searchDao(): SearchDao
    abstract fun backupDao(): BackupDao
}
