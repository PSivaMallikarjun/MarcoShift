package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssetBucket
import com.example.model.AssetType
import com.example.ui.theme.*

@Composable
fun AssetBucketCard(
    asset: AssetBucket,
    onShiftOut: (fraction: Double) -> Unit,
    onOpenShiftModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor = getAssetTintColor(asset.type)
    val isPositiveApr = asset.effectiveApr >= 0.0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("asset_card_${asset.type.name.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header: Ticker, Name, Allocation Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tintColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAssetIcon(asset.type),
                            contentDescription = asset.type.displayName,
                            tint = tintColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = asset.type.ticker,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Surface(
                                color = SurfaceVariantDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", asset.allocationPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = tintColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = asset.type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Balance display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "\$${formatCurrency(asset.balance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    // Yield / APR
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositiveApr) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositiveApr) EmeraldPositive else CrimsonNegative,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (isPositiveApr) "+" else ""}${String.format("%.2f", asset.effectiveApr * 100)}% APR",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositiveApr) EmeraldPositive else CrimsonNegative,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Allocation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceVariantDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((asset.allocationPercent / 100.0).toFloat().coerceIn(0f, 1f))
                        .background(tintColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Quick Actions: Shift 25%, Shift 50%, Shift All / Custom Shift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (asset.type == AssetType.CASH) "Risk-free reserve" else "Duration: ${asset.type.durationYears}y",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (asset.type != AssetType.CASH && asset.balance > 100.0) {
                        ActionChip(
                            label = "Shift 25%",
                            onClick = { onShiftOut(0.25) }
                        )
                        ActionChip(
                            label = "Shift 50%",
                            onClick = { onShiftOut(0.50) }
                        )
                    }

                    ActionChip(
                        label = "Reallocate",
                        onClick = onOpenShiftModal,
                        highlight = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = if (highlight) CyanNeon.copy(alpha = 0.15f) else SurfaceVariantDark,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, if (highlight) CyanNeon else BorderSubtle)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) CyanNeon else TextSecondary,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

fun getAssetIcon(type: AssetType): ImageVector {
    return when (type) {
        AssetType.CASH -> Icons.Default.AccountBalanceWallet
        AssetType.BONDS -> Icons.Default.AccountBalance
        AssetType.TECH_EQUITIES -> Icons.Default.AutoGraph
        AssetType.COMMODITIES -> Icons.Default.LocalFireDepartment
        AssetType.REAL_ESTATE -> Icons.Default.Apartment
        AssetType.CRYPTO -> Icons.Default.CurrencyBitcoin
    }
}

fun getAssetTintColor(type: AssetType): Color {
    return when (type) {
        AssetType.CASH -> AssetCashColor
        AssetType.BONDS -> AssetBondsColor
        AssetType.TECH_EQUITIES -> AssetEquitiesColor
        AssetType.COMMODITIES -> AssetCommoditiesColor
        AssetType.REAL_ESTATE -> AssetRealEstateColor
        AssetType.CRYPTO -> AssetCryptoColor
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount)
}
