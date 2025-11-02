package com.example.test.utils

import android.content.Context
import android.media.AudioManager
import android.view.SoundEffectConstants

object SoundManager {

    private var soundEnabled = true

    fun playClick(context: Context) {
        if (!soundEnabled) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun isSoundEnabled() = soundEnabled
}
