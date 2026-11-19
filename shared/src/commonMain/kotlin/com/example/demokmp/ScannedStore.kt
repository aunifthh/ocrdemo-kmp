package com.example.demokmp

private const val KEY_SCANNED_DOS = "scanned_dos"

fun loadScanned(): List<String> {
    val saved = loadSaved(KEY_SCANNED_DOS) ?: return emptyList()
    if (saved.isBlank()) return emptyList()
    return saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

fun saveScanned(list: List<String>) {
    val value = list.joinToString(",")
    save(KEY_SCANNED_DOS, value)
}
