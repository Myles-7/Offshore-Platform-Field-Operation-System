package com.offshore.platform.mobile.util

import java.io.File

/**
 * Abstract interface for video recording.
 *
 * Implementations:
 *   - [MediaRecorderVideoRecorder]: Current stable implementation using platform MediaRecorder.
 *   - [CameraXVideoRecorder]: Placeholder for future CameraX VideoCapture integration.
 *
 * This abstraction allows swapping the implementation without changing
 * UI code. VideoRecordScreen depends on this interface, not any concrete class.
 */
interface VideoRecorder {

    /** Recorder state. */
    enum class State { IDLE, INITIALISING, RECORDING, PAUSED, STOPPED, SAVED, ERROR }

    /** Current recorder state. */
    val state: State

    /** Path of the recorded video file when State == SAVED. */
    val outputFile: File?

    /** Error message when State == ERROR. */
    val errorMessage: String?

    /**
     * Prepare the recorder — allocate resources, open device.
     * Must be called before [start].
     *
     * @param outputDir Directory to save the video file.
     * @param fileNameHint Suggested file name prefix.
     */
    suspend fun prepare(outputDir: File, fileNameHint: String)

    /** Start recording. Blocks until recording has started. */
    suspend fun start()

    /** Pause recording (optional — not all encoders support this). */
    suspend fun pause()

    /** Resume recording after pause. */
    suspend fun resume()

    /** Stop recording and finalise the video file. */
    suspend fun stop()

    /** Release all resources. Safe to call at any time. */
    suspend fun release()
}
