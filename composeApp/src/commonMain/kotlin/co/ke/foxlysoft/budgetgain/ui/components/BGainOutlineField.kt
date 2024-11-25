package co.ke.foxlysoft.budgetgain.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import budgetgain.composeapp.generated.resources.Res
import budgetgain.composeapp.generated.resources.ic_visibility
import co.ke.foxlysoft.budgetgain.utils.ErrorStatus
import co.ke.foxlysoft.budgetgain.utils.FieldInput
import co.ke.foxlysoft.budgetgain.utils.TextFieldInput
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlineFieldWithState(
    modifier: Modifier = Modifier,
    labelStr: String,
    textFieldInput: TextFieldInput,
    errorStatus: ErrorStatus,
    keyboardOptions: KeyboardOptions,
    isPasswordField: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: ImageVector? = null,
    onValueChange: (TextFieldValue) -> Unit,
) {

    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = textFieldInput.value,
        onValueChange = { onValueChange(it) },
        label = { Text(labelStr, style = MaterialTheme.typography.bodyMedium) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                )
            }
        },
        isError = textFieldInput.hasInteracted && errorStatus.isError,
        supportingText = {
            if (textFieldInput.hasInteracted && errorStatus.isError) {
                errorStatus.errorMsg?.let {
                    Text(
                        text = it, modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        trailingIcon = if (isPasswordField) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = if (passwordVisible) painterResource(Res.drawable.ic_visibility)
//                        else painterResource(Res.drawable.ic_visibility_off),
                        else painterResource(Res.drawable.ic_visibility),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        } else if (textFieldInput.hasInteracted && errorStatus.isError) {
            {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null)
            }
        } else {
            null
        },
    )

//    OutlinedTextField(
//        modifier = modifier,
//        value = fieldInput.value,
//        onValueChange = {
//            onValueChange(it)
//        },
//        label = {
//            Text(text = label, style = MaterialTheme.typography.bodyMedium)
//        },
//        singleLine = true,
//        keyboardOptions = keyboardOptions,
//        keyboardActions = keyboardActions,
//        leadingIcon = leadingIcon,
//        isError = fieldInput.hasInteracted && errorStatus.isError,
//        supportingText = {
//            if (fieldInput.hasInteracted && errorStatus.isError) {
//                errorStatus.errorMsg?.let {
//                    Text(
//                        text = it.asString(), modifier = Modifier.fillMaxWidth(),
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//        },
//        trailingIcon = if (isPasswordField) {
//            {
//                IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                    Icon(
//                        painter = if (passwordVisible) painterResource(R.drawable.ic_visibility)
//                        else painterResource(R.drawable.ic_visibility_off),
//                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
//                    )
//                }
//            }
//        } else if (fieldInput.hasInteracted && errorStatus.isError) {
//            {
//                Icon(imageVector = Icons.Filled.Info, contentDescription = null)
//            }
//        } else {
//            null
//        },
//        visualTransformation = if (isPasswordField && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None
//    )
}