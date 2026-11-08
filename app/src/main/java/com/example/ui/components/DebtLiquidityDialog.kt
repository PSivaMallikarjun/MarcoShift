package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DebtLiability
import com.example.model.MacroIndicators
import com.example.ui.theme.*

@Composable
fun DebtLiquidityDialog(
    debt: DebtLiability,
    liquidCash: Double,
    macro: MacroIndicators,
    onPayDownDebt: (amount: Double) -> Unit,
    onDrawEmergencyLiquidity: (amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("debt_liquidity_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, BorderHighlight),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
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
                            imageVector = Icons.Default.PriceCheck,
                            contentDescription = "Debt Management",
                            tint = AmberAlert
                        )
                        Text(
                            text = "DEBT & LIQUIDITY FACILITY",
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
                    text = "Manage your leveraged debt liabilities and emergency liquidity line to prevent bankruptcy strikes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Debt Structure Breakdown
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Outstanding Principal:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(text = "\$${String.format("%,.0f", debt.principal)}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Repo Benchmark Rate:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(text = "${String.format("%.2f", macro.repoRateApr * 100)}%", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = CyanNeon)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Risk Spread + Inflation:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(text = "+${String.format("%.2f", (debt.baseSpreadApr + macro.inflationApr * 0.35) * 100)}%", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = AmberAlert)
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = BorderSubtle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Service Bill (Due every 12s):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "\$${String.format("%,.0f", debt.monthlyServiceCost)}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = AmberAlert)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pay Down Debt Options
                Text(
                    text = "REDUCE DEBT PRINCIPAL (from Liquid Cash: \$${String.format("%,.0f", liquidCash)})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val pay10k = 10_000.0
                    val pay25k = 25_000.0
                    OutlinedButton(
                        onClick = { onPayDownDebt(pay10k) },
                        enabled = liquidCash >= pay10k && debt.principal > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(text = "Pay \$10k", fontSize = 11.sp, color = if (liquidCash >= pay10k) EmeraldPositive else TextMuted)
                    }

                    OutlinedButton(
                        onClick = { onPayDownDebt(pay25k) },
                        enabled = liquidCash >= pay25k && debt.principal > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(text = "Pay \$25k", fontSize = 11.sp, color = if (liquidCash >= pay25k) EmeraldPositive else TextMuted)
                    }

                    Button(
                        onClick = { onPayDownDebt(minOf(liquidCash * 0.75, debt.principal)) },
                        enabled = liquidCash > 500 && debt.principal > 0,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Max Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Credit Line
                Text(
                    text = "EMERGENCY CREDIT INJECTION (+5% Debt Surcharge)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDrawEmergencyLiquidity(15_000.0) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(text = "+ \$15k Cash", fontSize = 11.sp, color = CyanNeon)
                    }

                    OutlinedButton(
                        onClick = { onDrawEmergencyLiquidity(30_000.0) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(text = "+ \$30k Cash", fontSize = 11.sp, color = CyanNeon)
                    }
                }
            }
        }
    }
}
