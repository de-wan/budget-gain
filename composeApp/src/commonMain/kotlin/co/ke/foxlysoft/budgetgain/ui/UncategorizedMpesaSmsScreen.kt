package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.ui.components.BGPaginatedList
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.ui.components.BGainPaginationController
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.MpesaSmsTypes
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.dateMillisToString
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
fun UncategorizedMpesaSmsScreen(
    uncategorizedMpesaSmsScreenViewModel: UncategorizedMpesaSmsScreenViewModel = koinViewModel(),
    onNavigate: (String) -> Unit,
    onOpenSnackbar: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val selectableCategories by uncategorizedMpesaSmsScreenViewModel.selectableCategories.collectAsStateWithLifecycle()
    val smsToCategorize = remember { mutableStateOf<MpesaSmsEntity?>(null) }
    val selectedSmsIds = remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val search by uncategorizedMpesaSmsScreenViewModel.search.collectAsStateWithLifecycle()

    val paginationController = remember { BGainPaginationController() }

    UncategorizedMpesaSmsContent(
        onGetItems = uncategorizedMpesaSmsScreenViewModel::getUncategorizedMpesaSms,
        onCategorize = {
            smsToCategorize.value = it
            showBottomSheet = true
        },
        onIgnoreSms = {sms ->
            coroutineScope.launch {
                uncategorizedMpesaSmsScreenViewModel.ignoreSingleSms(sms)
                paginationController.refreshAllPages()
            }
            onOpenSnackbar("SMS successfully ignored")
        },
        paginationController = paginationController,
        search = search,
        onSearchChange = uncategorizedMpesaSmsScreenViewModel::onSearchChange,
        selectedSmsIds = selectedSmsIds.value,
        isMultiSelectMode = isMultiSelectMode,
        onEnterMultiSelectMode = { smsId: Long ->
            selectedSmsIds.value += smsId
            isMultiSelectMode = true
        },
        onToggleSelection = { smsId: Long ->
            if (selectedSmsIds.value.contains(smsId)) {
                selectedSmsIds.value -= smsId
            } else {
                selectedSmsIds.value += smsId
            }
        },
        onCategorizeSelected = {
            showBottomSheet = true
        },
        onClearSelection = {
            isMultiSelectMode = false
            selectedSmsIds.value = emptySet()
        },
        onOpenSnackbar = onOpenSnackbar
    )

    if (showBottomSheet) {
        val isBulkMode = isMultiSelectMode && selectedSmsIds.value.isNotEmpty()
        val merchantLabel = if (isBulkMode) {
            "${selectedSmsIds.value.size} selected items"
        } else {
            smsToCategorize.value?.let { sms ->
                sms.subjectSecondaryIdentifier.ifBlank { sms.subjectPrimaryIdentifier }
            }.orEmpty()
        }
        CategorizationBottomSheet(
            itemCount = if (isBulkMode) selectedSmsIds.value.size else 1,
            merchantLabel = merchantLabel,
            selectableCategories = selectableCategories,
            onCategorySearch = uncategorizedMpesaSmsScreenViewModel::updateCategorySearchQuery,
            onDismiss = { showBottomSheet = false },
            onSubmit = { selectedCategory, categorizeSimilar ->
                if (isBulkMode) {
                    uncategorizedMpesaSmsScreenViewModel.categorizeBulkSms(
                        selectedCategory,
                        selectedSmsIds.value,
                        categorizeSimilar
                    )
                } else {
                    uncategorizedMpesaSmsScreenViewModel.categorizeSms(
                        selectedCategory,
                        smsToCategorize.value!!,
                        categorizeSimilar
                    )
                }
            },
            onSuccess = {
                paginationController.refreshAllPages()
                onOpenSnackbar("SMS successfully categorized")
                showBottomSheet = false
                smsToCategorize.value = null
                selectedSmsIds.value = emptySet()
                isMultiSelectMode = false
            },
            onError = { message -> onOpenSnackbar("Error categorizing SMS: $message") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizationBottomSheet(
    itemCount: Int,
    merchantLabel: String,
    selectableCategories: List<CategoryEntity>,
    onCategorySearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: suspend (categoryName: String, categorizeSimilar: Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var categoryName by remember { mutableStateOf("") }
    var categoryError by remember { mutableStateOf(ErrorStatus(false)) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var categorizeSimilar by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { onCategorySearch("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = if (itemCount > 1) "Categorize $itemCount transactions" else "Categorize transaction",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            BGainOutlineField(
                modifier = Modifier.fillMaxWidth(),
                labelStr = "Category",
                Value = categoryName,
                errorStatus = categoryError,
                trailingIcon = {
                    IconButton(onClick = {
                        onCategorySearch(categoryName)
                        isCategoryMenuExpanded = !isCategoryMenuExpanded
                    }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Show categories")
                    }
                },
                onValueChange = { value ->
                    categoryName = value
                    categoryError = ErrorStatus(false)
                    onCategorySearch(value)
                    isCategoryMenuExpanded = true
                },
                validator = {}
            )
            Box {
                if (isCategoryMenuExpanded && selectableCategories.isNotEmpty()) {
                    Popup(onDismissRequest = { isCategoryMenuExpanded = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .padding(horizontal = 32.dp)
                                .zIndex(1f)
                        ) {
                            LazyColumn {
                                items(selectableCategories) { category ->
                                    TextButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            categoryName = category.name
                                            categoryError = ErrorStatus(false)
                                            isCategoryMenuExpanded = false
                                        }
                                    ) { Text(category.name) }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (itemCount > 1) {
                        "Also categorize similar transactions for the selected merchants"
                    } else {
                        "Also categorize similar transactions by $merchantLabel"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Checkbox(
                    checked = categorizeSimilar,
                    onCheckedChange = { categorizeSimilar = it }
                )
            }
            submissionError?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                onClick = {
                    if (selectableCategories.none { it.name == categoryName }) {
                        categoryError = ErrorStatus(true, "Select a category from the list")
                        return@Button
                    }
                    coroutineScope.launch {
                        isSubmitting = true
                        submissionError = null
                        try {
                            onSubmit(categoryName, categorizeSimilar)
                            onSuccess()
                        } catch (error: Exception) {
                            val message = error.message ?: "Unable to categorize transactions"
                            submissionError = message
                            onError(message)
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (itemCount > 1) "Categorize $itemCount transactions" else "Categorize transaction")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UncategorizedMpesaSmsContent(
    onGetItems: suspend (Int, Int) -> List<MpesaSmsEntity>,
    paginationController: BGainPaginationController = remember { BGainPaginationController() },
    onIgnoreSms: (MpesaSmsEntity) -> Unit = {},
    onCategorize: (MpesaSmsEntity) -> Unit = {},
    onEnterMultiSelectMode: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit,
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    selectedSmsIds: Set<Long> = emptySet(),
    isMultiSelectMode: Boolean = false,
    onClearSelection: () -> Unit = {},
    onCategorizeSelected: () -> Unit = {},
    onOpenSnackbar: (String) -> Unit = {}
) {
    Column {
        Text(text = "Uncategorized MPESA sms", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Show either search bar or bulk actions bar
        if (isMultiSelectMode) {
            // Bulk actions bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedSmsIds.size} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IconButton(
                        onClick = { onClearSelection() },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear selection"
                        )
                    }
                }
                Button(
                    onClick = onCategorizeSelected,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Categorize")
                }
            }
        } else {
            // Search bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BGainOutlineField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(0.dp),
                    labelStr = "Search",
                    Value = search,
                    errorStatus = ErrorStatus(isError = false),
                    onValueChange = {onSearchChange(it)},
                    validator = {},
                )
                IconButton(onClick = {
                    paginationController.refresh()
                }){
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        BGPaginatedList(
            onGetKey = { it.id },
            onGetItem = { sms ->
                SmsItem(
                    sms = sms,
                    isSelected = selectedSmsIds.contains(sms.id),
                    isMultiSelectMode = isMultiSelectMode,
                    onLongPress = { onEnterMultiSelectMode(sms.id) },
                    onToggleSelection = { onToggleSelection(sms.id) },
                    onIgnoreSms = onIgnoreSms,
                    onCategorize = onCategorize
                )
            },
            onGetItems = { limit, offset ->
                onGetItems(limit, offset)
            },
            controller = paginationController,
        )
    }
}

@Composable
fun SmsItem(
    sms: MpesaSmsEntity,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSelection: () -> Unit = {},
    onIgnoreSms: (MpesaSmsEntity) -> Unit = {},
    onCategorize: (MpesaSmsEntity) -> Unit = {}
) {
    // State to track the expanded state of the menu
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = if (isMultiSelectMode) {
            Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = { onToggleSelection() }
                    )
                }
        } else {
            Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show checkbox in multi-select mode
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

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
            // Show menu only when not in multi-select mode
            if (!isMultiSelectMode) {
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
                            onCategorize(sms)
                            menuExpanded = false
                        },
                            text = {
                                Text("Categorize")
                            })
                        DropdownMenuItem(onClick = {
                            onIgnoreSms(sms)
                            menuExpanded = false
                        },
                            text = {
                                Text("Ignore")
                            })
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun UncategorizedMpesaSmsContentPreview() {
    val currentTime = 1707300000000L // Sample timestamp
    val sampleSms = listOf(
        MpesaSmsEntity(
            id = 1,
            transactionId = 1001,
            smsType = MpesaSmsTypes.SEND_MONEY,
            ref = "MMP1234567",
            amount = 25_000,
            dateTime = currentTime,
            subjectPrimaryIdentifierType = "phone",
            subjectPrimaryIdentifier = "+254712345678",
            subjectSecondaryIdentifierType = "name",
            subjectSecondaryIdentifier = "John Doe",
            cost = 10,
            balance = 500_000
        ),
        MpesaSmsEntity(
            id = 2,
            transactionId = 1002,
            smsType = MpesaSmsTypes.RECEIVE_MONEY,
            ref = "MMP1234568",
            amount = 50_000,
            dateTime = currentTime,
            subjectPrimaryIdentifierType = "phone",
            subjectPrimaryIdentifier = "+254787654321",
            subjectSecondaryIdentifierType = "name",
            subjectSecondaryIdentifier = "Jane Smith",
            cost = 0,
            balance = 550_000
        ),
        MpesaSmsEntity(
            id = 3,
            transactionId = 1003,
            smsType = MpesaSmsTypes.PAYBILL,
            ref = "MMP1234569",
            amount = 15_000,
            dateTime = currentTime,
            subjectPrimaryIdentifierType = "paybill",
            subjectPrimaryIdentifier = "100100",
            subjectSecondaryIdentifierType = "account",
            subjectSecondaryIdentifier = "ACC123",
            cost = 50,
            balance = 535_000
        )
    )

    BudgetGainTheme {
        Surface {
            UncategorizedMpesaSmsContent(
                onGetItems = { _, _ -> sampleSms },
                onCategorize = {},
                onEnterMultiSelectMode = {},
                onToggleSelection = {},
                search = "",
                onSearchChange = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UncategorizedMpesaSmsContentMultiselectPreview() {
    val currentTime = 1707300000000L // Sample timestamp
    val sampleSms = listOf(
        MpesaSmsEntity(
            id = 1,
            transactionId = 1001,
            smsType = MpesaSmsTypes.SEND_MONEY,
            ref = "MMP1234567",
            amount = 25_000,
            dateTime = currentTime,
            subjectPrimaryIdentifierType = "phone",
            subjectPrimaryIdentifier = "+254712345678",
            subjectSecondaryIdentifierType = "name",
            subjectSecondaryIdentifier = "John Doe",
            cost = 10,
            balance = 500_000
        ),
        MpesaSmsEntity(
            id = 2,
            transactionId = 1002,
            smsType = MpesaSmsTypes.RECEIVE_MONEY,
            ref = "MMP1234568",
            amount = 50_000,
            dateTime = currentTime,
            subjectPrimaryIdentifierType = "phone",
            subjectPrimaryIdentifier = "+254787654321",
            subjectSecondaryIdentifierType = "name",
            subjectSecondaryIdentifier = "Jane Smith",
            cost = 0,
            balance = 550_000
        ),
        MpesaSmsEntity(
            id = 3,
            transactionId = 1003,
            smsType = MpesaSmsTypes.PAYBILL,
            ref = "MMP1234569",
            amount = 15_000,
            dateTime = currentTime,
            subjectPrimaryIdentifierType = "paybill",
            subjectPrimaryIdentifier = "100100",
            subjectSecondaryIdentifierType = "account",
            subjectSecondaryIdentifier = "ACC123",
            cost = 50,
            balance = 535_000
        )
    )

    val selectedSmsIds = remember { mutableStateOf<Set<Long>>(emptySet()) }
    selectedSmsIds.value += 3


    BudgetGainTheme {
        Surface {
            UncategorizedMpesaSmsContent(
                selectedSmsIds = selectedSmsIds.value,
                onGetItems = { _, _ -> sampleSms },
                onCategorize = {},
                onEnterMultiSelectMode = {},
                onToggleSelection = {},
                isMultiSelectMode = true,
                search = "",
                onSearchChange = {},
            )
        }
    }
}
