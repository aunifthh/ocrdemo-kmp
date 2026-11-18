package com.example.demokmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

// Live text camera: DO under "Number/Date", 2-in-a-row check, duplicates ignored (like ocrdemo)
@Composable
fun TextCamera(alreadyHave: List<String>, modifier: Modifier = Modifier, onCode: (String) -> Unit) {
    val confirmer = remember { ScanConfirmer() }
    TextCameraView(modifier) { lines ->
        confirmer.offer(findDoNumber(lines), alreadyHave)?.let { onCode(it) }
    }
}