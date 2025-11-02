package com.example.test.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.SettingsRepository
import com.example.test.utils.SoundManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)
    val darkMode = repo.darkFlow.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val soundEnabled = repo.soundEnabledFlow.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setDarkMode(v: Boolean) = viewModelScope.launch { repo.setDark(v) }
    fun setSoundEnabled(v: Boolean) = viewModelScope.launch {
        repo.setSoundEnabled(v)
        SoundManager.setSoundEnabled(v)
    }
}
