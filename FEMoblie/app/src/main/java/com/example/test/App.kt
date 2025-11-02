package com.example.test

import android.app.Application
import com.example.test.data.SettingsRepository
import com.example.test.utils.SoundManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Load sound preference from DataStore
        applicationScope.launch {
            val repo = SettingsRepository(this@App)
            val soundEnabled = repo.soundEnabledFlow.first()
            SoundManager.setSoundEnabled(soundEnabled)
        }
    }
}
