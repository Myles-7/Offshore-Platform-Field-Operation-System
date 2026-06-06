package com.offshore.platform.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.offshore.platform.mobile.data.local.converter.RoomTypeConverters
import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*

/**
 * Room database for offline-first mobile operations.
 *
 * Migration strategy:
 *   - Version 2: initial schema with all entities.
 *   - Future versions: add Migration(N, N+1) via [DatabaseModule].
 *   - Use fallbackToDestructiveMigration() during early development only.
 *
 * Rules:
 *   - No file blobs — only paths and metadata.
 *   - Sync queue survives app restarts.
 *   - All syncable entities carry localId/serverId/version/syncStatus.
 */
@Database(
    entities = [
        LocalWorkOrderEntity::class,
        LocalWorkOrderRecordEntity::class,
        LocalWorkOrderAttachmentEntity::class,
        LocalSignatureEntity::class,
        LocalAcceptanceEntity::class,
        LocalPdfEntity::class,
        LocalMaterialRequirementEntity::class,
        LocalMaterialUsageEntity::class,
        LocalQualificationStatusEntity::class,
        LocalAiResultEntity::class,
        LocalKnowledgeCaseEntity::class,
        LocalSyncQueueEntity::class,
        LocalSyncLogEntity::class,
        LocalConflictHintEntity::class,
        LocalDeviceInfoEntity::class,
        LocalSyncCheckpointEntity::class,
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // ---- business DAOs ----
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun workOrderRecordDao(): WorkOrderRecordDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun signatureDao(): SignatureDao
    abstract fun acceptanceDao(): AcceptanceDao
    abstract fun pdfDao(): PdfDao
    abstract fun materialRequirementDao(): MaterialRequirementDao
    abstract fun materialUsageDao(): MaterialUsageDao
    abstract fun qualificationStatusDao(): QualificationStatusDao
    abstract fun aiResultDao(): AiResultDao
    abstract fun knowledgeCaseDao(): KnowledgeCaseDao

    // ---- sync subsystem DAOs ----
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun conflictHintDao(): ConflictHintDao
    abstract fun deviceInfoDao(): DeviceInfoDao
    abstract fun syncCheckpointDao(): SyncCheckpointDao
}
