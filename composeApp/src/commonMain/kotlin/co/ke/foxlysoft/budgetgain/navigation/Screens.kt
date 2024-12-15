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
    data object SpendScreen : Screens("spendScreen/{id}"){
        fun createRoute(id: Long) = "spendScreen/$id"
    }
    data object CategoryDetailsScreen : Screens("categoryDetailsScreen/{id}"){
        fun createRoute(id: Long) = "categoryDetailsScreen/$id"
    }
}