package com.example.demokmp

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ScanMode { NONE, QR, TEXT, TYPE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen() {
    val scanned = remember { mutableStateListOf<String>() }
    var input by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val secretCode = remember { getSecretCode() }
    var mode by remember { mutableStateOf(ScanMode.NONE) }       // which camera is on
    var showSettings by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val status = remember { mutableStateMapOf<String, String>() }

    // ONE place for the rules — used by QR, text camera and typing
    fun addDo(raw: String) {
        val n = raw.trim()
        when {
            !isValidDo(n) -> { message = "\"$n\" is not a 9-digit DO"; vibrate(false) }
            n in scanned  -> { message = "$n already scanned"; vibrate(false) }
            else -> {
                scanned.add(n)
                status[n] = "sending…"
                message = "Added $n"
                vibrate(true)
                scope.launch {
                    val ok = sendDo(secretCode, n)
                    status[n] = if (ok) "waiting for PC…" else "not sent ✗"
                    if (!ok) message = "$n not sent — check internet"
                }
            }
        }
    }

    // Every 2 s, ask Firebase what the extension did with our DOs
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            if (scanned.isNotEmpty()) {
                val results = fetchResults(secretCode)
                scanned.forEach { n -> results[n]?.let { status[n] = it } }
            }
        }
    }

    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (showSettings) {
                SettingsScreen(
                    secretCode = secretCode,
                    onCopy = { clipboard.setText(AnnotatedString(secretCode)); message = "Code copied" },
                    onClear = { scanned.clear(); status.clear(); message = "List cleared" },
                    onBack = { showSettings = false }
                )
            } else {
                Column(Modifier.fillMaxSize().safeContentPadding()) {

                    // ---- Top bar: title + settings ----
                    Row(
                        Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DO Scanner", fontSize = 22.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { mode = ScanMode.NONE; showSettings = true }) {
                            Text("⚙", fontSize = 22.sp)
                        }
                    }

                    Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {

                        // ---- Toggle: Scan QR | Scan Text | Type (tap again to close) ----
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            listOf(
                                ScanMode.QR to "Scan QR",
                                ScanMode.TEXT to "Scan Text",
                                ScanMode.TYPE to "Type"
                            ).forEachIndexed { index, (m, label) ->
                                SegmentedButton(
                                    selected = mode == m,
                                    onClick = { message = ""; mode = if (mode == m) ScanMode.NONE else m },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                ) { Text(label, maxLines = 1) }
                            }
                        }

                        // ---- Camera card ----
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().weight(3f).padding(top = 16.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                when (mode) {
                                    ScanMode.QR -> QrCamera(Modifier.fillMaxSize()) { addDo(it) }
                                    ScanMode.TEXT -> TextCamera(scanned, Modifier.fillMaxSize()) { addDo(it) }
                                    ScanMode.TYPE -> Column(
                                        Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Enter DO number", fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = input,
                                            onValueChange = { input = it.filter { c -> c.isDigit() }.take(9) },  // digits only, max 9
                                            placeholder = { Text("e.g. 802141782") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                        )
                                        Button(
                                            onClick = { addDo(input); input = "" },
                                            enabled = input.length == 9,                       // only when 9 digits typed
                                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                        ) { Text("Add") }
                                    }
                                    ScanMode.NONE -> Text("Choose Scan QR, Scan Text or Type",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (message.isNotEmpty()) {
                            Text(message, modifier = Modifier.padding(top = 8.dp))
                        }

                        // ---- Results card (with manual entry + status) ----
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().weight(2f).padding(top = 12.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("SCANNED DO NUMBERS (${scanned.size})",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                LazyColumn(Modifier.weight(1f).padding(top = 8.dp)) {
                                    items(scanned) { doNo ->
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Text(doNo, fontSize = 18.sp, modifier = Modifier.weight(1f))
                                            Text(status[doNo] ?: "",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}