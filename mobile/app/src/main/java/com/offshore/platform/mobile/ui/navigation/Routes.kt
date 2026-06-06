package com.offshore.platform.mobile.ui.navigation

/**
 * Route constants for Compose Navigation.
 */
object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LOGIN = "login"
    const val WORK_ORDER_LIST = "work_order_list"
    const val WORK_ORDER_DETAIL = "work_order_detail/{workOrderId}"
    const val WORK_ORDER_RECORDS = "work_order_records/{workOrderId}"
    const val WORK_ORDER_ATTACHMENTS = "work_order_attachments/{workOrderId}"
    const val RECORD_CREATE = "record_create/{workOrderId}"
    const val RECORD_EDIT = "record_edit/{recordId}"
    const val CAMERA = "camera"
    const val CAMERA_WITH_IDS = "camera_wo/{workOrderId}/{recordId}/{workOrderNo}/{workLocation}"
    const val VIDEO_RECORD = "video_record/{workOrderId}/{recordId}/{workOrderNo}"
    const val AUDIO_RECORD = "audio_record/{workOrderId}/{recordId}/{workOrderNo}"
    const val SIGNATURE = "signature/{workOrderId}"
    const val PDF_VIEW = "pdf_view/{workOrderId}"
    const val PDF_PREVIEW = "pdf_preview/{workOrderId}"
    const val SYNC_CENTER = "sync_center"
    const val MATERIAL_USAGE = "material_usage/{workOrderId}"
    const val QUALIFICATION_STATUS = "qualification_status"
    const val AI_RESULTS = "ai_results/{workOrderId}"
    const val KNOWLEDGE = "knowledge"
    const val MINE = "mine"

    fun workOrderDetail(id: Long) = "work_order_detail/$id"
    fun recordCreate(workOrderId: Long) = "record_create/$workOrderId"
    fun recordEdit(recordId: Long) = "record_edit/$recordId"
    fun signature(workOrderId: Long) = "signature/$workOrderId"
    fun pdfView(workOrderId: Long) = "pdf_view/$workOrderId"
    fun materialUsage(workOrderId: Long) = "material_usage/$workOrderId"
    fun aiResults(workOrderId: Long) = "ai_results/$workOrderId"
}
