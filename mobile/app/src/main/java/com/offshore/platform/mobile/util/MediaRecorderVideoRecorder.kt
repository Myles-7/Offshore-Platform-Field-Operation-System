package com.offshore.platform.mobile.util

import android.media.MediaRecorder
import com.offshore.platform.mobile.OffshoreApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Stable video recorder based on platform [MediaRecorder].
 *
 * Used by VideoRecordScreen on all devices (API 24+).
 * This is the CURRENT DEFAULT implementation.
 *
 * When CameraX VideoCapture is production-ready, it can be swapped in
 * by providing a [CameraXVideoRecorder] that also implements [VideoRecorder].
 */
class MediaRecorderVideoRecorder : VideoRecorder {

    override var state: VideoRecorder.State = VideoRecorder.State.IDLE
        private set
    override var outputFile: File? = null
        private set
    override var errorMessage: String? = null
        private set

    private var mediaRecorder: MediaRecorder? = null
    private var targetFile: File? = null

    override suspend fun prepare(outputDir: File, fileNameHint: String) {
        withContext(Dispatchers.IO) {
            state = VideoRecorder.State.INITIALISING
            outputDir.mkdirs()
            val ext = if (fileNameHint.endsWith(".mp4")) "" else ".mp4"
            targetFile = File(outputDir, "${fileNameHint}${ext}")

            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(OffshoreApp.instance)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoSize(1280, 720)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(3_000_000)
                setAudioEncodingBitRate(128_000)
                setOutputFile(targetFile!!.absolutePath)
                prepare()
            }
        }
    }

    override suspend fun start() {
        withContext(Dispatchers.IO) {
            try {
                mediaRecorder?.start()
                state = VideoRecorder.State.RECORDING
            } catch (e: Exception) {
                state = VideoRecorder.State.ERROR
                errorMessage = "录像启动失败: ${e.message}"
            }
        }
    }

    override suspend fun pause() {
        withContext(Dispatchers.IO) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                    state = VideoRecorder.State.PAUSED
                }
                // On API < 24, pause is not supported — we just continue recording
            } catch (e: Exception) {
                errorMessage = "暂停失败: ${e.message}"
            }
        }
    }

    override suspend fun resume() {
        withContext(Dispatchers.IO) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                    state = VideoRecorder.State.RECORDING
                }
            } catch (e: Exception) {
                errorMessage = "恢复失败: ${e.message}"
            }
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            try {
                mediaRecorder?.apply {
                    if (state == VideoRecorder.State.RECORDING || state == VideoRecorder.State.PAUSED) {
                        try { stop() } catch (_: Exception) {}
                    }
                    try { release() } catch (_: Exception) {}
                }
                mediaRecorder = null
                state = VideoRecorder.State.STOPPED
                outputFile = targetFile?.takeIf { it.exists() }
                if (outputFile == null) {
                    state = VideoRecorder.State.ERROR
                    errorMessage = "录像文件保存失败"
                }
            } catch (e: Exception) {
                state = VideoRecorder.State.ERROR
                errorMessage = "停止录像失败: ${e.message}"
            }
        }
    }

    override suspend fun release() {
        withContext(Dispatchers.IO) {
            try {
                mediaRecorder?.apply {
                    try { stop() } catch (_: Exception) {}
                    try { release() } catch (_: Exception) {}
                }
                mediaRecorder = null
                state = VideoRecorder.State.IDLE
            } catch (_: Exception) {}
        }
    }
}
