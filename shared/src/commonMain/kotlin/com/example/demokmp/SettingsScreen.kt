package com.example.demokmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Same as ocrdemo's activity_settings.xml
@Composable
fun SettingsScreen(secretCode: String, onCopy: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().safeContentPadding()) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
            Text("Settings", fontSize = 22.sp)
        }
        Column(Modifier.padding(16.dp)) {
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
        }
    }
}