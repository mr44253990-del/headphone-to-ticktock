package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEqualizerManager
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.GestureMapping
import com.example.data.SoundPreset
import com.example.service.MediaAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    private val equalizerManager = SoundEqualizerManager.getInstance(application)

    val activePreset: StateFlow<SoundPreset> = repository.activePreset
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SoundPreset()
        )

    val activeMapping: StateFlow<GestureMapping> = repository.activeMapping
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GestureMapping()
        )

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            combine(activePreset, isSoundEnabled) { preset, enabled ->
                Pair(preset, enabled)
            }.collect { (preset, enabled) ->
                equalizerManager.applyPreset(preset, enabled)
            }
        }
    }

    fun toggleSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        viewModelScope.launch {
            equalizerManager.applyPreset(activePreset.value, enabled)
        }
    }

    fun setSoundMode(mode: String) {
        viewModelScope.launch {
            repository.applySoundModePreset(mode)
        }
    }

    fun updateBassBoost(value: Int) {
        viewModelScope.launch {
            val current = repository.getPresetDirect()
            repository.savePreset(current.copy(bassBoost = value, mode = "CUSTOM"))
        }
    }

    fun updateVirtualizer(value: Int) {
        viewModelScope.launch {
            val current = repository.getPresetDirect()
            repository.savePreset(current.copy(virtualizer = value, mode = "CUSTOM"))
        }
    }

    fun updateLoudness(value: Int) {
        viewModelScope.launch {
            val current = repository.getPresetDirect()
            repository.savePreset(current.copy(loudness = value, mode = "CUSTOM"))
        }
    }

    fun updateEqBand(bandIndex: Int, value: Int) {
        viewModelScope.launch {
            val current = repository.getPresetDirect()
            val updated = when (bandIndex) {
                0 -> current.copy(eqBand1 = value, mode = "CUSTOM")
                1 -> current.copy(eqBand2 = value, mode = "CUSTOM")
                2 -> current.copy(eqBand3 = value, mode = "CUSTOM")
                3 -> current.copy(eqBand4 = value, mode = "CUSTOM")
                4 -> current.copy(eqBand5 = value, mode = "CUSTOM")
                else -> current
            }
            repository.savePreset(updated)
        }
    }

    fun toggleGestureService(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(isGestureServiceEnabled = enabled))
        }
    }

    fun toggleFloatingBubble(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(isFloatingBubbleEnabled = enabled))
        }
    }

    fun toggleAutoScroll(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(isAutoScrollEnabled = enabled))
        }
    }

    fun updateAutoScrollInterval(seconds: Int) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(autoScrollIntervalSeconds = seconds))
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(isHapticFeedbackEnabled = enabled))
        }
    }

    fun updateSwipeSpeed(speed: String) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(swipeSpeed = speed))
        }
    }

    fun updateSinglePressAction(action: String) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(singlePressAction = action))
        }
    }

    fun updateDoublePressAction(action: String) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(doublePressAction = action))
        }
    }

    fun updateLongPressAction(action: String) {
        viewModelScope.launch {
            val current = repository.getMappingDirect()
            repository.saveMapping(current.copy(longPressAction = action))
        }
    }

    fun testDirectGesture(action: String) {
        MediaAccessibilityService.triggerDirectAction(action)
    }

    class Factory(
        private val application: Application,
        private val repository: AppRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
