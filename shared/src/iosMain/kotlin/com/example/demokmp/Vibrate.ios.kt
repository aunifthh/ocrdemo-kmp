package com.example.demokmp

import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual fun vibrate(ok: Boolean) {
    UINotificationFeedbackGenerator().notificationOccurred(
        if (ok) UINotificationFeedbackType.UINotificationFeedbackTypeSuccess
        else UINotificationFeedbackType.UINotificationFeedbackTypeError
    )
}