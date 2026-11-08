package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssetBucket
import com.example.model.AssetType
import com.example.ui.theme.*

enum class VisualizerMode {
    DONUT_CHART,
    STACKED_BARS
}

@Composable
fun AssetAllocationVisualizer(
    assets: List<AssetBucket>,
    totalAssets: Double,
    onSelectAsset: (AssetType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAssetType by remember { mutableStateOf<AssetType?>(null) }
    var mode by remember { mutableStateOf(VisualizerMode.DONUT_CHART) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("asset_allocation_visualizer"),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title, Mode Toggle, and Selected Asset Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Allocation Visualizer",
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "REAL-TIME ALLOCATION VISUALIZER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                // Mode toggle (Donut vs Bar)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceVariantDark)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { mode = VisualizerMode.DONUT_CHART },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = "Donut View",
                            tint = if (mode == VisualizerMode.DONUT_CHART) CyanNeon else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = { mode = VisualizerMode.STACKED_BARS },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar View",
                            tint = if (mode == VisualizerMode.STACKED_BARS) CyanNeon else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (mode == VisualizerMode.DONUT_CHART) {
                // DONUT CHART VIEW WITH CENTER HERO METRIC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Canvas Box
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .testTag("donut_chart_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChartCanvas(
                            assets = assets,
                            totalAssets = totalAssets,
                            selectedAsset = selectedAssetType,
                            onSliceClick = { selectedAssetType = it }
                        )

                        // Center dynamic text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val activeAsset = assets.find { it.type == selectedAssetType }
                            if (activeAsset != null) {
                                Text(
                                    text = activeAsset.type.ticker,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = getAssetTintColor(activeAsset.type),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${String.format("%.1f", activeAsset.allocationPercent)}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            } else {
                                Text(
                                    text = "TOTAL ASSETS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "\$${formatLargeNumber(totalAssets)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Dynamic Legend List
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        assets.forEach { asset ->
                            val isSelected = asset.type == selectedAssetType
                            val color = getAssetTintColor(asset.type)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) SurfaceVariantDark else Color.Transparent)
                                    .clickable {
                                        selectedAssetType = if (isSelected) null else asset.type
                                        onSelectAsset(asset.type)
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
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
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = asset.type.ticker,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) color else TextPrimary,
                                        fontSize = 10.sp
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "\$${formatLargeNumber(asset.balance)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "${String.format("%.1f", asset.allocationPercent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // STACKED ALLOCATION BARS VIEW
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Unified Stacked Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(SurfaceVariantDark)
                    ) {
                        assets.filter { it.allocationPercent > 0.5 }.forEach { asset ->
                            val weight = (asset.allocationPercent / 100.0).toFloat()
                            val color = getAssetTintColor(asset.type)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weight.coerceAtLeast(0.01f))
                                    .background(color)
                                    .clickable {
                                        selectedAssetType = asset.type
                                        onSelectAsset(asset.type)
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Detailed horizontal asset breakdown rows
                    assets.chunked(2).forEach { rowAssets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowAssets.forEach { asset ->
                                val color = getAssetTintColor(asset.type)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectAsset(asset.type) },
                                    color = SurfaceVariantDark,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, BorderSubtle)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = asset.type.ticker,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                        Text(
                                            text = "${String.format("%.1f", asset.allocationPercent)}%",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChartCanvas(
    assets: List<AssetBucket>,
    totalAssets: Double,
    selectedAsset: AssetType?,
    onSliceClick: (AssetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeWidth = 18.dp

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        if (totalAssets <= 0.0) return@Canvas

        var startAngle = -90f
        val gapAngle = 2.5f

        assets.forEach { asset ->
            val sweepAngle = ((asset.balance / totalAssets) * 360f).toFloat()
            if (sweepAngle > 0.5f) {
                val isSelected = asset.type == selectedAsset
                val effectiveSweep = (sweepAngle - gapAngle).coerceAtLeast(0.5f)
                val color = getAssetTintColor(asset.type)

                drawArc(
                    color = if (isSelected) color else color.copy(alpha = 0.85f),
                    startAngle = startAngle + (gapAngle / 2f),
                    sweepAngle = effectiveSweep,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = size,
                    style = Stroke(
                        width = if (isSelected) strokeWidth.toPx() + 6f else strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
            startAngle += sweepAngle
        }
    }
}

private fun formatLargeNumber(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.1fk", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
