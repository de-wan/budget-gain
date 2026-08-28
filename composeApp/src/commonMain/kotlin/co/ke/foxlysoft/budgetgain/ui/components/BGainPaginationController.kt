package co.ke.foxlysoft.budgetgain.ui.components

class BGainPaginationController {
    private var refresh: (() -> Unit)? = null
    private var refreshAllPages: (() -> Unit)? = null

    internal fun setRefreshCallback(callback: () -> Unit) {
        refresh = callback
    }

    internal fun setRefreshAllPagesCallback(callback: () -> Unit) {
        refreshAllPages = callback
    }

    fun refresh() {
        refresh?.invoke()
    }

    fun refreshAllPages() {
        refreshAllPages?.invoke()
    }
}