package com.protech.ojtjournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatsBarChart(
    data: List<Pair<String, Float>>, // Label, Value pairs
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = TextStyle(
        fontSize = 12.sp,
        color = textColor
    )
    val valueTextStyle = TextStyle(
        fontSize = 10.sp,
        color = textColor,
        textAlign = TextAlign.Center
    )
    
    Canvas(modifier = modifier.padding(
        top = 10.dp,
        bottom = 30.dp,
        start = 10.dp,
        end = 10.dp
    )) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = (canvasWidth - 20) / data.size - 10
        
        // Find the maximum value for scaling
        val maxValue = data.maxOfOrNull { it.second } ?: 1f
        val scaleFactor = canvasHeight / (maxValue * 1.2f)
        
        // Draw horizontal grid lines
        val gridLineCount = 5
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 0..gridLineCount) {
            val y = canvasHeight - (canvasHeight * i / gridLineCount)
            drawLine(
                color = axisColor,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f,
                pathEffect = dashPathEffect
            )
        }
        
        // Draw bars and labels
        data.forEachIndexed { index, (label, value) ->
            val barHeight = value * scaleFactor
            val x = (index * (barWidth + 10)) + 10
            val y = canvasHeight - barHeight
            
            // Draw bar
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
            
            // Draw value above bar
            if (value > 0) {
                val valueText = AnnotatedString(value.toInt().toString())
                val textSize = textMeasurer.measure(valueText, valueTextStyle)
                val textY = (y - textSize.size.height).coerceAtLeast(0f)
                drawText(
                    textMeasurer = textMeasurer,
                    text = valueText,
                    topLeft = Offset(
                        x + (barWidth - textSize.size.width) / 2,
                        textY
                    ),
                    style = valueTextStyle
                )
            }
            
            // Draw label below bar
            val labelText = AnnotatedString(label)
            val labelTextSize = textMeasurer.measure(labelText, labelTextStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                topLeft = Offset(
                    x + (barWidth - labelTextSize.size.width) / 2,
                    canvasHeight
                ),
                style = labelTextStyle
            )
        }
    }
}

@Composable
fun StatsLineChart(
    data: List<Pair<String, Float>>, 
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF3F51B5)
) {
    if (data.isEmpty()) return
    
    // Use data as-is, as it should already be properly ordered
    val chartData = data
    
    val maxValue = chartData.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 24f // Slightly smaller text size
        color = Color.Gray.toArgb()
    }
    
    Canvas(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp) // Increased vertical padding
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height - 48f  // Leave space for x-axis labels
        
        // Calculate point spacing
        val pointSpacing = if (chartData.size > 1) canvasWidth / (chartData.size - 1) else canvasWidth
        
        // Draw background grid
        val gridColor = Color.LightGray.copy(alpha = 0.3f)
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        
        // Draw horizontal grid lines
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val y = canvasHeight * i / gridLineCount
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f,
                pathEffect = dashPathEffect
            )
        }
        
        // Draw vertical grid lines
        for (i in 0 until chartData.size) {
            val x = i * pointSpacing
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, canvasHeight),
                strokeWidth = 1f,
                pathEffect = dashPathEffect
            )
        }
        
        // Draw axis lines
        drawLine(
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, canvasHeight),
            color = Color.Gray,
            strokeWidth = 2f
        )
        drawLine(
            start = Offset(0f, 0f),
            end = Offset(0f, canvasHeight),
            color = Color.Gray,
            strokeWidth = 2f
        )
        
        // Draw points and lines
        val points = chartData.mapIndexed { index, (_, value) ->
            val x = index * pointSpacing
            val y = canvasHeight - (value / maxValue * canvasHeight).coerceAtMost(canvasHeight - 10f)
            Offset(x, y)
        }
        
        // Draw area under the line
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, canvasHeight)
                lineTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(points.last().x, canvasHeight)
                close()
            }
            drawPath(
                path = fillPath,
                color = lineColor.copy(alpha = 0.1f)
            )
        }
        
        // Draw lines between points with shadow
        if (points.size > 1) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            
            // Draw shadow
            drawPath(
                path = linePath,
                color = lineColor.copy(alpha = 0.3f),
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
            
            // Draw main line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
        
        // Draw points with outer ring
        points.forEach { point ->
            // Draw outer circle
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = point
            )
            // Draw inner circle
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = point
            )
        }
        
        // Draw values above points
        points.forEachIndexed { index, point ->
            val value = chartData[index].second.toInt().toString()
            val textSize = textPaint.measureText(value)
            
            // Ensure text Y position is never negative
            val textY = (point.y - textPaint.fontMetrics.descent - 16).coerceAtLeast(16f)
            
            drawContext.canvas.nativeCanvas.drawText(
                value,
                point.x - (textSize / 2),
                textY,
                textPaint
            )
        }
        
        // Draw x-axis labels with better positioning
        chartData.forEachIndexed { index, (label, _) ->
            val x = index * pointSpacing
            val textSize = textPaint.measureText(label)
            
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x - (textSize / 2),
                canvasHeight + 36,
                textPaint
            )
        }
    }
} 