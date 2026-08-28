package co.ke.foxlysoft.budgetgain

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import co.ke.foxlysoft.budgetgain.ui.AppNavDrawer
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme

@Composable
@Preview
fun App() {
    BudgetGainTheme {
        AppNavDrawer()
    }
}