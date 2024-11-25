package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.ke.foxlysoft.budgetgain.navigation.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val firstTime = homeScreenViewModel.firstTime.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(text = "First Time: Key: ${firstTime.settingKey} Value: ${firstTime.value}")
        Button(onClick = { CoroutineScope(Dispatchers.IO).launch {
            homeScreenViewModel.setToFalse()
        } }) {
            Text(text = "Set to false")
        }
        Button(onClick = {
            onNavigate(Screens.CreateBudgetScreen.route)
        }){
            Text(text = "Create Budget")
        }
    }
}