package co.ke.foxlysoft.budgetgain.my_calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WeekHeader() {
    val days = listOf("Su","Mo","Tu","We","Th","Fr","Sa")
    Row(modifier = Modifier.fillMaxWidth()
    ) {
        days.forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(day,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

        }
    }
}

@Preview
@Composable
fun WeekHeaderPreview() {
    BudgetGainTheme {
        Surface {
            WeekHeader()
        }
    }
}

@Preview
@Composable
fun DarkWeekHeaderPreview() {
    BudgetGainTheme(darkTheme = true) {
        Surface {
            WeekHeader()
        }
    }
}