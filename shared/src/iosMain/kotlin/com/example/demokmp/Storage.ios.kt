package com.example.demokmp

import platform.Foundation.NSUserDefaults

actual fun loadSaved(key: String): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(key)

actual fun save(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
}
