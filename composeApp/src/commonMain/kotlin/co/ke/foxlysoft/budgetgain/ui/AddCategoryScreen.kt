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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.touchlab.kermit.Logger
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

    var categoryName by remember { mutableStateOf(TextFieldValue("")) }
    var categoryNameErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var categoryAmount by remember { mutableStateOf(TextFieldValue("")) }
    var categoryAmountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false))}

    fun clearErrorStatus() {
        categoryNameErrorStatus = ErrorStatus(isError = false)
        categoryAmountErrorStatus = ErrorStatus(isError = false)
    }

    fun isFormValid(): Boolean {
        var isValid = true
        if (categoryName.text.isEmpty()) {
            categoryNameErrorStatus = ErrorStatus(isError = true, errorMsg = "Category Name is required")
            isValid = false
        }
        if (categoryAmount.text.isEmpty()) {
            categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Category Amount is required")
            isValid = false
        } else if (categoryAmount.text.toDoubleOrNull() == null) {
            categoryAmountErrorStatus = ErrorStatus(isError = true, errorMsg = "Invalid Amount")
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ){
        Text(text="Add Category $id")
        BGainOutlineField(
            modifier = Modifier
                .fillMaxWidth(),
            labelStr = "Category Name",
            textFieldInput = categoryName,
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
            textFieldInput = categoryAmount,
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
            submitAttempted = submitAttempted
        )
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
                        name = categoryName.text,
                        amount = amountToCents(categoryAmount.text),
                        spentAmount = 0,
                    )
                )
                onNavigateBack()
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