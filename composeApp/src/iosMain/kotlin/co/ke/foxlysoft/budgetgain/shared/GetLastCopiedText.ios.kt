package co.ke.foxlysoft.budgetgain.shared

import platform.UIKit.UIPasteboard

actual fun getLastCopiedText(): String? {
    return UIPasteboard.generalPasteboard.string
}