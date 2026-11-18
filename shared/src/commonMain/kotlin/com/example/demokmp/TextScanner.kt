package com.example.demokmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Full-screen live camera that reads the DO under "Number/Date"
@Composable
fun TextScanner(
    alreadyHave: List<String>,   // DOs already in the list (ignored silently, like ocrdemo)
    lastMessage: String,
    onCode: (String) -> Unit,
    onClose: () -> Unit
) {
    val confirmer = remember { ScanConfirmer() }   // 2-in-a-row check from Lesson 1

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        TextCameraView(Modifier.fillMaxSize()) { lines ->
            val candidate = findDoNumber(lines)
            confirmer.offer(candidate, alreadyHave)?.let { onCode(it) }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            Text(
                if (lastMessage.isEmpty()) "Point at the \"Number/Date\" on the picking list" else lastMessage,
                color = Color.White
            )
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Done")
            }
        }
    }
}