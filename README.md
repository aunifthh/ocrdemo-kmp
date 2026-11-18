# OcrDemo – DO Scanner (KMP: Android + iOS)

Scans Daikin DO numbers from picking lists and sends them to the **ocr-extension** (Chrome), which auto-selects them in the TMS.
Kotlin Multiplatform + Compose Multiplatform version.

## Features

| Feature | Status |
|---|---|
| Scan QR (main method) | ✅ |
| Scan Text – live camera OCR, reads the number under "Number/Date" | ✅ |
| Type – manual DO entry (digits only, 9 digits) | ✅ |
| 9-digit check + duplicate check | ✅ |
| Send to Firebase → extension auto-ticks in TMS | ✅ |
| Status per DO: sending / waiting for PC / ticked ✓ / not found ✗ | ✅ |
| Vibration (short = accepted, long = duplicate/invalid) | ✅ |
| Settings page with secret code + Copy | ✅ |
| Dark mode | ✅ |
| Tested on Android device | ⏳ |

## How it works

```
Phone app ──► Firebase (lists/<code>/pending) ◄── ocr-extension ──► TMS page (search + tick DO)
    ▲                                                   │
    └──────── reads lists/<code>/done | failed ◄────────┘
```

- Same Firebase database and format as the native app, so the extension works with either app.
- Pairing: copy the secret code from **Settings (⚙)** → paste it into the extension side panel.

## Project structure

| Folder | What's inside |
|---|---|
| `shared/src/commonMain` | Shared code for both phones: screens, DO rules, Firebase, OCR logic |
| `shared/src/androidMain` | Android only: CameraX + ML Kit, storage, vibration |
| `shared/src/iosMain` | iPhone only: AVFoundation + Vision, storage, haptics |
| `androidApp` | Android app entry point |
| `iosApp` | Xcode project (iPhone app entry point) |

## Run

- **Android:** open this folder in Android Studio → select `androidApp` → Run.
- **iPhone:** open `iosApp/iosApp.xcodeproj` in Xcode (or select `iosApp` in Android Studio) → choose a device → Run.
  - Set your own **Team** in Signing & Capabilities.
  - Camera features need a real device (the simulator has no camera).

## Libraries

KScan (QR), Ktor (HTTP), kotlinx-serialization-json, ML Kit Text Recognition + CameraX (Android), Apple Vision + AVFoundation (iOS).