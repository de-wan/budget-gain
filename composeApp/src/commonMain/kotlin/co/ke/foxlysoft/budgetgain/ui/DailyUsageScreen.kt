package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.my_calendar.DayStatus
import co.ke.foxlysoft.budgetgain.my_calendar.MonthCalendar
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateBudgetScreen(
    dailyUsageScreenViewModel: DailyUsageScreenViewModel = koinViewModel(),
) {
    val dayStatus = mutableMapOf<LocalDate, DayStatus>()
    CreateBudgetScreenContent(dayStatus)
}

data class RemainingBudget (
    val type: String,
    val amount: Long,
    val percentage: Double = 50.0
)

@Composable
fun CreateBudgetScreenContent(dayStatus: Map<LocalDate, DayStatus>) {
    val remainingBudges = listOf(
        RemainingBudget("Daily", 1000),
        RemainingBudget("Weekly", 1000),
        RemainingBudget("Monthly", 1000, 100.0)
    )

    Column(modifier = Modifier.fillMaxSize()
        .padding(16.dp)) {
        Text("Daily Usage", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Remaining Budget")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ){
            for (remainingBudget in remainingBudges) {
                Card(
                    modifier = Modifier.weight(1f).padding(8.dp, 2.dp)
                ){
                    Column(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(55.dp),
                                progress = {remainingBudget.percentage.toFloat() / 100},
                                trackColor = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(remainingBudget.percentage.toString().dropLast(2)+"%")
                        }
                        Row {
                            if (remainingBudget.type == "Daily") {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Money Icon",
                                )
                            } else if (remainingBudget.type == "Weekly") {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarViewWeek,
                                    contentDescription = "Money Icon",
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Money Icon",
                                )
                            }

                            Text(remainingBudget.type)
                        }

                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card {
            MonthCalendar(
                year = 2026,
                month = 2,
                onDayClick = {},
                dayStatus
            )
        }
    }
}

@Preview
@Composable
fun CreateBudgetScreenPreview() {
    val dayStatus = mutableMapOf<LocalDate, DayStatus>()
    BudgetGainTheme {
        Surface {
            CreateBudgetScreenContent(dayStatus)
        }
    }
}