package com.offshore.platform.mobile.data.local

import com.offshore.platform.mobile.data.local.entity.*
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pure JVM unit tests for entity mapper functions.
 * Tests JSON-to-Entity conversion with syncStatus = SYNCED and correct field mapping.
 *
 * Uses RobolectricTestRunner to work around Hilt ASM transform classpath filtering
 * (Kotlin 2.1.0 + AGP 8.7.3 + Hilt compatibility).
 */
@RunWith(RobolectricTestRunner::class)
class EntityMapperTest {

    @Test
    fun workOrderMapsSyncStatusSYNCED() {
        val json = buildJsonObject {
            put("workOrderNo", JsonPrimitive("WO-001"))
            put("workTitle", JsonPrimitive("Test Task"))
            put("projectName", JsonPrimitive("Platform A"))
            put("status", JsonPrimitive("ASSIGNED"))
            put("priority", JsonPrimitive("HIGH"))
        }
        val wo = json.toWorkOrderEntity(100L, 2, "2026-06-06 10:00", 1, "dev-1", 0, "2026-06-06 10:00")
        assertThat(wo.workOrderNo).isEqualTo("WO-001")
        assertThat(wo.serverId).isEqualTo(100L)
        assertThat(wo.version).isEqualTo(2)
        assertThat(wo.syncStatus).isEqualTo("SYNCED")
        assertThat(wo.status).isEqualTo("ASSIGNED")
    }

    @Test
    fun workOrderWithDeletedFlagTrue() {
        val json = buildJsonObject {
            put("workOrderNo", JsonPrimitive("WO-002"))
            put("workTitle", JsonPrimitive("Deleted"))
            put("deletedFlag", JsonPrimitive("1"))
        }
        val wo = json.toWorkOrderEntity(200L, 1, "2026-06-06", 1, "dev-1", 1, "now")
        assertThat(wo.deletedFlag).isEqualTo(1)
    }

    @Test
    fun workRecordMapsCorrectly() {
        val json = buildJsonObject {
            put("workOrderId", JsonPrimitive("99"))
            put("constructionDesc", JsonPrimitive("Welding done"))
            put("abnormalFlag", JsonPrimitive("1"))
            put("weather", JsonPrimitive("cloudy"))
        }
        val rec = json.toWorkRecordEntity(50L, 1, "2026-06-06", 1, "dev-1", 0, "now")
        assertThat(rec.workOrderId).isEqualTo(99L)
        assertThat(rec.constructionDesc).isEqualTo("Welding done")
        assertThat(rec.abnormalFlag).isEqualTo(1)
    }

    @Test
    fun attachmentMapsCorrectly() {
        val json = buildJsonObject {
            put("workOrderId", JsonPrimitive("99"))
            put("fileId", JsonPrimitive("file-xyz"))
            put("attachmentType", JsonPrimitive("PHOTO"))
            put("attachmentName", JsonPrimitive("site.jpg"))
        }
        val att = json.toAttachmentEntity(30L, 1, "2026-06-06", 1, "dev-1", 0, "now")
        assertThat(att.fileId).isEqualTo("file-xyz")
        assertThat(att.attachmentType).isEqualTo("PHOTO")
        assertThat(att.syncStatus).isEqualTo("SYNCED")
    }

    @Test
    fun entityPreservesOperatorIdAndDeviceId() {
        val json = buildJsonObject {
            put("workOrderNo", JsonPrimitive("WO-005"))
            put("workTitle", JsonPrimitive("Ctx"))
        }
        val wo = json.toWorkOrderEntity(700L, 1, "2026-06-06", 42L, "device-xyz", 0, "now")
        assertThat(wo.operatorId).isEqualTo(42L)
        assertThat(wo.deviceId).isEqualTo("device-xyz")
    }

    @Test
    fun localIdGeneratedWhenAbsent() {
        val json = buildJsonObject {
            put("workOrderNo", JsonPrimitive("WO-006"))
            put("workTitle", JsonPrimitive("Gen"))
        }
        val wo = json.toWorkOrderEntity(500L, 1, "now", 1, "d", 0, "now")
        assertThat(wo.localId).isNotEmpty()
        assertThat(wo.localId.startsWith("wo-")).isTrue()
    }
}
// Actually let me just edit the file directly
