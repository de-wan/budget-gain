package co.ke.foxlysoft.budgetgain.utils

import androidx.compose.ui.text.input.TextFieldValue


data class TextFieldInput(
    var value: TextFieldValue = TextFieldValue(),
    var hasInteracted: Boolean = false,
)

data class FieldInput(
    val value: String = "",
    val hasInteracted: Boolean = false,
)

data class ErrorStatus(
    val isError: Boolean,
    val errorMsg: String? = null,
)