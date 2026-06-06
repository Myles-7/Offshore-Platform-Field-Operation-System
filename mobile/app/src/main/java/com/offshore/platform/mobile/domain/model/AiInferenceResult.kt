package com.offshore.platform.mobile.domain.model

import kotlinx.serialization.Serializable

/**
 * AI defect detection box — matches backend AiDefectBox.
 */
@Serializable
data class AiDefectBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val defectType: String,
    val confidence: Float,
    val boxLabel: String? = null
)

/**
 * AI inference result from any engine (local or remote).
 */
data class AiInferenceResult(
    val defectType: String,
    val confidence: Float,
    val suspectedDefectFlag: Boolean,
    val modelVersion: String,
    val inferenceTimeMs: Long,
    val boxes: List<AiDefectBox> = emptyList()
)
