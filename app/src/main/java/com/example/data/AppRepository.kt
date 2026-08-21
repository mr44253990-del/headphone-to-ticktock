package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(private val db: AppDatabase) {

    val activePreset: Flow<SoundPreset> = db.soundPresetDao().getPresetFlow()
        .map { it ?: SoundPreset() }

    val activeMapping: Flow<GestureMapping> = db.gestureMappingDao().getMappingFlow()
        .map { it ?: GestureMapping() }

    suspend fun getPresetDirect(): SoundPreset {
        return db.soundPresetDao().getPresetDirect() ?: SoundPreset()
    }

    suspend fun getMappingDirect(): GestureMapping {
        return db.gestureMappingDao().getMappingDirect() ?: GestureMapping()
    }

    suspend fun savePreset(preset: SoundPreset) {
        db.soundPresetDao().savePreset(preset)
    }

    suspend fun saveMapping(mapping: GestureMapping) {
        db.gestureMappingDao().saveMapping(mapping)
    }

    suspend fun applySoundModePreset(mode: String) {
        val current = getPresetDirect()
        val updated = when (mode) {
            "MUSIC" -> SoundPreset(
                id = current.id,
                mode = "MUSIC",
                bassBoost = 400,
                virtualizer = 300,
                loudness = 300,
                eqBand1 = 200,
                eqBand2 = 100,
                eqBand3 = 0,
                eqBand4 = 100,
                eqBand5 = 300
            )
            "GAMING" -> SoundPreset(
                id = current.id,
                mode = "GAMING",
                bassBoost = 300,
                virtualizer = 600, // wider spatial environment for directional sounds
                loudness = 500, // amplified dialog/footsteps
                eqBand1 = -100,
                eqBand2 = 100,
                eqBand3 = 300,
                eqBand4 = 400,
                eqBand5 = 200
            )
            "DJ" -> SoundPreset(
                id = current.id,
                mode = "DJ",
                bassBoost = 950, // maximum bass thump
                virtualizer = 400,
                loudness = 700, // high amplifier/gain
                eqBand1 = 1200, // boost sub-bass
                eqBand2 = 800,  // boost low-mid
                eqBand3 = -200, // scoop mids for V-shape
                eqBand4 = 400,  // treble definition
                eqBand5 = 1000  // high sizzle
            )
            else -> current.copy(mode = "CUSTOM")
        }
        db.soundPresetDao().savePreset(updated)
    }
}
