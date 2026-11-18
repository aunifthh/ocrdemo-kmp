package com.example.demokmp

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView

// Live QR camera that keeps scanning code after code (sits inside the camera card)
@Composable
fun QrCamera(modifier: Modifier = Modifier, onCode: (String) -> Unit) {
    var round by remember { mutableStateOf(0) }       // changing this restarts the camera
    var waiting by remember { mutableStateOf(false) } // short pause after each scan

    LaunchedEffect(waiting) {
        if (waiting) { delay(1200); round++; waiting = false }
    }

    Box(modifier) {
        if (!waiting) {
            key(round) {
                ScannerView(
                    codeTypes = listOf(BarcodeFormat.FORMAT_QR_CODE),
                    scannerUiOptions = null
                ) { result ->
                    when (result) {
                        is BarcodeResult.OnSuccess -> { onCode(result.barcode.data.trim()); waiting = true }
                        is BarcodeResult.OnFailed -> waiting = true
                        BarcodeResult.OnCanceled -> waiting = true
                    }
                }
            }
        }
    }
}