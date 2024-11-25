package co.ke.foxlysoft.budgetgain

import android.content.Context

object ContextProvider {
    private lateinit var _context: Context

    val context: Context
        get() = _context

    fun setContext(context: Context) {
        _context = context
    }
}