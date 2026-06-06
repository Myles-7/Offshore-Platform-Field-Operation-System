package com.offshore.platform.mobile.util

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicReference

/**
 * Lightweight audio player for voice notes (MediaPlayer-based).
 *
 * Design:
 *   - Single-player model — only one audio plays at a time.
 *   - Starting a new track automatically stops any currently-playing track.
 *   - Player state is exposed via StateFlow for Compose integration.
 *   - No complex ExoPlayer dependency — voice notes are short, local files.
 *   - Compatible with Android 7.0+ (API 24+).
 *
 * Threading: MediaPlayer operations run on a dedicated background thread
 * to avoid blocking the UI.
 */
object AudioPlayerManager {

    enum class State { IDLE, LOADING, PLAYING, PAUSED, STOPPED, ERROR }

    data class PlayerState(
        val state: State = State.IDLE,
        val currentFile: File? = null,
        val currentFileName: String? = null,
        val durationMs: Int = 0,
        val currentPositionMs: Int = 0,
        val progress: Float = 0f, // 0.0 - 1.0
        val error: String? = null
    ) {
        val durationFormatted: String get() = formatDuration(durationMs)
        val positionFormatted: String get() = formatDuration(currentPositionMs)
    }

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val mediaPlayer = AtomicReference<MediaPlayer?>(null)
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentFilePath: String? = null

    /** Start playing a local audio file. Stops any previous playback. */
    fun play(file: File) {
        if (!file.exists()) {
            _state.value = _state.value.copy(
                state = State.ERROR,
                error = "文件不存在或已被清理",
                currentFile = null,
                currentFileName = null
            )
            return
        }

        // Stop any currently-playing audio
        stopInternal()

        currentFilePath = file.absolutePath
        _state.value = PlayerState(
            state = State.LOADING,
            currentFile = file,
            currentFileName = file.name
        )

        scope.launch {
            try {
                val mp = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MediaPlayer()
                } else {
                    @Suppress("DEPRECATION")
                    MediaPlayer()
                }

                mp.setDataSource(file.absolutePath)
                mp.prepare()
                val duration = mp.duration

                mp.setOnCompletionListener {
                    _state.value = _state.value.copy(
                        state = State.STOPPED,
                        currentPositionMs = duration,
                        progress = 1f
                    )
                    progressJob?.cancel()
                }

                mp.setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what extra=$extra")
                    _state.value = _state.value.copy(
                        state = State.ERROR,
                        error = "播放出错 (code=$what)"
                    )
                    progressJob?.cancel()
                    true
                }

                mp.start()
                mediaPlayer.set(mp)

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        state = State.PLAYING,
                        durationMs = duration
                    )
                }

                // Progress tracking coroutine
                progressJob = scope.launch {
                    while (isActive) {
                        try {
                            val pos = mp.currentPosition
                            val dur = mp.duration
                            if (dur > 0) {
                                withContext(Dispatchers.Main) {
                                    _state.value = _state.value.copy(
                                        currentPositionMs = pos,
                                        progress = pos.toFloat() / dur.toFloat()
                                    )
                                }
                            }
                            delay(250)
                        } catch (_: Exception) { break }
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Failed to play: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        state = State.ERROR,
                        error = "播放失败: ${e.message}"
                    )
                }
            }
        }
    }

    /** Pause playback. */
    fun pause() {
        val mp = mediaPlayer.get() ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _state.value = _state.value.copy(state = State.PAUSED)
                progressJob?.cancel()
            }
        } catch (_: Exception) {}
    }

    /** Resume playback from pause. */
    fun resume() {
        val mp = mediaPlayer.get() ?: return
        try {
            mp.start()
            _state.value = _state.value.copy(state = State.PLAYING)
            // Restart progress tracking
            progressJob = scope.launch {
                while (isActive) {
                    try {
                        val pos = mp.currentPosition
                        val dur = mp.duration
                        if (dur > 0) {
                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(
                                    currentPositionMs = pos,
                                    progress = pos.toFloat() / dur.toFloat()
                                )
                            }
                        }
                        delay(250)
                    } catch (_: Exception) { break }
                }
            }
        } catch (_: Exception) {}
    }

    /** Stop playback and release the MediaPlayer. */
    fun stop() {
        stopInternal()
        _state.value = PlayerState(state = State.STOPPED)
    }

    /** Release resources — call from ViewModel onCleared / page exit. */
    fun release() {
        stopInternal()
        _state.value = PlayerState(state = State.IDLE)
    }

    private fun stopInternal() {
        progressJob?.cancel()
        progressJob = null
        currentFilePath = null
        val mp = mediaPlayer.getAndSet(null)
        try {
            mp?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
    }

    private fun formatDuration(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }
}
