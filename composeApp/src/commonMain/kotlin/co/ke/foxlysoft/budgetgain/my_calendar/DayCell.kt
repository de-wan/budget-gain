package co.ke.foxlysoft.budgetgain.my_calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate

private val CarriedForwardIndicatorColor = Color(0xFF00C853)
private val UsedIndicatorColor = Color(0xFF2979FF)
private val OverusedIndicatorColor = Color(0xFFFF1744)

@Composable
fun DayCell(
    date: LocalDate?,
    dayStatus: DayStatus?,
    isSelected: Boolean = false,
    onDayClick: (LocalDate) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isToday = date == today
    Box(
        modifier = Modifier
            .size(48.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
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
                        text = it.day.toString(),
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    if (dayStatus?.isMovedForward == true) Dot(CarriedForwardIndicatorColor)
                    if (dayStatus?.isUsed == true) Dot(UsedIndicatorColor)
                    if (dayStatus?.isOverUsed == true) Dot(OverusedIndicatorColor)
                }
            }
        }
    }
}

@Composable
fun Dot(color: Color) {
    val outlineColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    Box(
        Modifier
            .padding(horizontal = 1.dp)
            .size(8.dp)
            .border(1.dp, outlineColor, CircleShape)
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

@Composable
@Preview
fun DarkTodayDayCellPreview() {
    BudgetGainTheme(darkTheme = true) {
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
fun SelectedDayCellPreview() {
    BudgetGainTheme {
        Surface {
            DayCell(
                date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                dayStatus = DayStatus(
                    isMovedForward = true,
                    isUsed = true,
                    isOverUsed = true
                ),
                onDayClick = {},
                isSelected = true
            )
        }
    }
}