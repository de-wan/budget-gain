package co.ke.foxlysoft.budgetgain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.ke.foxlysoft.budgetgain.ui.AddCategoryScreen
import co.ke.foxlysoft.budgetgain.ui.AllBudgetsScreen
import co.ke.foxlysoft.budgetgain.ui.CategoryDetailsScreen
import co.ke.foxlysoft.budgetgain.ui.CreateBudgetScreen
import co.ke.foxlysoft.budgetgain.ui.HomeScreen
import co.ke.foxlysoft.budgetgain.ui.MerchantsScreen
import co.ke.foxlysoft.budgetgain.ui.SpendScreen
import co.ke.foxlysoft.budgetgain.ui.UncategorizedMpesaSmsScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier, navHostController: NavHostController = rememberNavController(), onOpenSnackbar: (String)->Unit = {}) {
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
            Screens.CreateBudgetScreen.route,
        ) {
            CreateBudgetScreen(
                onNavigate = {route ->
                    navHostController.navigate(route)
                }
            )
        }
        animatedComposable(
            Screens.AddCategoryScreen.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L
            AddCategoryScreen(
                onNavigateBack = {
                        navHostController.popBackStack()
                },
                id = id
            )
        }
        animatedComposable(
            Screens.SpendScreen.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L
            SpendScreen(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                categoryId = id,
                onOpenSnackbar = onOpenSnackbar
            )
        }
        animatedComposable(
            Screens.CategoryDetailsScreen.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { navBackStackEntry ->
            val id = navBackStackEntry.arguments?.getLong("id") ?: 0L
            CategoryDetailsScreen(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                categoryId = id
            )
        }
        animatedComposable(
            Screens.Merchants.route
        ) {
            MerchantsScreen(
                onNavigate = { route ->
                    navHostController.navigate(route)
                },)
        }
        animatedComposable(
            Screens.UncategorizedMpesaSmsScreen.route,
        ) {
            UncategorizedMpesaSmsScreen(
                onNavigate = {route ->
                    navHostController.navigate(route)
                }
            )
        }
    }
}