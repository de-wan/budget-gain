package co.ke.foxlysoft.budgetgain

import androidx.compose.ui.window.ComposeUIViewController
import co.ke.foxlysoft.budgetgain.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    },
) { App() }


