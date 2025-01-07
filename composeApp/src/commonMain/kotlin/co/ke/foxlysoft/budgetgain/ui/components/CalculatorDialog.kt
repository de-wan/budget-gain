package co.ke.foxlysoft.budgetgain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
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
import co.ke.foxlysoft.budgetgain.utils.isDigitStr

@Composable
fun CalculatorDialog(
    onDismissRequest: () -> Unit,
    onApply: (result: String) -> Unit,
) {
    var displayText by remember { mutableStateOf("0") }

    var currentOperator by remember { mutableStateOf<String?>(null) }
    var operand1 by remember { mutableStateOf<String?>(null) }
    var operand2 by remember { mutableStateOf<String?>(null) }

    // Update display text state
    fun updateDisplay(text: String) {
        displayText = text
    }

    fun clearState() {
        currentOperator = null
        operand1 = null
        operand2 = null
        updateDisplay("0")
    }

    // Perform calculation
    fun calculateResult(op1: String, op2: String, operator: String): String {
        return try {
            val num1 = op1.toDouble()
            val num2 = op2.toDouble()
            val result = when (operator) {
                "+" -> num1 + num2
                "-" -> num1 - num2
                "*" -> num1 * num2
                "/" -> if (num2 != 0.0) num1 / num2 else "Error"
                else -> "Error"
            }
            if (result is Double) result.toString() else "Error"
        } catch (e: Exception) {
            "Error"
        }
    }

    // Handle button click
    fun handleButtonClick(input: String, updateDisplay: (String) -> Unit) {
        when {
            isDigitStr(input) || input == "." -> {
                if (currentOperator == null) {
                    operand1 = (operand1 ?: "") + input
                    updateDisplay(operand1!!)
                } else {
                    operand2 = (operand2 ?: "") + input
                    updateDisplay(operand2!!)
                }
            }
            input in listOf("+", "-", "*", "/") -> {
                currentOperator = input
            }
            input == "=" -> {
                if (operand1 != null && operand2 != null && currentOperator != null) {
                    val result = calculateResult(operand1!!, operand2!!, currentOperator!!)
                    operand1 = result
                    operand2 = null
                    currentOperator = null
                    updateDisplay(result)
                }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(16.dp), // Inner padding for text
                ){
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