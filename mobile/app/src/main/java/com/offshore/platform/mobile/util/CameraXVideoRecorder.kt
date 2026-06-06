package com.offshore.platform.mobile.util

import java.io.File

/**
 * Placeholder for CameraX VideoCapture integration.
 *
 * CameraX video recording requires:
 *   1. CameraX VideoCapture use case (camera-video artifact)
 *   2. A real device with CameraX-compatible camera HAL
 *   3. Testing on target API 24+ devices in the field
 *
 * Until all three are confirmed:
 *   - [MediaRecorderVideoRecorder] remains the DEFAULT.
 *   - This class is a reserved extension point.
 *   - Switching is done by providing this instance to VideoRecordScreen
 *     instead of MediaRecorderVideoRecorder.
 *
 * To integrate when ready:
 *   1. Create VideoCapture use case alongside Preview + ImageCapture.
 *   2. Route VideoCapture.OnVideoSavedCallback → outputFile.
 *   3. Update state transitions to match CameraX lifecycle.
 *   4. Test on real offshore devices with weak network.
 */
class CameraXVideoRecorder : VideoRecorder {

    override var state: VideoRecorder.State = VideoRecorder.State.IDLE
        private set
    override var outputFile: File? = null
        private set
    override var errorMessage: String? = null
        private set

    override suspend fun prepare(outputDir: File, fileNameHint: String) {
        // TODO: Set up VideoCapture use case
        // - Bind to lifecycle with CameraSelector
        // - Configure Quality.HIGHEST
        // - Set OutputFileOptions
        state = VideoRecorder.State.ERROR
        errorMessage = "CameraX VideoCapture is not yet implemented — using MediaRecorder"
    }

    override suspend fun start() {
        state = VideoRecorder.State.ERROR
        errorMessage = "CameraX VideoCapture is not yet implemented — using MediaRecorder"
    }

    override suspend fun pause() {
        // Not supported in CameraX VideoCapture
    }

    override suspend fun resume() {
        // Not supported in CameraX VideoCapture
    }

    override suspend fun stop() {
        state = VideoRecorder.State.ERROR
        errorMessage = "CameraX VideoCapture is not yet implemented — using MediaRecorder"
    }

    override suspend fun release() {
        state = VideoRecorder.State.IDLE
    }
}
