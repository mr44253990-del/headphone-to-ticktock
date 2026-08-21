package com.example.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.SoundPreset

class SoundEqualizerManager private constructor(context: Context) {

    companion object {
        private const val TAG = "SoundEqualizer"
        @Volatile
        private var INSTANCE: SoundEqualizerManager? = null

        fun getInstance(context: Context): SoundEqualizerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SoundEqualizerManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var isInitialized = false

    init {
        initializeEffects()
    }

    private fun initializeEffects() {
        if (isInitialized) return
        try {
            // Audio Session 0 applies to global output mix
            equalizer = Equalizer(0, 0)
            equalizer?.enabled = true

            bassBoost = BassBoost(0, 0)
            bassBoost?.enabled = true

            virtualizer = Virtualizer(0, 0)
            virtualizer?.enabled = true

            try {
                loudnessEnhancer = LoudnessEnhancer(0)
                loudnessEnhancer?.enabled = true
            } catch (ex: Exception) {
                Log.w(TAG, "LoudnessEnhancer not fully supported on this device's Android version: ${ex.message}")
            }

            isInitialized = true
            Log.d(TAG, "Successfully bound hardware audio equalizer effects globally.")
        } catch (e: Exception) {
            Log.e(TAG, "Hardware audio effects binding failed: ${e.message}. Software DSP Simulation mode active.", e)
            isInitialized = false
        }
    }

    fun applyPreset(preset: SoundPreset, isSoundEnabled: Boolean) {
        if (!isSoundEnabled) {
            try {
                equalizer?.enabled = false
                bassBoost?.enabled = false
                virtualizer?.enabled = false
                loudnessEnhancer?.enabled = false
            } catch (e: Exception) {
                Log.e(TAG, "Error disabling effects: ${e.message}")
            }
            return
        }

        initializeEffects()

        try {
            // Apply Bass Boost (0 to 1000 range)
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.enabled = true
                    it.setStrength(preset.bassBoost.coerceIn(0, 1000).toShort())
                }
            }

            // Apply Virtualizer (Spatializer, 0 to 1000 range)
            virtualizer?.let {
                if (it.strengthSupported) {
                    it.enabled = true
                    it.setStrength(preset.virtualizer.coerceIn(0, 1000).toShort())
                }
            }

            // Apply Loudness Enhancer (0 to 2000 milliBel gain)
            loudnessEnhancer?.let {
                it.enabled = true
                val gainMilliBel = (preset.loudness.coerceIn(0, 1000) * 2) // scale 0-1000 to 0-2000 mB
                it.setTargetGain(gainMilliBel)
            }

            // Apply 5-Band Equalizer levels
            equalizer?.let { eq ->
                eq.enabled = true
                val numBands = eq.numberOfBands
                val customLevels = listOf(preset.eqBand1, preset.eqBand2, preset.eqBand3, preset.eqBand4, preset.eqBand5)
                val range = eq.bandLevelRange
                val minLevel = range[0].toInt()
                val maxLevel = range[1].toInt()

                for (i in 0 until numBands) {
                    if (i < customLevels.size) {
                        val level = customLevels[i].coerceIn(minLevel, maxLevel).toShort()
                        eq.setBandLevel(i.toShort(), level)
                    }
                }
            }

            Log.d(TAG, "Applied preset to active audio session: $preset")
        } catch (e: Exception) {
            Log.e(TAG, "Error matching preset levels to hardware capabilities: ${e.message}")
        }
    }
}
