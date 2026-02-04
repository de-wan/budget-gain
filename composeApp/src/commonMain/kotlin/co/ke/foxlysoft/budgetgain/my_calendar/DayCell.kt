package co.ke.foxlysoft.budgetgain.my_calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DayCell(
    date: LocalDate?,
    dayStatus: DayStatus?,
    onDayClick: (LocalDate) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isToday = date == today
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(enabled = date != null) { date?.let(onDayClick) }
    ) {
        date?.let {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val backgroundModifier = if (isToday) {
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .then(backgroundModifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it.dayOfMonth.toString(),
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    if (dayStatus?.isMovedForward == true) Dot(Color.Green)
                    if (dayStatus?.isUsed == true) Dot(Color.Blue)
                    if (dayStatus?.isOverUsed == true) Dot(Color.Red)
                }
            }
        }
    }
}

@Composable
fun Dot(color: Color) {
    Box(
        Modifier
            .padding(2.dp)
            .size(6.dp)
            .background(color, shape = CircleShape)
    )
}

@Composable
@Preview
fun DayCellPreview() {
    BudgetGainTheme {
        Surface {
            DayCell(
                date = LocalDate(2023, 1, 1),
                dayStatus = DayStatus(
                    isMovedForward = true,
                    isUsed = true,
                    isOverUsed = true
                ),
                onDayClick = {}
            )
        }
    }
}

@Composable
@Preview
fun TodayDayCellPreview() {
    BudgetGainTheme {
        Surface {
            DayCell(
                date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                dayStatus = DayStatus(
                    isMovedForward = true,
                    isUsed = true,
                    isOverUsed = true
                ),
                onDayClick = {}
            )
        }
    }
}

@Composable
@Preview
fun DarkDayCellPreview() {
    BudgetGainTheme(darkTheme = true) {
        Surface {
            DayCell(
                date = LocalDate(2023, 1, 1),
                dayStatus = DayStatus(
                    isMovedForward = true,
                    isUsed = true,
                    isOverUsed = true
                ),
                onDayClick = {}
            )
        }
    }
}