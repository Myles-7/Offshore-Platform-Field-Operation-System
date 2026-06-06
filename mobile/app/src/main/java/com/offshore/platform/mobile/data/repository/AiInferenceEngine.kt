package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.domain.model.AiInferenceResult
import java.io.File

/**
 * Pluggable AI inference engine — supports swapping between
 * mock (offline), TFLite (on-device), and remote (server).
 */
interface AiInferenceEngine {

    /** Display name for UI. */
    val name: String

    /** Whether this engine supports offline (no-network) inference. */
    val supportsOffline: Boolean

    /**
     * Run inference on a local photo file.
     * Returns null if the engine cannot process this file.
     */
    suspend fun infer(file: File): AiInferenceResult?
}

/**
 * Mock AI engine — always returns a deterministic dummy result.
 * Used during development and when real models are not loaded.
 */
class MockAiInferenceEngine : AiInferenceEngine {
    override val name = "Mock AI (Dev)"
    override val supportsOffline = true

    private var callCount = 0

    override suspend fun infer(file: File): AiInferenceResult {
        callCount++
        val defects = listOf("PEELING", "CRACK", "RUST", "NORMAL")
        val idx = callCount % defects.size
        return AiInferenceResult(
            defectType = defects[idx],
            confidence = (0.65f + idx * 0.05f).coerceAtMost(0.99f),
            suspectedDefectFlag = defects[idx] != "NORMAL",
            modelVersion = "mock-v1.0",
            inferenceTimeMs = 120L,
            boxes = emptyList()
        )
    }
}

/**
 * Placeholder for TFLite on-device inference.
 * Will be implemented when a TFLite model is available.
 */
class TFLiteAiInferenceEngine : AiInferenceEngine {
    override val name = "TFLite (待接入)"
    override val supportsOffline = true

    override suspend fun infer(file: File): AiInferenceResult? {
        // TODO: Load .tflite model, run inference, return AiInferenceResult
        return null
    }
}
