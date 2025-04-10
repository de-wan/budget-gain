package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.ke.foxlysoft.budgetgain.navigation.AppNavHost
import co.ke.foxlysoft.budgetgain.navigation.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavDrawer() {
    val navigationController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember{ SnackbarHostState() }

    ModalNavigationDrawer(drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.requiredWidth(250.dp),
                drawerState = drawerState,
            ) {
                AppNavDrawerContent(
                    navHostController = navigationController,
                    onCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
           topBar = {
               AppTopBar(onOpenDrawer = {
                   scope.launch {
                       drawerState.apply {
                           if (isClosed) open() else close()
                       }
                   }
               })
           }
        ) { innerPadding ->
            AppNavHost(
                navHostController = navigationController,
                modifier = Modifier.padding(innerPadding),
                onOpenSnackbar = {msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                },
                onOpenConfirmSnackbar = { msg, actionLabel, onConfirm ->
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = msg,
                            actionLabel = actionLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onConfirm()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AppNavDrawerContent(modifier: Modifier = Modifier, onCloseDrawer: () -> Unit, navHostController: NavHostController) {
    Text(
        text = "Budget Gain",
        fontSize = 24.sp,
        modifier = Modifier.padding(16.dp)
    )
    HorizontalDivider()
//    Button(onClick = { /*TODO*/ }) {
//        Text(text = "Create Budget")
//    }
    NavigationDrawerItem(
        label = {
            Text(text = "Home", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        },
        selected = false,
        onClick = {
            onCloseDrawer()
            navHostController.navigate(Screens.Home.route)
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    NavigationDrawerItem(
        label = {
            Text(text = "All Budgets", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        },
        selected = false,
        onClick = {
            onCloseDrawer()
            navHostController.navigate(Screens.AllBudgets.route)
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    NavigationDrawerItem(
        label = {
            Text(text = "Uncategorized Mpesa SMS", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        },
        selected = false,
        onClick = {
            onCloseDrawer()
            navHostController.navigate(Screens.UncategorizedMpesaSmsScreen.route)
        }
    )
//    Spacer(modifier = Modifier.height(8.dp))
//    NavigationDrawerItem(
//        label = {
//            Text(text = "Recurring Transactions", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
//        },
//        selected = false,
//        onClick = { /*TODO*/ }
//    )
//    Spacer(modifier = Modifier.height(8.dp))
//    NavigationDrawerItem(
//        label = {
//            Text(text = "My Transactions", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
//        },
//        selected = false,
//        onClick = { /*TODO*/ }
//    )
//    Spacer(modifier = Modifier.height(8.dp))
//    HorizontalDivider()
//    Text(
//        text = "Manage Accounts",
//        fontSize = 24.sp,
//        modifier = Modifier.padding(16.dp)
//    )
//    Spacer(modifier = Modifier.height(8.dp))
//    NavigationDrawerItem(
//        label = {
//            Text(text = "My Accounts", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
//        },
//        selected = false,
//        onClick = { /*TODO*/ }
//    )
//    Spacer(modifier = Modifier.height(8.dp))
//    NavigationDrawerItem(
//        label = {
//            Text(text = "Payers", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
//        },
//        selected = false,
//        onClick = { /*TODO*/ }
//    )
    Spacer(modifier = Modifier.height(8.dp))
    NavigationDrawerItem(
        label = {
            Text(text = "Merchants", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        },
        selected = false,
        onClick = {
            onCloseDrawer()
            navHostController.navigate(Screens.Merchants.route)
        }
    )
}