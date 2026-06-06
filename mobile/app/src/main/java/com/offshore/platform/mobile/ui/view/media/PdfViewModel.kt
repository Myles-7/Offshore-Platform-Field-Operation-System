package com.offshore.platform.mobile.ui.view.media

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.PdfUtil
import com.offshore.platform.mobile.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class PdfUiState(
    val isLoading: Boolean = true,
    val pdfFilePath: String? = null,
    val pdfFile: File? = null,
    val pdfEntity: LocalPdfEntity? = null,
    val isGenerating: Boolean = false,
    val isSharing: Boolean = false,
    val showMissingFileDialog: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val acceptanceLocked: Boolean = false
)

@HiltViewModel
class PdfViewModel @Inject constructor(
    private val pdfDao: PdfDao,
    private val workOrderDao: WorkOrderDao,
    private val recordDao: WorkOrderRecordDao,
    private val attachmentDao: AttachmentDao,
    private val signatureDao: SignatureDao,
    private val acceptanceDao: AcceptanceDao,
    private val materialUsageDao: MaterialUsageDao,
    private val aiResultDao: AiResultDao,
    private val syncQueueDao: SyncQueueDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfUiState())
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    private var workOrderId: Long = 0

    fun init(woId: Long) {
        workOrderId = woId
        loadExistingPdf()
    }

    /** Load most recent PDF for this work order. */
    private fun loadExistingPdf() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val pdfs = pdfDao.getByWorkOrderId(workOrderId)
            val latest = pdfs.firstOrNull()
            val file = latest?.localFilePath?.let { File(it) }
            val exists = file?.exists() == true

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pdfEntity = latest,
                pdfFilePath = if (exists) latest?.localFilePath else null,
                pdfFile = if (exists) file else null,
                showMissingFileDialog = latest != null && !exists,
                acceptanceLocked = latest?.archivedFlag == 1
            )
        }
    }

    /** Open PDF with system PDF viewer via Intent.ACTION_VIEW. */
    fun openPdf(context: Context) {
        val file = _uiState.value.pdfFile
        if (file == null || !file.exists()) {
            _uiState.value = _uiState.value.copy(
                showMissingFileDialog = true,
                message = "PDF文件不存在或已被清理，请重新生成"
            )
            return
        }
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "查看PDF验收单")
            context.startActivity(chooser)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                message = "没有可用的PDF查看器: ${e.message}"
            )
        }
    }

    /** Share PDF via Intent.ACTION_SEND + FileProvider. */
    fun sharePdf(context: Context) {
        val file = _uiState.value.pdfFile
        if (file == null || !file.exists()) {
            _uiState.value = _uiState.value.copy(
                showMissingFileDialog = true,
                message = "PDF文件不存在或已被清理，请重新生成"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true)
            try {
                withContext(Dispatchers.Main) {
                    val authority = "${context.packageName}.fileprovider"
                    val uri = FileProvider.getUriForFile(context, authority, file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "验收单-${_uiState.value.pdfEntity?.pdfNo ?: "PDF"}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(intent, "分享PDF验收单")
                    try {
                        context.startActivity(chooser)
                    } catch (e: Exception) {
                        Toast.makeText(context, "没有可用的应用来分享PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "分享失败: ${e.message}",
                    error = e.message
                )
            } finally {
                _uiState.value = _uiState.value.copy(isSharing = false)
            }
        }
    }

    /** Generate a new PDF from current work order data. */
    fun generatePdf(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)

            try {
                // Check if acceptance is locked
                val acceptance = acceptanceDao.getByWorkOrderId(workOrderId)
                if (acceptance != null && acceptance.lockedFlag == 1) {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        acceptanceLocked = true,
                        message = "验收记录已锁定，不能覆盖旧PDF。如需重新生成，请先解锁验收记录。"
                    )
                    return@launch
                }

                val wo = workOrderDao.getByServerId(workOrderId)
                val records = withContext(Dispatchers.IO) {
                    recordDao.getByWorkOrderIdSync(workOrderId)
                }
                val attachments = withContext(Dispatchers.IO) {
                    attachmentDao.getByWorkOrderIdSync(workOrderId)
                }
                val signatures = signatureDao.getByWorkOrderId(workOrderId)
                val materials = withContext(Dispatchers.IO) {
                    materialUsageDao.getByWorkOrderIdSync(workOrderId)
                }
                val aiResults = withContext(Dispatchers.IO) {
                    aiResultDao.getByWorkOrderIdSync(workOrderId)
                }

                // Build material usage summary
                val materialSummary = if (materials.isNotEmpty()) {
                    materials.joinToString("; ") { "${it.materialName}×${it.usageQty}${it.unit ?: ""}" }
                } else null

                // Build construction description from records
                val constructionDesc = records.joinToString("\n") { rec ->
                    buildString {
                        rec.constructionTime?.let { append("[$it] ") }
                        rec.constructionDesc?.let { append(it) }
                        rec.siteCondition?.let { append(" (现场: $it)") }
                    }
                }.takeIf { it.isNotBlank() }

                // AI summary
                val aiSummary = aiResults.firstOrNull()?.let {
                    "缺陷类型: ${it.defectType ?: "未知"}, 置信度: ${"%.1f".format(it.confidence * 100)}%, " +
                    "模型版本: ${it.modelVersion ?: "N/A"}, 状态: ${it.reviewStatus}"
                }

                // Load signature bitmap
                val sigBmp = signatures.firstOrNull()?.localFilePath?.let { path ->
                    val sigFile = File(path)
                    if (sigFile.exists()) BitmapFactory.decodeFile(path) else null
                }

                val pdfData = PdfUtil.PdfData(
                    workOrderNo = wo?.workOrderNo ?: "WO-$workOrderId",
                    projectName = wo?.projectName,
                    workLocation = wo?.workLocation,
                    workContent = wo?.workContentSummary,
                    maintainerName = wo?.maintainerName,
                    leaderName = wo?.leaderName,
                    workType = wo?.workType,
                    priority = wo?.priority,
                    plannedStartTime = wo?.plannedStartTime,
                    plannedEndTime = wo?.plannedEndTime,
                    actualStartTime = wo?.actualStartTime,
                    actualEndTime = wo?.actualEndTime,
                    constructionDesc = constructionDesc,
                    siteCondition = records.firstOrNull()?.siteCondition,
                    materialUsage = materialSummary,
                    attachmentCount = attachments.size,
                    acceptorName = acceptance?.acceptorName ?: TokenManager.getRealName(),
                    acceptanceOpinion = acceptance?.acceptanceOpinion,
                    acceptanceTime = acceptance?.acceptanceTime,
                    signatureBitmap = sigBmp,
                    aiSummary = aiSummary
                )

                val pdfFile = withContext(Dispatchers.IO) {
                    PdfUtil.generate(context, pdfData)
                }

                if (pdfFile == null) {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = "PDF生成失败，请稍后重试"
                    )
                    return@launch
                }

                val now = DateTimeUtil.nowFormatted()
                val localId = "pdf-${UUID.randomUUID()}"
                val pdfNo = "PDF-${DateTimeUtil.fileNameTimestamp()}"

                // Previous PDF entity
                val prevPdf = _uiState.value.pdfEntity
                val isRegeneration = prevPdf != null
                val version = if (isRegeneration) (prevPdf?.version ?: 0) + 1 else 1

                // Save to local_pdf
                val entity = LocalPdfEntity(
                    localId = localId,
                    workOrderId = workOrderId,
                    acceptanceId = acceptance?.id,
                    pdfNo = pdfNo,
                    pdfTitle = "验收单-${wo?.workOrderNo ?: "WO-$workOrderId"}",
                    pdfStatus = "GENERATED",
                    generatedAt = now,
                    generatorName = TokenManager.getRealName() ?: TokenManager.getUsername(),
                    pageCount = 1,
                    localFilePath = pdfFile.absolutePath,
                    fileSize = pdfFile.length(),
                    version = version,
                    syncStatus = "LOCAL_ONLY",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now,
                    updatedAt = now
                )
                pdfDao.insert(entity)

                // Save as attachment metadata (file is local, metadata goes to sync queue)
                val attLocalId = "att-${UUID.randomUUID()}"
                val attEntity = LocalWorkOrderAttachmentEntity(
                    localId = attLocalId,
                    workOrderId = workOrderId,
                    attachmentType = "PDF",
                    attachmentName = pdfFile.name,
                    captureTime = now,
                    captureUserName = TokenManager.getRealName() ?: TokenManager.getUsername(),
                    watermarkFlag = 0,
                    localFilePath = pdfFile.absolutePath,
                    fileSize = pdfFile.length(),
                    mimeType = "application/pdf",
                    uploadStatus = "PENDING",
                    syncStatus = "LOCAL_ONLY",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now,
                    updatedAt = now
                )
                attachmentDao.insert(attEntity)

                // Enqueue both to sync queue
                val pdfPayload = buildJsonObject {
                    put("localId", localId)
                    put("workOrderId", workOrderId)
                    put("pdfNo", pdfNo)
                    put("pdfTitle", "验收单-${wo?.workOrderNo ?: "WO-$workOrderId"}")
                    put("version", version)
                    put("deviceId", DeviceManager.getOrCreate())
                }
                syncQueueDao.enqueue(LocalSyncQueueEntity(
                    moduleType = "PDF", entityType = "WORK_ORDER_PDF",
                    localId = localId, serverId = prevPdf?.serverId,
                    workOrderId = workOrderId, actionType = if (isRegeneration) "UPDATE" else "CREATE",
                    payloadJson = Json.encodeToString(JsonObject.serializer(), pdfPayload),
                    syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                ))

                val attPayload = buildJsonObject {
                    put("localId", attLocalId)
                    put("attachmentType", "PDF")
                    put("attachmentName", pdfFile.name)
                    put("captureTime", now)
                    put("deviceId", DeviceManager.getOrCreate())
                }
                syncQueueDao.enqueue(LocalSyncQueueEntity(
                    moduleType = "ATTACHMENT", entityType = "WORK_ORDER_ATTACHMENT",
                    localId = attLocalId, workOrderId = workOrderId, actionType = "CREATE",
                    payloadJson = Json.encodeToString(JsonObject.serializer(), attPayload),
                    syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                ))

                // Trigger upload worker for the new files
                try {
                    com.offshore.platform.mobile.worker.UploadWorker.enqueueOneTime(context)
                } catch (_: Exception) {}

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    pdfEntity = entity,
                    pdfFilePath = pdfFile.absolutePath,
                    pdfFile = pdfFile,
                    showMissingFileDialog = false,
                    message = if (isRegeneration) "PDF已重新生成（版本 $version）" else "PDF已生成"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = "生成失败: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
