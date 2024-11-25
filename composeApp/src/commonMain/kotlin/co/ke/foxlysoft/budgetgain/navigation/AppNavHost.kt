package co.ke.foxlysoft.budgetgain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import co.ke.foxlysoft.budgetgain.ui.AllBudgetsScreen
import co.ke.foxlysoft.budgetgain.ui.CreateBudgetScreen
import co.ke.foxlysoft.budgetgain.ui.HomeScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier, navHostController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navHostController,
        modifier = modifier,
        startDestination = Screens.Home.route
    ) {
        animatedComposable(
            Screens.Home.route
        ) {
            HomeScreen(
                onNavigate = { route ->
                navHostController.navigate(route)
            },)

        }
        animatedComposable(
            Screens.AllBudgets.route
        ) {
            AllBudgetsScreen(
                onNavigate = { route ->
                navHostController.navigate(route)
            },)
        }
        animatedComposable(
            Screens.CreateBudgetScreen.route
        ) {
            CreateBudgetScreen(
                onNavigate = {route ->
                    navHostController.navigate(route)
                }
            )
        }
    }
}