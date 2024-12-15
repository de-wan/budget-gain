package co.ke.foxlysoft.budgetgain.shared

import android.content.Context
import android.widget.Toast

actual class ToastManager(private val context: Context) {
    actual fun showToast(message: String) {
        // Get the context from somewhere appropriate, like an Application or Activity
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}