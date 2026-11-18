package com.example.demokmp

import android.content.Context

// Android needs a "Context" to save anything. MainActivity sets this when the app starts.
lateinit var appContext: Context

private val prefs
    get() = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

actual fun loadSaved(key: String): String? =
    prefs.getString(key, null)

actual fun save(key: String, value: String) {
    prefs.edit().putString(key, value).apply()
}