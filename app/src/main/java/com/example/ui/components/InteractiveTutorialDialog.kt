package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AssetType
import com.example.ui.theme.*

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val content: String,
    val keyPoints: List<String>,
    val interactiveActionLabel: String? = null
)

@Composable
fun InteractiveTutorialDialog(
    onDismiss: () -> Unit,
    onExecutePracticeShift: (AssetType, AssetType, Double) -> Unit,
    onTriggerPracticeShock: () -> Unit
) {
    var currentStepIndex by remember { mutableStateOf(0) }
    var practiceActionCompleted by remember { mutableStateOf(false) }

    val steps = remember {
        listOf(
            TutorialStep(
                stepNumber = 1,
                title = "CORE MACRO MECHANICS & NET WORTH",
                subtitle = "Survival of the Most Adaptive",
                icon = Icons.Default.AccountBalanceWallet,
                content = "MacroShift models an active macro-economy where every second represents one fiscal month. Your Total Net Worth equals all your asset balances minus your leveraged debt liabilities.",
                keyPoints = listOf(
                    "Asset balances grow or shrink based on real-time annualized yields (APR / 12).",
                    "Unhedged Cash is continuously eroded by real CPI inflation drag.",
                    "Static financial allocations will be rapidly penalized during regime shifts."
                )
            ),
            TutorialStep(
                stepNumber = 2,
                title = "DYNAMIC ASSET BUCKETS & DURATION",
                subtitle = "Mastering Your 6 Capital Instruments",
                icon = Icons.Default.PieChart,
                content = "Capital can be rotated across 6 distinct asset classes, each with unique liquidity, duration, and policy sensitivities:",
                keyPoints = listOf(
                    "Liquid Cash: Risk-free nominal yield, but burns during inflation. Crucial for paying debt!",
                    "Sovereign Bonds (8.5y Duration): When repo rates hike, bond capital drops immediately (-Duration × ΔRate).",
                    "Tech Equities: High baseline alpha (+14%), but compressed by high discount rates and tax hikes.",
                    "Commodities & Gold: Strongest shield during runaway inflation and geopolitical chokepoints.",
                    "REITs & Crypto: Offer high income or explosive beta, but carry refinancing and regulatory risks."
                )
            ),
            TutorialStep(
                stepNumber = 3,
                title = "GOVERNANCE DIRECTIVE SHOCKS",
                subtitle = "Sudden Geopolitical & Central Bank Mandates",
                icon = Icons.Default.Bolt,
                content = "The automated Governance Directive Injector introduces unpredictable policy shifts (e.g., Central Bank +250 bps rate hikes, 40% capital gains tax overhauls, or crude oil embargoes).",
                keyPoints = listOf(
                    "Directives immediately shock underlying asset principal and revalue yields.",
                    "An alert banner displays the policy impact and remaining active duration.",
                    "Use tactical quick presets or the Capital Shifter to rotate into favored assets before insolvency."
                )
            ),
            TutorialStep(
                stepNumber = 4,
                title = "DEBT SERVICING & BANKRUPTCY",
                subtitle = "The Mandatory 12-Second Billing Cycle",
                icon = Icons.Default.Dangerous,
                content = "You carry leveraged debt liabilities requiring recurring coupon payments compounded by (Repo Rate + 3.5% Spread + Inflation Spread).",
                keyPoints = listOf(
                    "Every 12 seconds, debt service is automatically deducted from Liquid Cash.",
                    "If Cash < Payment Due: You receive a Default Strike, and shortfall is penalized into debt principal (+25%).",
                    "Accumulating 3 Default Strikes triggers immediate Sovereign Bankruptcy!"
                )
            ),
            TutorialStep(
                stepNumber = 5,
                title = "FIRST EVENT GUIDED SIMULATION",
                subtitle = "Hands-On Capital Rotation Practice",
                icon = Icons.Default.SportsEsports,
                content = "Put the mechanics to the test right now! Practice your first strategic rebalance and survive a simulated central bank rate shock.",
                keyPoints = listOf(
                    "Practice 1: Shift 25% of Tech Equities into Cash to secure your debt runway.",
                    "Practice 2: Inject a central bank rate hike shock and observe bond yield repricing.",
                    "Practice 3: Observe your resilience agility rating rise as you adapt."
                ),
                interactiveActionLabel = "EXECUTE TACTICAL ROTATION NOW"
            )
        )
    }

    val currentStep = steps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("interactive_tutorial_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "INTERACTIVE TUTORIAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanNeon,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Step ${currentStep.stepNumber} of ${steps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Indicator Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (idx <= currentStepIndex) CyanNeon else SurfaceVariantDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Text(
                        text = currentStep.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanNeon,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    Text(
                        text = currentStep.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Points Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceVariantDark,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentStep.keyPoints.forEach { point ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = EmeraldPositive,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // Interactive practice button on Step 5
                    if (currentStep.stepNumber == 5) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = BackgroundDark,
                            border = BorderStroke(1.dp, if (practiceActionCompleted) EmeraldPositive else AmberAlert)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (practiceActionCompleted) "PRACTICE COMPLETED! YOU'RE READY!" else "LIVE GUIDED ACTION:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (practiceActionCompleted) EmeraldPositive else AmberAlert
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onExecutePracticeShift(AssetType.TECH_EQUITIES, AssetType.CASH, 0.25)
                                            onTriggerPracticeShock()
                                            practiceActionCompleted = true
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyanNeon,
                                            contentColor = BackgroundDark
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Test 25% Shift & Rate Shock",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Controls (Back / Next / Finish)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(text = "Previous", color = TextSecondary)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text(text = "Skip Tutorial", color = TextMuted)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = BackgroundDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = if (currentStepIndex < steps.size - 1) "Next Step" else "Start Playing",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (currentStepIndex < steps.size - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
