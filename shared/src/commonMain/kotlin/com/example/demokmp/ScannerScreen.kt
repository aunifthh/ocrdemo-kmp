package com.example.demokmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun ScannerScreen() {
    val scanned = remember { mutableStateListOf<String>() }
    var input by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val secretCode = remember { getSecretCode() }
    var showCode by remember { mutableStateOf(false) }
    var scanningQr by remember { mutableStateOf(false) }   // NEW: is the camera open?
    var scanningText by remember { mutableStateOf(false) }   // is the text camera open?
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()                            // lets us run "suspend" work
    val status =
        remember { mutableStateMapOf<String, String>() }  // DO → "sending…" / "sent ✓" / "failed ✗"

    // ONE place for the rules — used by typing AND by QR
    fun addDo(raw: String) {
        val n = raw.trim()
        when {
            !isValidDo(n) -> {
                message = "\"$n\" is not a 9-digit DO"; vibrate(false)
            }

            n in scanned -> {
                message = "$n already scanned"; vibrate(false)
            }

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
            if (scanningQr) {
                QrScanner(
                    lastMessage = message,
                    onCode = { addDo(it) },
                    onClose = { scanningQr = false }
                )
            } else if (scanningText) {
                TextScanner(
                    alreadyHave = scanned,
                    lastMessage = message,
                    onCode = { addDo(it) },
                    onClose = { scanningText = false }
                )
            } else {
                // Main screen
                Column(
                    modifier = Modifier.fillMaxSize().safeContentPadding().padding(16.dp)
                ) {
                    Text("DO Scanner", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Scan a picking list to send its DO number")
                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { message = ""; scanningQr = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Scan QR") }

                        FilledTonalButton(
                            onClick = { message = ""; scanningText = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Scan Text") }

                        OutlinedButton(
                            onClick = { showCode = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Code") }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("Type a DO number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { addDo(input); input = "" },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("Add") }

                    Text(message, modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "SCANNED DO NUMBERS (${scanned.size})",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(scanned) { doNo ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text(doNo, fontSize = 18.sp, modifier = Modifier.weight(1f))
                                Text(status[doNo] ?: "")
                            }
                        }
                    }
                }
            }

            if (showCode) {
                AlertDialog(
                    onDismissRequest = { showCode = false },
                    title = { Text("Your secret code") },
                    text = { Text(secretCode) },
                    confirmButton = {
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(secretCode))
                            message = "Code copied"
                            showCode = false
                        }) { Text("Copy") }
                    },
                    dismissButton = { TextButton(onClick = { showCode = false }) { Text("Close") } }
                )
            }
        }
    }
}