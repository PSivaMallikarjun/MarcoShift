package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DirectiveUrgency
import com.example.model.GovernanceDirective
import com.example.ui.theme.*

@Composable
fun GovernanceDirectiveBanner(
    directive: GovernanceDirective,
    onDismiss: () -> Unit,
    onQuickShiftClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (directive.urgency) {
        DirectiveUrgency.CATASTROPHIC -> CrimsonNegative
        DirectiveUrgency.CRITICAL -> CrimsonNegative
        DirectiveUrgency.HIGH -> AmberAlert
        DirectiveUrgency.MODERATE -> CyanNeon
    }

    val infiniteTransition = rememberInfiniteTransition(label = "banner_glow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulse"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("governance_directive_banner"),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        border = BorderStroke(1.5.dp, borderColor.copy(alpha = borderAlpha)),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Directive Urgency & Countdown
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
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Alert",
                        tint = borderColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "GOVERNANCE DIRECTIVE INJECTED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = borderColor,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = BackgroundDark,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${directive.remainingTicks}s remaining",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Headline
            Text(
                text = directive.headline,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = directive.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Macro Rate / Shock Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (directive.repoRateDeltaBps != 0) {
                    ShockChip(
                        label = "REPO",
                        value = "${if (directive.repoRateDeltaBps > 0) "+" else ""}${directive.repoRateDeltaBps} bps",
                        isNegative = directive.repoRateDeltaBps > 0
                    )
                }
                if (directive.inflationDeltaBps != 0) {
                    ShockChip(
                        label = "CPI",
                        value = "${if (directive.inflationDeltaBps > 0) "+" else ""}${directive.inflationDeltaBps} bps",
                        isNegative = directive.inflationDeltaBps > 0
                    )
                }
                if (directive.capitalGainsTaxDelta != 0.0) {
                    ShockChip(
                        label = "TAX",
                        value = "+${(directive.capitalGainsTaxDelta * 100).toInt()}%",
                        isNegative = true
                    )
                }
                directive.assetShocks.entries.take(2).forEach { (asset, shock) ->
                    ShockChip(
                        label = asset.ticker,
                        value = "${if (shock > 0) "+" else ""}${(shock * 100).toInt()}%",
                        isNegative = shock < 0
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onQuickShiftClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("directive_quick_shift_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Shift",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ADAPT / SHIFT CAPITAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("directive_dismiss_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "DISMISS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ShockChip(label: String, value: String, isNegative: Boolean) {
    Surface(
        color = BackgroundDark,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, if (isNegative) CrimsonNegative.copy(alpha = 0.5f) else EmeraldPositive.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) CrimsonNegative else EmeraldPositive
            )
        }
    }
}
