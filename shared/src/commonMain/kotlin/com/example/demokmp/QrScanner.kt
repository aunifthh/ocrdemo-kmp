package com.example.demokmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView

// Full-screen camera that keeps scanning QR codes one after another
@Composable
fun QrScanner(
    lastMessage: String,          // e.g. "Added 802141782" — shown at the bottom
    onCode: (String) -> Unit,     // called with each QR text
    onClose: () -> Unit           // called when the user taps Done
) {
    var round by remember { mutableStateOf(0) }       // changing this restarts the camera
    var waiting by remember { mutableStateOf(false) } // short pause after each scan

    // The library stops after one code, so wait 1.2 s, then start a fresh scan
    LaunchedEffect(waiting) {
        if (waiting) {
            delay(1200)
            round++
            waiting = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (!waiting) {
            key(round) {
                ScannerView(
                    codeTypes = listOf(BarcodeFormat.FORMAT_QR_CODE),
                    scannerUiOptions = null   // hide the library's own buttons, we use ours
                ) { result ->
                    when (result) {
                        is BarcodeResult.OnSuccess -> {
                            onCode(result.barcode.data.trim())
                            waiting = true
                        }
                        is BarcodeResult.OnFailed -> waiting = true   // try again
                        BarcodeResult.OnCanceled -> onClose()
                    }
                }
            }
        }

        // Bottom bar: last result + Done button
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            Text(
                if (lastMessage.isEmpty()) "Point the camera at a DO QR code" else lastMessage,
                color = Color.White
            )
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Done")
            }
        }
    }
}