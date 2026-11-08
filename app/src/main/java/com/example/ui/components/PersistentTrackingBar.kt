package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DebtLiability
import com.example.model.MacroIndicators
import com.example.ui.theme.*

@Composable
fun PersistentTrackingBar(
    totalNetWorth: Double,
    peakNetWorth: Double,
    netWorthHistory: List<Double>,
    liquidCash: Double,
    debt: DebtLiability,
    macro: MacroIndicators,
    resilienceScore: Int,
    modifier: Modifier = Modifier
) {
    val isCashLow = liquidCash < debt.monthlyServiceCost
    val isCashTight = liquidCash < (debt.monthlyServiceCost * 2)

    val cashBadgeColor by animateColorAsState(
        targetValue = when {
            isCashLow -> CrimsonNegative
            isCashTight -> AmberAlert
            else -> EmeraldPositive
        },
        label = "cashBadgeColor"
    )

    // Pulse animation for low cash / danger
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dangerPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dangerAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("persistent_tracking_bar"),
        color = SurfaceDark,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, if (isCashLow) CrimsonNegative.copy(alpha = dangerPulse) else BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Macro Regime Ticker & Resilience Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (macro.repoRateApr > 0.07) CrimsonNegative else CyanNeon)
                    )
                    Text(
                        text = macro.regimeName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanNeon,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Resilience Rating
                Surface(
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Resilience",
                            tint = if (resilienceScore > 75) EmeraldPositive else if (resilienceScore > 40) AmberAlert else CrimsonNegative,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "ADAPTIVE: $resilienceScore%",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Column Core Metrics: NET WORTH | LIQUID CASH | DEBT-SERVICING LIABILITIES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Total Net Worth
                Surface(
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("net_worth_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "TOTAL NET WORTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "\$${formatLargeNumber(totalNetWorth)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (totalNetWorth > 0) TextPrimary else CrimsonNegative,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        // Sparkline chart of net worth history
                        SparklineChart(
                            data = netWorthHistory,
                            lineColor = if (totalNetWorth >= peakNetWorth * 0.9) CyanNeon else CrimsonNegative,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        )
                    }
                }

                // 2. Liquid Cash
                Surface(
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("liquid_cash_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCashLow) CrimsonDark.copy(alpha = 0.25f) else SurfaceVariantDark,
                    border = BorderStroke(
                        1.dp,
                        if (isCashLow) CrimsonNegative.copy(alpha = dangerPulse) else BorderSubtle
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LIQUID CASH",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isCashLow) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = CrimsonNegative,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Text(
                            text = "\$${formatLargeNumber(liquidCash)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = cashBadgeColor,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        val runwayMultiplier = if (debt.monthlyServiceCost > 0) liquidCash / debt.monthlyServiceCost else 99.0
                        Text(
                            text = when {
                                isCashLow -> "DEFICIT (<1x Bill!)"
                                runwayMultiplier < 3.0 -> "Runway: ${String.format("%.1f", runwayMultiplier)}x due"
                                else -> "Safe Runway (${String.format("%.0f", runwayMultiplier)}x)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (isCashLow) CrimsonNegative else TextMuted
                        )
                    }
                }

                // 3. Current Inflation-Adjusted Debt-Servicing Liabilities
                Surface(
                    modifier = Modifier
                        .weight(1.15f)
                        .testTag("debt_liability_card"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (debt.isDangerZone) CrimsonDark.copy(alpha = 0.25f) else SurfaceVariantDark,
                    border = BorderStroke(
                        1.dp,
                        if (debt.isDangerZone) CrimsonNegative.copy(alpha = dangerPulse) else BorderSubtle
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "DEBT SERVICING",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "\$${formatLargeNumber(debt.monthlyServiceCost)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = if (debt.defaultStrikes > 0) CrimsonNegative else AmberAlert,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "/bill",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Due in ${debt.secondsUntilDue}s",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (debt.secondsUntilDue <= 3) CrimsonNegative else CyanNeon,
                                fontWeight = FontWeight.Bold
                            )
                            if (debt.defaultStrikes > 0) {
                                Text(
                                    text = "Strike ${debt.defaultStrikes}/${debt.maxStrikesAllowed}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = CrimsonNegative,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-strip: Benchmark Repo Rate & CPI Inflation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(BackgroundDark)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroMetricMini(
                        label = "REPO RATE",
                        value = "${String.format("%.2f", macro.repoRateApr * 100)}%",
                        valueColor = if (macro.repoRateApr > 0.07) CrimsonNegative else CyanNeon
                    )
                    MacroMetricMini(
                        label = "INFLATION (CPI)",
                        value = "${String.format("%.2f", macro.inflationApr * 100)}%",
                        valueColor = if (macro.inflationApr > 0.06) AmberAlert else TextPrimary
                    )
                    MacroMetricMini(
                        label = "DEBT PRINCIPAL",
                        value = "\$${formatLargeNumber(debt.principal)}",
                        valueColor = TextSecondary
                    )
                }

                // Countdown progress bar for debt tick
                val progress = (debt.secondsUntilDue.toFloat() / debt.cycleLengthSeconds.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceVariantDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(if (debt.secondsUntilDue <= 3) CrimsonNegative else CyanNeon)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroMetricMini(label: String, value: String, valueColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SparklineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val minVal = data.minOrNull() ?: 0.0
        val maxVal = (data.maxOrNull() ?: 1.0).coerceAtLeast(minVal + 1.0)
        val range = maxVal - minVal

        val points = data.mapIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1)) * size.width
            val normY = ((value - minVal) / range).toFloat()
            val y = size.height - (normY * size.height)
            Offset(x, y.coerceIn(0f, size.height))
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun formatLargeNumber(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.1fk", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
