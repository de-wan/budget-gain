package co.ke.foxlysoft.budgetgain.navigation

sealed class Screens(
    val route: String,
) {
    data object Home : Screens("home")
    data object AllBudgets : Screens("allBudgets")
    data object CreateBudgetScreen : Screens("createBudgetScreen")
}