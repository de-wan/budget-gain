package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.PaginationState
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.dateMillisToString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun UncategorizedMpesaSmsScreen(
    uncategorizedMpesaSmsScreenViewModel: UncategorizedMpesaSmsScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyColumnListState = rememberLazyListState()

    val selectableCategories by uncategorizedMpesaSmsScreenViewModel.selectableCategories.collectAsStateWithLifecycle()

    val smsList = uncategorizedMpesaSmsScreenViewModel.smsList.collectAsStateWithLifecycle().value
    val pagingState = uncategorizedMpesaSmsScreenViewModel.pagingState.collectAsStateWithLifecycle()

    val smsToCategorize = remember { mutableStateOf<MpesaSmsEntity?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var categoryNameErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var categoryNameAutoCompleteExpanded by remember { mutableStateOf(false) }
    var shouldCategorizeSimilarByMerchant by remember { mutableStateOf(true)}
    var submitAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        uncategorizedMpesaSmsScreenViewModel.clearPaging()
        uncategorizedMpesaSmsScreenViewModel.getUncategorizedMpesaSms()
    }

    val shouldPaginate = remember {
        derivedStateOf {
            uncategorizedMpesaSmsScreenViewModel.canPaginate && (
                    lazyColumnListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -5
                    ) >= (lazyColumnListState.layoutInfo.totalItemsCount - 3)
        }
    }

    LaunchedEffect(key1 = shouldPaginate.value) {
        if (shouldPaginate.value && pagingState.value == PaginationState.REQUEST_INACTIVE) {
            uncategorizedMpesaSmsScreenViewModel.getUncategorizedMpesaSms()
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    fun clearErrorStatus() {
        categoryNameErrorStatus = ErrorStatus(false)
    }

    fun isCategorizeFormValid(): Boolean {
        var isValid = true
        if (categoryName.isEmpty()) {
            categoryNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Category Name is required")
            isValid = false
        } else {
            // check if category name in selectable category names
            var isFound = false;
            for (c in selectableCategories) {
                if (c.name == categoryName) {
                    isFound = true
                    break
                }
            }
            if (!isFound) {
                categoryNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Please select category from list")
                isValid = false
            }
        }

        return isValid
    }

    Column (
        modifier = Modifier.padding(8.dp)
    ){
        Text(text = "Uncategorized MPESA sms", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Page: ${uncategorizedMpesaSmsScreenViewModel.page}, Pagestate: ${pagingState.value}, Can paginate: ${uncategorizedMpesaSmsScreenViewModel.canPaginate}")
        Spacer(modifier = Modifier.height(8.dp))
        ElevatedCard (
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            LazyColumn(
                state = lazyColumnListState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(
                    smsList.size,
                    key = { smsList[it].id },
                ) { index ->
                    SmsItem(smsList[index], onCategorize = {
                        smsToCategorize.value = smsList[index]
                        showBottomSheet = true
                    })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        when (pagingState.value) {
            PaginationState.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            PaginationState.REQUEST_INACTIVE -> {
//                Text(text = "Request Inactive")
            }
            PaginationState.PAGINATING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            PaginationState.ERROR -> {
//                Text(text = "Error")
            }
            PaginationState.PAGINATION_EXHAUST -> {
//                Text(text = "Pagination Exhaust")
            }
            PaginationState.EMPTY -> {
//                Text(text = "Empty")
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Categorize Sms", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Category",
                    Value = categoryName,
                    errorStatus = categoryNameErrorStatus,
                    onValueChange = {
                        println("Category name changed to $it")
                        uncategorizedMpesaSmsScreenViewModel.updateCategorySearchQuery(it)
                        categoryNameAutoCompleteExpanded = true

                        categoryName = it
                    },
                    validator = {
                        // TODO: validate if in list
                        categoryNameErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                // Dropdown menu
                Box {
                    if (categoryNameAutoCompleteExpanded && selectableCategories.isNotEmpty()) {
                        Popup(
                            onDismissRequest = { categoryNameAutoCompleteExpanded = false },
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .zIndex(1f),
                            ) {
                                LazyColumn {
                                    items(items = selectableCategories) { category ->
                                        TextButton(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            onClick = {
                                                categoryName = category.name
                                                categoryNameAutoCompleteExpanded = false
                                            },
                                        ){
                                            Text(text = category.name)
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ){
                    var dispName = smsToCategorize.value?.subjectSecondaryIdentifier ?: ""
                    if (dispName.isEmpty()) {
                        dispName = smsToCategorize.value?.subjectPrimaryIdentifier ?: ""
                    }
                    Text(text = "Categorize all transactions by $dispName")
                    Checkbox(checked = shouldCategorizeSimilarByMerchant,
                        modifier = Modifier.size(24.dp),
                        onCheckedChange = {
                        shouldCategorizeSimilarByMerchant = !shouldCategorizeSimilarByMerchant
                    })
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        submitAttempted = true
                        clearErrorStatus()

                        if (!isCategorizeFormValid()) {
                            return@Button
                        }
                        uncategorizedMpesaSmsScreenViewModel.categorizeSms(
                            categoryName,
                            smsToCategorize.value!!,
                            shouldCategorizeSimilarByMerchant,
                            onComplete = {
                                uncategorizedMpesaSmsScreenViewModel.clearPaging()
                                uncategorizedMpesaSmsScreenViewModel.getUncategorizedMpesaSms()
                            },
                            onError = {}
                        )

                        
                }){
                    Text(text = "Categorize Sms")
                }
            }


        }
    }
}

@Composable
fun SmsItem(
    sms: MpesaSmsEntity,
    onCategorize: () -> Unit = {}
) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "#${sms.ref}", style = TextStyle(fontSize = 14.sp))
                    Text(text = dateMillisToString(sms.dateTime), style = TextStyle(fontSize = 12.sp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = sms.smsType.toString(), style = TextStyle(fontSize = 12.sp))
                    Text(text = "KSH ${centsToString(sms.amount)}", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = sms.subjectPrimaryIdentifier, style = TextStyle(fontSize = 12.sp))
                    Text(text = sms.subjectSecondaryIdentifier, style = TextStyle(fontSize = 12.sp))
                }

            }
            // Dropdown menu
            Box {
                IconButton(onClick = {
                    menuExpanded = true
                }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        onCategorize()
                        menuExpanded = false
                    },
                        text = {
                            Text("Categorize")
                        })
                }
            }
        }

    }
}