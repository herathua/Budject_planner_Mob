package com.example.budject_planner.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budject_planner.domain.model.ChartData
import kotlin.math.*

@Composable
fun BudgetChart(
    chartData: ChartData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Budget Overview (Last 30 Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.labels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LineChart(
                    chartData = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(
                        color = Color(0xFF4CAF50),
                        label = "Income"
                    )
                    LegendItem(
                        color = Color(0xFFF44336),
                        label = "Expense"
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(
    chartData: ChartData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40.dp.toPx()
        
        val chartWidth = canvasWidth - 2 * padding
        val chartHeight = canvasHeight - 2 * padding
        
        val maxValue = maxOf(
            chartData.incomeData.maxOrNull() ?: 0.0,
            chartData.expenseData.maxOrNull() ?: 0.0
        )
        
        if (maxValue > 0) {
            // Draw grid lines
            drawGridLines(
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                maxValue = maxValue
            )
            
            // Draw income line
            drawLine(
                data = chartData.incomeData,
                color = Color(0xFF4CAF50),
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                maxValue = maxValue,
                strokeWidth = 3.dp.toPx()
            )
            
            // Draw expense line
            drawLine(
                data = chartData.expenseData,
                color = Color(0xFFF44336),
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                maxValue = maxValue,
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawGridLines(
    chartWidth: Float,
    chartHeight: Float,
    padding: Float,
    maxValue: Double
) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val strokeWidth = 1.dp.toPx()
    
    // Horizontal grid lines
    for (i in 0..4) {
        val y = padding + (chartHeight * i / 4)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = strokeWidth
        )
    }
    
    // Vertical grid lines
    for (i in 0..4) {
        val x = padding + (chartWidth * i / 4)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawLine(
    data: List<Double>,
    color: Color,
    chartWidth: Float,
    chartHeight: Float,
    padding: Float,
    maxValue: Double,
    strokeWidth: Float
) {
    if (data.isEmpty()) return
    
    val points = data.mapIndexed { index, value ->
        val x = padding + (chartWidth * index / (data.size - 1))
        val y = padding + chartHeight - (chartHeight * value.toFloat() / maxValue.toFloat())
        Offset(x, y)
    }
    
    // Draw line
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = strokeWidth
        )
    }
    
    // Draw points
    points.forEach { point ->
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = point
        )
    }
}

@Composable
fun PieChart(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Income vs Expense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Canvas(
                modifier = Modifier
                    .size(200.dp)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = minOf(size.width, size.height) / 2 - 20.dp.toPx()
                
                val total = income + expense
                if (total > 0) {
                    val incomeAngle = (income / total * 360f).toFloat()
                    val expenseAngle = (expense / total * 360f).toFloat()
                    
                    // Draw income slice
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = incomeAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    // Draw expense slice
                    drawArc(
                        color = Color(0xFFF44336),
                        startAngle = -90f + incomeAngle,
                        sweepAngle = expenseAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendItem(
                    color = Color(0xFF4CAF50),
                    label = "Income (${String.format("%.1f", income)})"
                )
                LegendItem(
                    color = Color(0xFFF44336),
                    label = "Expense (${String.format("%.1f", expense)})"
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BarChart(
    chartData: ChartData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.labels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val padding = 40.dp.toPx()
                    
                    val chartWidth = canvasWidth - 2 * padding
                    val chartHeight = canvasHeight - 2 * padding
                    
                    val maxValue = maxOf(
                        chartData.incomeData.maxOrNull() ?: 0.0,
                        chartData.expenseData.maxOrNull() ?: 0.0
                    )
                    
                    if (maxValue > 0) {
                        val barWidth = chartWidth / (chartData.labels.size * 2)
                        val spacing = barWidth * 0.2f
                        
                        chartData.labels.forEachIndexed { index, _ ->
                            val x = padding + (chartWidth * index / chartData.labels.size)
                            
                            // Income bar
                            val incomeHeight = (chartData.incomeData[index] / maxValue * chartHeight).toFloat()
                            drawRect(
                                color = Color(0xFF4CAF50),
                                topLeft = Offset(x, padding + chartHeight - incomeHeight),
                                size = Size(barWidth - spacing, incomeHeight)
                            )
                            
                            // Expense bar
                            val expenseHeight = (chartData.expenseData[index] / maxValue * chartHeight).toFloat()
                            drawRect(
                                color = Color(0xFFF44336),
                                topLeft = Offset(x + barWidth, padding + chartHeight - expenseHeight),
                                size = Size(barWidth - spacing, expenseHeight)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(
                        color = Color(0xFF4CAF50),
                        label = "Income"
                    )
                    LegendItem(
                        color = Color(0xFFF44336),
                        label = "Expense"
                    )
                }
            }
        }
    }
}

