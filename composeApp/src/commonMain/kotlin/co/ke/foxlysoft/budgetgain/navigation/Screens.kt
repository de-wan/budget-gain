package co.ke.foxlysoft.budgetgain.navigation

sealed class Screens(
    val route: String,
) {
    data object Back : Screens("back")
    data object Home : Screens("home")
    data object AllBudgets : Screens("allBudgets")
    data object CreateBudgetScreen : Screens("createBudgetScreen")
    data object AddCategoryScreen : Screens("addCategoryScreen/{id}"){
        fun createRoute(id: Long) = "addCategoryScreen/$id"
    }
}