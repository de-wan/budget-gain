package co.ke.foxlysoft.budgetgain

import android.content.Context

object ContextProvider {
    private var applicationContext: Context? = null

    val context
        get() =
            applicationContext
                ?: error("Android context has not been set. Please call setContext in your Application's onCreate.")

    fun setContext(context: Context) {
        applicationContext = context.applicationContext
    }
}