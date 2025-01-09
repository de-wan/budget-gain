package co.ke.foxlysoft.budgetgain.shared

import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.endEditing

actual class ToastManager {
    actual fun showToast(message: String) {
        // Dismiss the keyboard if it's open
        dismissKeyboard()

        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )
        // Dismiss the toast after a delay
        val delay = 1.5 // seconds
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            alert,
            animated = true,
            completion = {
                alert.dismissViewControllerAnimated(true, null)
            }
        )
    }

    //Function to dismiss the keyboard
    private fun dismissKeyboard() {
        val keyWindow = UIApplication.sharedApplication.keyWindow
        keyWindow?.endEditing(true)
    }
}