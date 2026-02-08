package co.ke.foxlysoft.budgetgain.ui.charts

import androidx.collection.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.width
import co.ke.foxlysoft.budgetgain.ui.Theme.BudgetGainTheme
import co.ke.foxlysoft.budgetgain.ui.Theme.Purple400
import co.ke.foxlysoft.budgetgain.ui.Theme.Purple700
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.math.abs
import kotlin.time.Clock

@Composable
fun MonthLineChart(
    yearMonth: String,
    dailyData: List<Pair<Int, Long>>
) {
    val currentDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    val daysInMonth = currentDate.yearMonth.numberOfDays
    val yData = (1..daysInMonth).map { day ->
        val point = dailyData.find { it.first == day }?.second?.toFloat() ?: 0f
        if (point == 0f) 0f else point/100

    }

    // State to hold the index of the selected data point
    var selectedIndex by remember { mutableStateOf<Int?>(6) }
    // TextMeasurer to draw text on the canvas
    val textMeasurer = rememberTextMeasurer()


    val labelColor = MaterialTheme.colorScheme.onSurface
    val lineAndFillColor = MaterialTheme.colorScheme.primary
    val selectionIndicatorColor = MaterialTheme.colorScheme.primary
    val selectionCircleInnerColor = MaterialTheme.colorScheme.onPrimary
    val selectionGuidelineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val tooltipBackgroundColor = MaterialTheme.colorScheme.onBackground
    val tooltipTextColor = MaterialTheme.colorScheme.onPrimary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(
                top = 16.dp,
                end = 16.dp,
                start = 16.dp,
                bottom = 40.dp
            )
            .pointerInput(Unit) {
                detectDragGestures( // 2. Use detectDragGestures for continuous input
                    onDragStart = { offset ->
                        // Logic to find and set the index on initial touch
                        val chartWidth = size.width
                        val xStep = chartWidth / (yData.size - 1)
                        val index = (offset.x / xStep).toInt().coerceIn(0, yData.size - 1)
                        selectedIndex = index
                    },
                    onDragEnd = {
                        // 3. Clear the selection when the user lifts their finger
//                        selectedIndex = null
                    },
                    onDrag = { change, _ ->
                        // 4. Update the index as the user drags their finger
                        change.consume()
                        val chartWidth = size.width
                        val xStep = chartWidth / (yData.size - 1)
                        val index = (change.position.x / xStep).toInt().coerceIn(0, yData.size - 1)
                        selectedIndex = index
                    }
                )
            }
    ) {
        val xData = dailyData.map { it.first.toFloat() }

        val maxValue = yData.maxOrNull() ?: 0f
        val minValue = yData.minOrNull() ?: 0f

        val chartWidth = size.width
        val chartHeight = size.height

        val xStep = chartWidth / (yData.size - 1)

        fun scaleY(value: Float): Float {
            if (maxValue == minValue) return chartHeight /2f
            return chartHeight - (
                    (value - minValue) / (maxValue - minValue)
                    ) * chartHeight
        }

        // --- Draw X-Axis Labels ---
        val xAxisLabels = listOf(1, 5, 10, 15, 20, 25, daysInMonth).distinct()
        xAxisLabels.forEach { day ->
            val labelIndex = day - 1
            if (labelIndex in yData.indices) {
                val labelX = labelIndex * xStep
                // 3. Use the Compose 'drawText' function
                val textLayoutResult = textMeasurer.measure(
                    text = day.toString(),
                    style = TextStyle(
                        color = labelColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = labelX - (textLayoutResult.size.width / 2),
                        y = chartHeight + 8.dp.toPx() // Position below the chart area
                    )
                )
            }
        }

        val path = Path()

        yData.forEachIndexed { index, value ->
            val x = index * xStep
            val y = scaleY(value)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (index - 1) * xStep
                val prevY = scaleY(yData[index - 1])

                val midX = (prevX + x) / 2

                path.cubicTo(
                    midX, prevY,
                    midX, y,
                    x, y
                )
            }
        }

//        val lineColor = MaterialTheme.colorScheme.primary

        drawPath(
            path = path,
            color = lineAndFillColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 10f
            )
        )

        val fillPath = Path().apply { addPath(path) }
        fillPath.lineTo(chartWidth, chartHeight)
        fillPath.lineTo(0f, chartHeight)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    lineAndFillColor.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                endY = chartHeight
            )
        )

        // --- Interaction Drawing ---
        selectedIndex?.let { index ->
            val x = index * xStep
            val y = scaleY(yData[index])
            val isSelected = selectedIndex == index

            // If selected, draw the value as text above the point
            if (isSelected) {
                // Draw gray horizontal line across the chart at the selected Y position
                drawLine(
                    color = selectionGuidelineColor,
                    start = Offset(x, chartHeight),
                    end = Offset(x, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // Creates a dashed line
                )

                // Draw a larger circle if the point is selected
                drawCircle(
                    color = selectionIndicatorColor,
                    radius = if (isSelected) 20f else 10f,
                    center = Offset(x, y)
                )

                // Draw the smaller white inner circle
                drawCircle(
                    color = selectionCircleInnerColor,
                    radius = 8f,
                    center = Offset(x, y)
                )

                val textValue = yData[index].toString()
                drawTooltip(
                    textMeasurer = textMeasurer,
                    text = textValue,
                    position = Offset(x, y),
                    backgroundColor = tooltipBackgroundColor,
                    textColor = tooltipTextColor,
                    shadowColor = labelColor.copy(alpha = 0.2f) // Softer shadow
                )
            }
        }

    }
}

// At the bottom of LineChart.kt or in a new utils file

private fun DrawScope.drawTooltip(
    textMeasurer: TextMeasurer,
    text: String,
    position: Offset,
    backgroundColor: Color,
    textColor: Color,
    shadowColor: Color,
    cornerRadius: Float = 8f
) {
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    )

    val tooltipWidth = textLayoutResult.size.width + 32.dp.toPx()
    val tooltipHeight = textLayoutResult.size.height + 16.dp.toPx()
    val tooltipY = position.y - tooltipHeight - 16.dp.toPx() // Position above the circle

    // Smartly adjust X position to stay within the canvas bounds
    var tooltipX = position.x - (tooltipWidth / 2)
    if (tooltipX < 0) {
        tooltipX = 0f
    }
    if (tooltipX + tooltipWidth > size.width) {
        tooltipX = size.width - tooltipWidth
    }

    // --- Multiplatform Shadow ---
    // Draw a semi-transparent copy of the shape slightly offset for a shadow effect.
    val shadowOffset = Offset(4f, 4f)
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(tooltipX + shadowOffset.x, tooltipY + shadowOffset.y),
        size = Size(tooltipWidth, tooltipHeight),
        cornerRadius = CornerRadius(cornerRadius)
    )
    // --- End of Shadow ---

    // Draw tooltip background
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(tooltipX, tooltipY),
        size = Size(tooltipWidth, tooltipHeight),
        cornerRadius = CornerRadius(cornerRadius)
    )

    // Draw text inside the tooltip
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(tooltipX + 16.dp.toPx(), tooltipY + 8.dp.toPx())
    )
}


@Composable
@Preview
fun MonthLineChartPreview(){
    val sampleDailyData = listOf(
        1 to 2_500L,
        2 to 7_500L,
        3 to 1_200L,
        4 to 9_000L,
        5 to 3_400L,
        6 to 5_800L,
        7 to 6_100L,
        20 to 5_000L
    )

    BudgetGainTheme {
        Surface {
            MonthLineChart("2026-02",dailyData = sampleDailyData)
        }
    }
}

@Composable
@Preview
fun DarkMonthLineChartPreview(){
    val sampleDailyData = listOf(
        1 to 2_500L,
        2 to 7_500L,
        3 to 1_200L,
        4 to 9_000L,
        5 to 3_400L,
        6 to 5_800L,
        7 to 6_100L,
        20 to 5_000L
    )

    BudgetGainTheme(darkTheme = true) {
        Surface {
            MonthLineChart("2026-02", dailyData = sampleDailyData)
        }
    }
}