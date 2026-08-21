package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sound_presets")
data class SoundPreset(
    @PrimaryKey val id: String = "active_preset",
    val mode: String = "MUSIC", // MUSIC, GAMING, DJ, CUSTOM
    val bassBoost: Int = 500, // 0 to 1000
    val virtualizer: Int = 300, // 0 to 1000
    val loudness: Int = 400, // 0 to 1000
    val eqBand1: Int = 300, // -1500 to 1500 milliBels
    val eqBand2: Int = 100,
    val eqBand3: Int = -100,
    val eqBand4: Int = 200,
    val eqBand5: Int = 500
)

@Entity(tableName = "gesture_mappings")
data class GestureMapping(
    @PrimaryKey val id: String = "active_mapping",
    val singlePressAction: String = "SCROLL_DOWN", // SCROLL_DOWN, SCROLL_UP, LIKE, PLAY_PAUSE, NONE
    val doublePressAction: String = "LIKE",
    val longPressAction: String = "PLAY_PAUSE",
    val isGestureServiceEnabled: Boolean = true,
    val isFloatingBubbleEnabled: Boolean = false, // Quick Floating "Vabol" Assistive Button
    val isAutoScrollEnabled: Boolean = false, // Auto-scroll timer
    val autoScrollIntervalSeconds: Int = 15, // 10s, 15s, 30s, 45s, 60s
    val isHapticFeedbackEnabled: Boolean = true,
    val swipeSpeed: String = "NORMAL" // FAST, NORMAL, SMOOTH
)

@Dao
interface SoundPresetDao {
    @Query("SELECT * FROM sound_presets WHERE id = :id LIMIT 1")
    fun getPresetFlow(id: String = "active_preset"): Flow<SoundPreset?>

    @Query("SELECT * FROM sound_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetDirect(id: String = "active_preset"): SoundPreset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreset(preset: SoundPreset)
}

@Dao
interface GestureMappingDao {
    @Query("SELECT * FROM gesture_mappings WHERE id = :id LIMIT 1")
    fun getMappingFlow(id: String = "active_mapping"): Flow<GestureMapping?>

    @Query("SELECT * FROM gesture_mappings WHERE id = :id LIMIT 1")
    suspend fun getMappingDirect(id: String = "active_mapping"): GestureMapping?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMapping(mapping: GestureMapping)
}

@Database(entities = [SoundPreset::class, GestureMapping::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundPresetDao(): SoundPresetDao
    abstract fun gestureMappingDao(): GestureMappingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_scroll_dj_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
