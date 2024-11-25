package co.ke.foxlysoft.budgetgain

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.compose_multiplatform
import co.ke.foxlysoft.budgetgain.navigation.AppNavHost
import co.ke.foxlysoft.budgetgain.ui.AppNavDrawer
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme

@Composable
@Preview
fun App() {
    BudgetGainTheme {
        AppNavDrawer()
    }
}