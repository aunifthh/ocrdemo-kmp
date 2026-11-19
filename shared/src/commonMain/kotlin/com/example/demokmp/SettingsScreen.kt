package com.example.demokmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Same as ocrdemo's activity_settings.xml (secret code + clear DO list)
@Composable
fun SettingsScreen(
    secretCode: String,
    onCopy: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().safeContentPadding()) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
            Text("Settings", fontSize = 22.sp)
        }
        Column(Modifier.padding(16.dp)) {
            // ---- Secret code ----
            Text("SECRET CODE", color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Paste this code into the Chrome extension. Treat it like a password.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(secretCode, fontFamily = FontFamily.Monospace, fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp))
            }
            FilledTonalButton(onClick = onCopy, modifier = Modifier.padding(top = 12.dp)) {
                Text("Copy")
            }

            // ---- Scanned list (same as ocrdemo) ----
            Text("SCANNED LIST", color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp))
            Text("Clears the DO numbers shown on the main screen so they can be scanned again.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp))
            OutlinedButton(
                onClick = { confirmClear = true },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Clear DO list", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Confirmation popup (same wording as ocrdemo's)
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear DO list?") },
            text = {
                Text("This removes all DO numbers from the main screen. Duplicates of numbers you " +
                        "already scanned will no longer be detected, and this can't be undone.")
            },
            confirmButton = {
                TextButton(onClick = { onClear(); confirmClear = false }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}