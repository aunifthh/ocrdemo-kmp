package com.example.demokmp

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Same database as ocr demo main app and extension
const val DB_URL = "https://ocr-star-b3312-default-rtdb.asia-southeast1.firebasedatabase.app"

private val client = HttpClient()   // Ktor picks OkHttp on Android, Darwin on iPhone

// Same result as ocr demo main  dbRef.push().setValue(n)
// → lists/<code>/pending/<auto-key> = "802141782"
suspend fun sendDo(code: String, doNo: String): Boolean = try {
    val res = client.post("$DB_URL/lists/$code/pending.json") {
        setBody(TextContent("\"$doNo\"", ContentType.Application.Json))
    }
    res.status.isSuccess()   // true if Firebase accepted it
} catch (e: Exception) {
    false                    // no internet, timeout, etc.
}

// Reads what the extension wrote back, as  DO number → status text
//   failed/<key> = {"number":"802141782","reason":"no match"}
//   done/<key>   = "802141782"
suspend fun fetchResults(code: String): Map<String, String> {
    val out = mutableMapOf<String, String>()
    try {
        // failed first…
        val failed = Json.parseToJsonElement(
            client.get("$DB_URL/lists/$code/failed.json").bodyAsText()
        )
        if (failed is JsonObject) {
            failed.values.forEach { item ->
                val obj = item.jsonObject
                val n = obj["number"]?.jsonPrimitive?.content ?: return@forEach
                val reason = obj["reason"]?.jsonPrimitive?.content ?: "?"
                out[n] = "not found ✗ ($reason)"
            }
        }
        // …then done, so a later success wins over an earlier fail
        val done = Json.parseToJsonElement(
            client.get("$DB_URL/lists/$code/done.json").bodyAsText()
        )
        if (done is JsonObject) {
            done.values.forEach { out[it.jsonPrimitive.content] = "ticked ✓" }
        }
    } catch (e: Exception) {
        // no internet → just try again next round
    }
    return out
}