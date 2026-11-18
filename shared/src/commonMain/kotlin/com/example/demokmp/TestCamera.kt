package com.example.demokmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Each phone shows its own camera preview and reads text from frames (max every 250 ms)
@Composable
expect fun TextCameraView(modifier: Modifier, onLines: (List<TextLine>) -> Unit)