package com.example.instagram

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class Haptics(private val context: Context) {
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    fun light() {
        vibrator.vibrate(VibrationEffect.createOneShot(10, 120))
    }
    
    fun doubleClick() {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
    }
    
    fun heavy() {
        vibrator.vibrate(VibrationEffect.createOneShot(10, 255))
    }
}