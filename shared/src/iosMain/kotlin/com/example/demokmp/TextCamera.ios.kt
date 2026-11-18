package com.example.demokmp

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.ImageIO.kCGImagePropertyOrientationRight
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import kotlin.time.TimeSource

// A plain UIView that shows the camera picture and resizes it with the view
@OptIn(ExperimentalForeignApi::class)
class CameraPreviewView(private val previewLayer: AVCaptureVideoPreviewLayer) :
    UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    init { layer.addSublayer(previewLayer) }
    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.setFrame(bounds)
        CATransaction.commit()
    }
}

// Receives every camera frame; reads text at most every 250 ms (same as ocrdemo)
@OptIn(ExperimentalForeignApi::class)
class TextFrameDelegate(private val onLines: (List<TextLine>) -> Unit) :
    NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    private var lastScan = TimeSource.Monotonic.markNow()

    @ObjCSignatureOverride
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        if (lastScan.elapsedNow().inWholeMilliseconds < 250) return
        lastScan = TimeSource.Monotonic.markNow()

        val pixelBuffer = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return
        val request = VNRecognizeTextRequest()
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        request.usesLanguageCorrection = false        // don't "fix" numbers into words

        // Phone held upright → camera frames are rotated "right"
        VNImageRequestHandler(
            cVPixelBuffer = pixelBuffer,
            orientation = kCGImagePropertyOrientationRight,
            options = emptyMap<Any?, Any?>()
        ).performRequests(listOf(request), error = null)

        val lines = (request.results ?: emptyList<Any?>()).mapNotNull { obs ->
            val o = obs as? VNRecognizedTextObservation ?: return@mapNotNull null
            val text = (o.topCandidates(1uL).firstOrNull() as? VNRecognizedText)?.string
                ?: return@mapNotNull null
            o.boundingBox.useContents {
                // Vision measures from the BOTTOM-left (0–1). Flip so bigger top = lower, like Android.
                TextLine(
                    text,
                    origin.x.toFloat(),
                    (1.0 - origin.y - size.height).toFloat(),
                    (origin.x + size.width).toFloat(),
                    (1.0 - origin.y).toFloat()
                )
            }
        }
        dispatch_async(dispatch_get_main_queue()) { onLines(lines) }   // back to the UI thread
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun TextCameraView(modifier: Modifier, onLines: (List<TextLine>) -> Unit) {
    val currentOnLines by rememberUpdatedState(onLines)
    val session = remember { AVCaptureSession() }
    val delegate = remember { TextFrameDelegate { currentOnLines(it) } }
    val previewLayer = remember {
        AVCaptureVideoPreviewLayer(session = session).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }
    val sessionQueue = remember { dispatch_queue_create("camera.session", null) }

    DisposableEffect(Unit) {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) as? AVCaptureDeviceInput }
        if (input != null && session.canAddInput(input)) session.addInput(input)

        val output = AVCaptureVideoDataOutput()
        output.alwaysDiscardsLateVideoFrames = true      // like Android's KEEP_ONLY_LATEST
        output.setSampleBufferDelegate(delegate, dispatch_queue_create("ocr.frames", null))
        if (session.canAddOutput(output)) session.addOutput(output)

        dispatch_async(sessionQueue) { session.startRunning() }
        onDispose { dispatch_async(sessionQueue) { session.stopRunning() } }
    }

    UIKitView(factory = { CameraPreviewView(previewLayer) }, modifier = modifier)
}