package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.calculator_variant_outline
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.CommonCategoryDefinition
import co.ke.foxlysoft.budgetgain.database.CommonCategoryCatalog
import co.ke.foxlysoft.budgetgain.database.DEFAULT_CATEGORY_DARK_COLOR_ARGB
import co.ke.foxlysoft.budgetgain.database.DEFAULT_CATEGORY_ICON_KEY
import co.ke.foxlysoft.budgetgain.database.DEFAULT_CATEGORY_LIGHT_COLOR_ARGB
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.calc.CalculatorDialog
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.isValidAmount
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AddCategoryScreen(
    id: Long,
    addCategoryScreenViewModel: AddCategoryScreenViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    var submitAttempted by remember { mutableStateOf(false) }

    val currentBudget = addCategoryScreenViewModel.currentBudget.collectAsState().value
    var categoryName by remember { mutableStateOf("") }
    var selectedCommonCategory by remember { mutableStateOf<CommonCategoryDefinition?>(null) }
    var categoryNameErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var categoryAmount by remember { mutableStateOf("") }
    var customSubCategories by remember { mutableStateOf("") }
    var categoryAmountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}
    var showCustomSubCategoryDialog by remember { mutableStateOf(false) }
    var customSubCategoryName by remember { mutableStateOf("") }

    var isCalculatorOpen by remember { mutableStateOf(false) }
    val installedCatalogKeys by addCategoryScreenViewModel.getBudgetCategoriesFlow(id)
        .collectAsState(initial = emptyList())

    fun clearErrorStatus() {
        categoryNameErrorStatus = ErrorStatus(isError = false)
        categoryAmountErrorStatus = ErrorStatus(isError = false)
    }

    fun isFormValid(): Boolean {
        var isValid = true
        if (categoryName.isEmpty()) {
            categoryNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Category Name is required")
            isValid = false
        }
        if (categoryAmount.isEmpty()) {
            categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Category Amount is required")
            isValid = false
        } else if (categoryAmount.toDoubleOrNull() == null) {
            categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Invalid Amount")
            isValid = false
        } else {
            val maxAmount = currentBudget.initialBalance - currentBudget.budgetedAmount
            Logger.d("maxAmount: $maxAmount")
            if (amountToCents(categoryAmount) > maxAmount) {
                categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Amount exceeds maximum of ${centsToString(maxAmount)}")
                isValid = false
            }
        }

        return isValid
    }

    if (isCalculatorOpen) {
        CalculatorDialog(
            onDismissRequest = { isCalculatorOpen = false },
            onApply = {
                if (isValidAmount(it)) {
                    categoryAmount = it
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ){
        Text(text="Add Category $id")
        Text("Choose a common category as a template or enter a custom category below")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(addCategoryScreenViewModel.commonCategories, key = { it.key }) { definition ->
                val alreadyAdded = definition.key in installedCatalogKeys.mapNotNull { it.catalogKey }
                FilterChip(
                    selected = selectedCommonCategory?.key == definition.key,
                    enabled = !alreadyAdded,
                    onClick = {
                        selectedCommonCategory = definition
                        categoryName = definition.name
                        categoryNameErrorStatus = ErrorStatus(isError = false)
                    },
                    label = { Text(if (alreadyAdded) "${definition.name} ✓" else definition.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = MaterialCategoryIconRegistry.resolve(definition.iconKey),
                            contentDescription = null,
                        )
                    },
                )
            }
        }
        BGainOutlineField(
            modifier = Modifier
                .fillMaxWidth(),
            labelStr = "Category Name",
            Value = categoryName,
            errorStatus = categoryNameErrorStatus,
            onValueChange = {
                categoryName = it
            },
            validator = {
                if (it.isEmpty()){
                    categoryNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Name is required")
                    return@BGainOutlineField
                }
                categoryNameErrorStatus = ErrorStatus(isError = false)
            },
            submitAttempted = submitAttempted
        )
        FilterChip(
            selected = false,
            onClick = { showCustomSubCategoryDialog = true },
            label = { Text("Add subcategory") },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (customSubCategories.isBlank()) {
                "No extra subcategories added"
            } else {
                "Extra subcategories: $customSubCategories"
            }
        )
        Text("Max: ${centsToString(currentBudget.initialBalance - currentBudget.budgetedAmount)}")
        BGainOutlineField(
            modifier = Modifier
                .fillMaxWidth(),
            labelStr = "Category Amount",
            Value = categoryAmount,
            errorStatus = categoryAmountErrorStatus,
            onValueChange = { categoryAmount = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            validator = {
                if (it.isEmpty()){
                    categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is required")
                    return@BGainOutlineField
                }
                if (it.toFloatOrNull() == null) {
                    categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is invalid")
                    return@BGainOutlineField
                }
                categoryAmountErrorStatus = ErrorStatus(isError = false)
            },
            submitAttempted = submitAttempted,
            trailingIcon = {
                IconButton(onClick = {
                    isCalculatorOpen = true
                }){
                    Icon(
                        painter = painterResource(Res.drawable.calculator_variant_outline),
                        contentDescription = "Open Calculator"
                    )
                }
        }
    )

    if (showCustomSubCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomSubCategoryDialog = false },
            title = { Text("Add subcategory") },
            text = {
                OutlinedTextField(
                    value = customSubCategoryName,
                    onValueChange = { customSubCategoryName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subcategory name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = customSubCategoryName.isNotBlank(),
                    onClick = {
                        val trimmed = customSubCategoryName.trim()
                        customSubCategories = listOf(customSubCategories, trimmed)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                        customSubCategoryName = ""
                        showCustomSubCategoryDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSubCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Button(onClick = {
                submitAttempted = true
                clearErrorStatus()
                val isFormValid = isFormValid()
                Logger.d("isFormValid: $isFormValid")
                if (!isFormValid) {
                    return@Button
                }
                addCategoryScreenViewModel.createCategory(
                    CategoryEntity(
                        budgetId = id,
                        name = categoryName,
                        amount = amountToCents(categoryAmount),
                        spentAmount = 0,
                        catalogKey = if (selectedCommonCategory != null) {
                            CommonCategoryCatalog.normalizedKey(categoryName)
                        } else null,
                        lightColorArgb = DEFAULT_CATEGORY_LIGHT_COLOR_ARGB,
                        darkColorArgb = DEFAULT_CATEGORY_DARK_COLOR_ARGB,
                        iconKey = DEFAULT_CATEGORY_ICON_KEY,
                    ),
                    commonDefinition = selectedCommonCategory,
                    customSubCategoryNames = customSubCategories.split(','),
                    onComplete = onNavigateBack,
                )
            }){
                Text(text = "Add Category")
            }
            OutlinedButton(onClick = {
                onNavigateBack()
            }){
                Text(text = "Cancel")
            }
        }

    }


}
