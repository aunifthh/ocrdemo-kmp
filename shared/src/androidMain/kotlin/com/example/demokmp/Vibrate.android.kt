package com.example.demokmp

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual fun vibrate(ok: Boolean) {
    val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= 31)
            appContext.getSystemService(VibratorManager::class.java).defaultVibrator
        else
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val ms = if (ok) 80L else 300L   // short = ok, long = problem
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
    }
}