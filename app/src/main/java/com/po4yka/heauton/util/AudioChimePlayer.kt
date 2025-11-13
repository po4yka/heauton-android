package com.po4yka.heauton.util

import android.media.AudioAttributes
import android.media.ToneGenerator
import androidx.annotation.MainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for playing audio chimes.
 *
 * Uses ToneGenerator to play system tones for exercise phase transitions.
 */
@Singleton
class AudioChimePlayer @Inject constructor() {

    private var toneGenerator: ToneGenerator? = null

    init {
        initializeToneGenerator()
    }

    /**
     * Initialize the tone generator.
     */
    private fun initializeToneGenerator() {
        try {
            toneGenerator = ToneGenerator(
                AudioAttributes.USAGE_NOTIFICATION,
                ToneGenerator.MAX_VOLUME / 2 // 50% volume
            )
        } catch (e: Exception) {
            // Failed to initialize tone generator
            toneGenerator = null
        }
    }

    /**
     * Play a chime sound.
     *
     * This method plays a short, pleasant tone to indicate exercise phase transitions.
     */
    suspend fun playChime() {
        withContext(Dispatchers.Default) {
            try {
                toneGenerator?.let { generator ->
                    // Play a pleasant notification tone (PROP_BEEP)
                    generator.startTone(ToneGenerator.TONE_PROP_BEEP, 200) // 200ms
                } ?: run {
                    // If tone generator failed to initialize, try to reinitialize
                    initializeToneGenerator()
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                }
            } catch (e: Exception) {
                // Silently fail if we can't play the tone
            }
        }
    }

    /**
     * Play a completion chime (slightly different tone for completion).
     */
    suspend fun playCompletionChime() {
        withContext(Dispatchers.Default) {
            try {
                toneGenerator?.let { generator ->
                    // Play a higher tone for completion
                    generator.startTone(ToneGenerator.TONE_PROP_ACK, 300) // 300ms
                } ?: run {
                    initializeToneGenerator()
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                }
            } catch (e: Exception) {
                // Silently fail if we can't play the tone
            }
        }
    }

    /**
     * Release resources.
     * Call this when the player is no longer needed.
     */
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
