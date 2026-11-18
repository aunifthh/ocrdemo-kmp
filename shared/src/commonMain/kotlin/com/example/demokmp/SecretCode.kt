package com.example.demokmp

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// PROMISE: each phone must provide these two (no body here)
expect fun loadSaved(key: String): String?
expect fun save(key: String, value: String)

// Shared logic, written once — same idea as ocrdemo secretCode
@OptIn(ExperimentalUuidApi::class)
fun getSecretCode(): String {
    val saved = loadSaved("code")
    if (saved != null) return saved            // already created before → reuse

    val newCode = Uuid.random().toHexString()  // 32 random characters, no dashes
    save("code", newCode)                      // keep it for next time
    return newCode
}