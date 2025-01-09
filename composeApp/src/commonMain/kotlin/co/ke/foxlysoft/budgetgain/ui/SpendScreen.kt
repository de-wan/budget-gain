package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.ic_attach_file
import co.ke.foxlysoft.budgetgain.ui.components.BGainOutlineField
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.amountToCents
import co.ke.foxlysoft.budgetgain.utils.centsToString
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SpendScreen(
    categoryId: Long,
    spendScreenViewModel: SpendScreenViewModel = koinViewModel(parameters = {
        parametersOf(
            categoryId
        )
    }),
    onNavigateBack: () -> Unit
) {
    val category = spendScreenViewModel.currentCategory.collectAsState().value

    val merchantAccounts by spendScreenViewModel.merchantAccounts.collectAsState()
    var merchantAutoCompleteExpanded by remember { mutableStateOf(false) }

    var ref by remember { mutableStateOf("") }
    var refErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var amount by remember { mutableStateOf("") }
    var amountErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var merchant by remember { mutableStateOf("") }
    var merchantErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var description by remember { mutableStateOf("") }
    var descriptionErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var timestamp by remember { mutableStateOf("") }
    var timestampErrorStatus by remember { mutableStateOf(ErrorStatus(isError = false)) }
    var submitAttempted by remember { mutableStateOf(false) }

    fun clearErrorStatus() {
        refErrorStatus = ErrorStatus(isError = false)
        amountErrorStatus = ErrorStatus(isError = false)
        merchantErrorStatus = ErrorStatus(isError = false)
        descriptionErrorStatus = ErrorStatus(isError = false)
        timestampErrorStatus = ErrorStatus(isError = false)
    }

    fun isFormValid(): Boolean {
        var isValid = true
        if (ref.isEmpty()) {
            refErrorStatus = ErrorStatus(isError = true, errorMsg = "Ref is required")
            isValid = false
        }
        if (amount.isEmpty()) {
            amountErrorStatus = ErrorStatus(isError = true, errorMsg = "Amount is required")
            isValid = false
        } else if (amount.toFloatOrNull() == null) {
            amountErrorStatus = ErrorStatus(isError = true, errorMsg = "Amount is invalid")
            isValid = false
        }
        if (merchant.isEmpty()) {
            merchantErrorStatus = ErrorStatus(isError = true, errorMsg = "Merchant is required")
            isValid = false
        }
        if (description.isEmpty()) {
            descriptionErrorStatus =
                ErrorStatus(isError = true, errorMsg = "Description is required")
            isValid = false
        }
        if (timestamp.isEmpty()) {
            timestampErrorStatus = ErrorStatus(isError = true, errorMsg = "Timestamp is required")
            isValid = false
        }
        return isValid
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Text("Spend", style = MaterialTheme.typography.headlineLarge)
            IconButton(onClick = {
                // TODO: Open sms picker
            }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_attach_file),
                    contentDescription = "Attach Sms"
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("${category?.name}", style = MaterialTheme.typography.headlineSmall)


        }
        Text("Category Balance: ${category?.amount?.minus(category.spentAmount)
            ?.let { centsToString(it) }}")

        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Ref",
                    Value = ref,
                    errorStatus = refErrorStatus,
                    onValueChange = { ref = it },
                    validator = {
                        if (it.isEmpty()) {
                            refErrorStatus =
                                ErrorStatus(isError = true, errorMsg = "Ref is required")
                            return@BGainOutlineField
                        }
                        refErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Merchant",
                    Value = merchant,
                    errorStatus = merchantErrorStatus,
                    onValueChange = {
                        println("Merchant changed to $it")
                        spendScreenViewModel.updateMerchantSearchQuery(it)
                        merchantAutoCompleteExpanded = true

                        merchant = it
                    },
                    validator = {
                        if (it.isEmpty()) {
                            merchantErrorStatus =
                                ErrorStatus(isError = true, errorMsg = "Merchant is required")
                            return@BGainOutlineField
                        }
                        merchantErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                // Dropdown menu
                Box {
                    if (merchantAutoCompleteExpanded && merchantAccounts.isNotEmpty()) {
                        Popup(
                            onDismissRequest = { merchantAutoCompleteExpanded = false },
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .zIndex(1f),
                            ) {
                                LazyColumn {
                                    items(items = merchantAccounts) { account ->
                                        TextButton(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            onClick = {
                                                merchant = account.merchantName
                                                merchantAutoCompleteExpanded = false
                                            },
                                        ){
                                            Text(text = account.merchantName)
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Description",
                    Value = description,
                    errorStatus = descriptionErrorStatus,
                    onValueChange = { description = it },
                    validator = {
                        if (it.isEmpty()) {
                            descriptionErrorStatus =
                                ErrorStatus(isError = true, errorMsg = "Description is required")
                            return@BGainOutlineField
                        }
                        descriptionErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    labelStr = "Amount *",
                    Value = amount,
                    errorStatus = amountErrorStatus,
                    onValueChange = { amount = it },
                    validator = {
                        if (it.isEmpty()) {
                            amountErrorStatus =
                                ErrorStatus(isError = true, errorMsg = "Amount is required")
                            return@BGainOutlineField
                        }
                        amountErrorStatus = ErrorStatus(isError = false)
                    },
                    submitAttempted = submitAttempted
                )
                BGainOutlineField(
                    onDateChange = { },
                    labelStr = "Select Timestamp",
                    errorStatus = timestampErrorStatus,
                    isDateTimePicker = true,
                    onDateTimeChange = { timestamp = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    submitAttempted = submitAttempted
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        submitAttempted = true
                        clearErrorStatus()
                        val isFormValid = isFormValid()
                        Logger.d("isFormValid: $isFormValid")
                        if (!isFormValid) {
                            return@Button
                        }
                        try {
                            spendScreenViewModel.spend(
                                onComplete = {
//                                    onNavigateBack()
                                },
                                onError = {
//                                    toastManager.showToast("Error")
                                },
                                ref,
                                merchant,
                                description,
                                amountToCents(amount),
                                timestamp
                            )
                        } catch (e: Exception) {
                            Logger.e("Error spending", e)
                        }

                        onNavigateBack()
                    }) {
                        Text(text = "Spend")
                    }
                    FilledTonalButton(onClick = {
                        onNavigateBack()
                    }) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }

}