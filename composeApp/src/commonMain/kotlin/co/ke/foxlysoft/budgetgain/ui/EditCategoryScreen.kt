package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.calculator_variant_outline
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.calc.CalculatorDialog
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.ke.foxlysoft.budgetgain.utils.isValidAmount
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun EditCategoryScreen (
    categoryId: Long,
    editCategoryScreenViewModel: EditCategoryScreenViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
){
    val scope = rememberCoroutineScope()
    var submitAttempted by remember { mutableStateOf(false) }

    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    var categoryName by remember { mutableStateOf("") }
    var categoryNameErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var categoryAmount by remember { mutableStateOf("") }
    var categoryAmountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }

    var isCalculatorOpen by remember { mutableStateOf(false) }

    var currentBudget = editCategoryScreenViewModel.currentBudget.collectAsState().value

    fun getCategory() {
        scope.launch {
            val category = editCategoryScreenViewModel.getCategory(categoryId)
            categoryToEdit = category
            categoryName = category.name
            categoryAmount = (category.amount/100).toString()
        }

    }

    LaunchedEffect(Unit) {
        getCategory()
    }

    fun clearErrorStatus() {
        categoryNameErrorStatus = ErrorStatus(isError = false)
        categoryAmountErrorStatus = ErrorStatus(isError = false)
    }

    fun isFormValid(): Boolean {
        var isValid = true
        if (categoryName.isEmpty()) {
            categoryNameErrorStatus =
                ErrorStatus(isError = true, errorMsg = "Category Name is required")
            isValid = false
        }
        if (categoryAmount.isEmpty()) {
            categoryAmountErrorStatus =
                ErrorStatus(isError = true, errorMsg = "Category Amount is required")
            isValid = false
        } else if (categoryAmount.toDoubleOrNull() == null) {
            categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Invalid Amount")
            isValid = false
        }

        if (categoryAmount.toDoubleOrNull() != null){
            val doubleCategoryAmount = categoryAmount.toDouble()
            // validate min/max

            if (doubleCategoryAmount < 0.0) {
                categoryAmountErrorStatus =
                    ErrorStatus(isError = true, errorMsg = "Category Amount must be greater than 0")
                isValid = false
            }

            if (doubleCategoryAmount * 100 < (categoryToEdit?.spentAmount?.toDouble() ?: 0.0)) {
                categoryAmountErrorStatus =
                    ErrorStatus(isError = true, errorMsg = "Category Amount cannot be less than min")
                isValid = false
            }

            val max = (currentBudget.initialBalance - currentBudget.budgetedAmount) + (categoryToEdit?.amount ?: 0)
            if (doubleCategoryAmount * 100 > max) {
                categoryAmountErrorStatus =
                    ErrorStatus(isError = true, errorMsg = "Category Amount cannot be more than max")
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
            .padding(16.dp)
    ){
        Text(text="Edit Category")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text="Remaining Amount to Budget")
        Text(text= centsToString(currentBudget.initialBalance - currentBudget.budgetedAmount))
        Spacer(modifier = Modifier.height(8.dp))
        BGainOutlineField(
            modifier = Modifier
                .fillMaxWidth(),
            labelStr = "Category Name",
            Value = categoryName,
            errorStatus = categoryNameErrorStatus,
            onValueChange = { categoryName = it },
            validator = {
                if (it.isEmpty()){
                    categoryNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Name is required")
                    return@BGainOutlineField
                }
                categoryNameErrorStatus = ErrorStatus(isError = false)
            },
            submitAttempted = submitAttempted
        )
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
                if (it.toDoubleOrNull() == null) {
                    categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Budget Amount is invalid")
                    return@BGainOutlineField
                }
                if (it.toDoubleOrNull() != null){
                    val doubleCategoryAmount = it.toDouble()
                    // validate min/max

                    if (doubleCategoryAmount < 0.0) {
                        categoryAmountErrorStatus =
                            ErrorStatus(isError = true, errorMsg = "Category Amount must be greater than 0")
                        return@BGainOutlineField
                    }

                    if (doubleCategoryAmount * 100 < (categoryToEdit?.spentAmount?.toDouble() ?: 0.0)) {
                        categoryAmountErrorStatus =
                            ErrorStatus(isError = true, errorMsg = "Category Amount cannot be less than min")
                        return@BGainOutlineField
                    }

                    val max = (currentBudget.initialBalance - currentBudget.budgetedAmount) + (categoryToEdit?.amount ?: 0)
                    if (doubleCategoryAmount * 100 > max) {
                        categoryAmountErrorStatus =
                            ErrorStatus(isError = true, errorMsg = "Category Amount cannot be greater than max")
                        return@BGainOutlineField
                    }
                }
                categoryAmountErrorStatus = ErrorStatus(isError = false)
            },
            submitAttempted = submitAttempted,
            trailingIcon = {
                IconButton(onClick = {isCalculatorOpen = true}){
                    Icon(
                        painter = painterResource(Res.drawable.calculator_variant_outline),
                        contentDescription = "Open Calculator"
                    )
                }
            }
        )
        Row {
            Text(text="Min: ${centsToString(categoryToEdit?.spentAmount ?: 0)}")
            Spacer(modifier = Modifier.weight(1f))
            Text(text="Max: ${centsToString((currentBudget.initialBalance - currentBudget.budgetedAmount) + (categoryToEdit?.amount ?: 0))}")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Button(onClick = {
                if (categoryToEdit == null) {
                    return@Button
                }
                submitAttempted = true
                clearErrorStatus()
                val isFormValid = isFormValid()
                Logger.d("isFormValid: $isFormValid")
                if (!isFormValid) {
                    return@Button
                }

                categoryToEdit!!.name = categoryName
                categoryToEdit!!.amount = amountToCents(categoryAmount)

                editCategoryScreenViewModel.editCategory(
                    categoryToEdit!!,
                    onComplete = {
                        onNavigateBack()
                    }
                )
            }){
                Text(text = "Edit Category")
            }
            OutlinedButton(onClick = {
                onNavigateBack()
            }){
                Text(text = "Cancel")
            }
        }

    }
}