package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssetBucket
import com.example.model.AssetType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapitalShifterSheet(
    assets: List<AssetBucket>,
    initialSource: AssetType,
    onDismiss: () -> Unit,
    onConfirmShift: (from: AssetType, to: AssetType, fraction: Double) -> Unit
) {
    var sourceAsset by remember { mutableStateOf(initialSource) }
    var targetAsset by remember {
        mutableStateOf(
            if (initialSource == AssetType.CASH) AssetType.COMMODITIES else AssetType.CASH
        )
    }
    var fraction by remember { mutableStateOf(0.5f) } // 50% default

    val sourceBucket = assets.find { it.type == sourceAsset } ?: assets.first()
    val targetBucket = assets.find { it.type == targetAsset } ?: assets.last()
    val sourceBalance = sourceBucket.balance
    val transferAmount = sourceBalance * fraction

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderHighlight) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("capital_shifter_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapCalls,
                        contentDescription = "Shift",
                        tint = CyanNeon
                    )
                    Text(
                        text = "RAPID CAPITAL SHIFTER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Text(
                text = "Reallocate capital to avoid policy shocks, duration penalties, or debt defaults.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Source Asset Selector
            Text(
                text = "SOURCE ASSET (Shift Out From)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assets.take(3).forEach { asset ->
                    AssetSelectButton(
                        asset = asset,
                        isSelected = asset.type == sourceAsset,
                        onClick = {
                            sourceAsset = asset.type
                            if (targetAsset == asset.type) {
                                targetAsset = if (asset.type == AssetType.CASH) AssetType.COMMODITIES else AssetType.CASH
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assets.drop(3).forEach { asset ->
                    AssetSelectButton(
                        asset = asset,
                        isSelected = asset.type == sourceAsset,
                        onClick = {
                            sourceAsset = asset.type
                            if (targetAsset == asset.type) {
                                targetAsset = if (asset.type == AssetType.CASH) AssetType.COMMODITIES else AssetType.CASH
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Destination Asset Selector
            Text(
                text = "DESTINATION ASSET (Shift Capital Into)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assets.take(3).forEach { asset ->
                    AssetSelectButton(
                        asset = asset,
                        isSelected = asset.type == targetAsset,
                        onClick = {
                            targetAsset = asset.type
                        },
                        enabled = asset.type != sourceAsset,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assets.drop(3).forEach { asset ->
                    AssetSelectButton(
                        asset = asset,
                        isSelected = asset.type == targetAsset,
                        onClick = {
                            targetAsset = asset.type
                        },
                        enabled = asset.type != sourceAsset,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Percentage Slider & Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAPITAL PROPORTION: ${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Text(
                    text = "\$${String.format("%,.0f", transferAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Slider(
                value = fraction,
                onValueChange = { fraction = it },
                valueRange = 0.05f..1.0f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon,
                    inactiveTrackColor = SurfaceVariantDark
                ),
                modifier = Modifier.testTag("capital_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.25f, 0.50f, 0.75f, 1.0f).forEach { pct ->
                    OutlinedButton(
                        onClick = { fraction = pct },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (fraction == pct) CyanNeon else BorderSubtle
                        ),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (pct == 1.0f) "MAX" else "${(pct * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (fraction == pct) CyanNeon else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Projected Impact Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = SurfaceVariantDark,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "PROJECTED EXECUTION SUMMARY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${sourceAsset.ticker} New Balance:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "\$${String.format("%,.0f", (sourceBalance - transferAmount).coerceAtLeast(0.0))}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${targetAsset.ticker} New Balance:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "\$${String.format("%,.0f", targetBucket.balance + transferAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPositive
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Shift Button
            Button(
                onClick = {
                    onConfirmShift(sourceAsset, targetAsset, fraction.toDouble())
                    onDismiss()
                },
                enabled = sourceBalance > 0 && sourceAsset != targetAsset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_shift_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EXECUTE CAPITAL SHIFT",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AssetSelectButton(
    asset: AssetBucket,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val tint = getAssetTintColor(asset.type)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) tint.copy(alpha = 0.2f) else SurfaceVariantDark,
        border = BorderStroke(1.dp, if (isSelected) tint else BorderSubtle)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.type.ticker,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (!enabled) TextMuted else if (isSelected) tint else TextPrimary,
                fontSize = 11.sp
            )
        }
    }
}
