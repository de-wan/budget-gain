package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
          IconButton(
              onClick = {
                  onOpenDrawer()
              },
          ){
              Icon(
                  imageVector = Icons.Default.Menu,
                  contentDescription = "Menu",
              )
          }
        },
        title = { Text(text = "BudgetGain") },
        actions = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.clickable(onClick = onOpenDrawer)
            )
        }
    )
}