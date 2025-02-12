package co.ke.foxlysoft.budgetgain.shared

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import co.ke.foxlysoft.budgetgain.ContextProvider

actual fun getLastCopiedText(): String? {
    val context = ContextProvider.context;
    val clipboardManager = getSystemService(context, ClipboardManager::class.java)
        ?: return null // Ensure ClipboardManager is available

    return if (clipboardManager.hasPrimaryClip()) {
        val clipData: ClipData? = clipboardManager.primaryClip
        clipData?.getItemAt(0)?.text?.toString()
    } else {
        null
    }
}