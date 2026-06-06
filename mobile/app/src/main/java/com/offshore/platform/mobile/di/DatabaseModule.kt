package com.offshore.platform.mobile.di

import android.content.Context
import androidx.room.Room
import com.offshore.platform.mobile.data.local.AppDatabase
import com.offshore.platform.mobile.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "offshore_mobile.db"
        )
            .fallbackToDestructiveMigration() // Only during early development
            .build()
    }

    // ---- business DAOs ----

    @Provides
    fun provideWorkOrderDao(db: AppDatabase): WorkOrderDao = db.workOrderDao()

    @Provides
    fun provideWorkOrderRecordDao(db: AppDatabase): WorkOrderRecordDao = db.workOrderRecordDao()

    @Provides
    fun provideAttachmentDao(db: AppDatabase): AttachmentDao = db.attachmentDao()

    @Provides
    fun provideSignatureDao(db: AppDatabase): SignatureDao = db.signatureDao()

    @Provides
    fun provideAcceptanceDao(db: AppDatabase): AcceptanceDao = db.acceptanceDao()

    @Provides
    fun providePdfDao(db: AppDatabase): PdfDao = db.pdfDao()

    @Provides
    fun provideMaterialRequirementDao(db: AppDatabase): MaterialRequirementDao =
        db.materialRequirementDao()

    @Provides
    fun provideMaterialUsageDao(db: AppDatabase): MaterialUsageDao = db.materialUsageDao()

    @Provides
    fun provideQualificationStatusDao(db: AppDatabase): QualificationStatusDao =
        db.qualificationStatusDao()

    @Provides
    fun provideAiResultDao(db: AppDatabase): AiResultDao = db.aiResultDao()

    @Provides
    fun provideKnowledgeCaseDao(db: AppDatabase): KnowledgeCaseDao = db.knowledgeCaseDao()

    // ---- sync subsystem DAOs ----

    @Provides
    fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()

    @Provides
    fun provideSyncLogDao(db: AppDatabase): SyncLogDao = db.syncLogDao()

    @Provides
    fun provideConflictHintDao(db: AppDatabase): ConflictHintDao = db.conflictHintDao()

    @Provides
    fun provideDeviceInfoDao(db: AppDatabase): DeviceInfoDao = db.deviceInfoDao()

    @Provides
    fun provideSyncCheckpointDao(db: AppDatabase): SyncCheckpointDao = db.syncCheckpointDao()
}
