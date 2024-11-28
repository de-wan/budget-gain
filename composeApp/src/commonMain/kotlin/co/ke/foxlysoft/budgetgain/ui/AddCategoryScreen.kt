package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AddCategoryScreen(onNavigate: (String) -> Unit,id: Long) {
    Text(text="Add Category $id")
}