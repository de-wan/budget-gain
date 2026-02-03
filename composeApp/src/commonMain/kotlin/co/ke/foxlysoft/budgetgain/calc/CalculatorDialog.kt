package co.ke.foxlysoft.budgetgain.calc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import co.ke.foxlysoft.budgetgain.utils.formatWithCommas
import co.ke.foxlysoft.budgetgain.utils.isDigitStr

@Composable
fun CalculatorDialog(
    onDismissRequest: () -> Unit,
    onApply: (result: String) -> Unit,
) {
    var assistText by remember { mutableStateOf("")}
    var displayText by remember { mutableStateOf("0") }

    // Update display text state
    fun updateDisplay(text: String) {
        displayText = text
    }

    fun clearState() {
        updateDisplay("0")
        assistText = ""
    }

    // Handle button click
    fun handleButtonClick(input: String, updateDisplay: (String) -> Unit) {
        when {
            input == "Backspace" -> {
                if (assistText.isNotEmpty()) {
                    assistText = assistText.dropLast(1)
                }
            }
            input == "C" -> {
                clearState()
            }
            input == "=" -> {
                try {
                    val result = formatWithCommas(assistText.calc())
                    updateDisplay(result)
                } catch (e: Exception) {

                    updateDisplay("Error")
                }
            }
            else -> {
                assistText += input
            }
        }
    }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
        ) {
            Column (
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
            ){
                Text("Calculator",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Display element
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(16.dp), // Inner padding for text
                ){
                    Text(text = assistText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        maxLines = 1 // Ensure single-line display
                    )
                }

                // Define calculator buttons
                val buttons = listOf(
                    listOf("7", "8", "9", "/"),
                    listOf("4", "5", "6", "*"),
                    listOf("1", "2", "3", "-"),
                    listOf("0", ".", "=", "+")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ElevatedButton(onClick = { handleButtonClick("(", ::updateDisplay) }) {
                        Text(text = "(")
                    }
                    ElevatedButton(onClick = { handleButtonClick(")", ::updateDisplay) }) {
                        Text(text = ")")
                    }
                    ElevatedButton(onClick = { handleButtonClick("C", ::updateDisplay) }) {
                        Text(text = "C")
                    }
                    ElevatedButton(
                        onClick = { handleButtonClick("Backspace", ::updateDisplay) }
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace"
                        )
                    }
                }

                // Display rows dynamically
                for (row in buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (item in row) {
                            ElevatedButton(onClick = { handleButtonClick(item, ::updateDisplay) }) {
                                Text(text = item)
                            }
                        }
                    }
                }

                Row {
                    Button(onClick = {
                        onApply(displayText)
                        clearState()
                        onDismissRequest()
                    },) {
                        Text("Apply")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        clearState()
                        onDismissRequest()
                    }) {
                        Text("Cancel")
                    }
                }

            }
        }

    }
}