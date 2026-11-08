package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.SavedGameSlot
import com.example.auth.UserProfile
import com.example.engine.DirectiveInjector
import com.example.model.AssetType
import com.example.model.GameStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MacroShiftViewModel
import com.example.viewmodel.RebalancePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroDashboardScreen(
    viewModel: MacroShiftViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val savedSlot by viewModel.savedSlot.collectAsStateWithLifecycle()
    val showTutorial by viewModel.showTutorial.collectAsStateWithLifecycle()
    val roomHistory by viewModel.roomHistoryFlow.collectAsStateWithLifecycle()

    var showCapitalShifter by remember { mutableStateOf(false) }
    var shiftInitialSource by remember { mutableStateOf(AssetType.TECH_EQUITIES) }
    var showTimelineDialog by remember { mutableStateOf(false) }
    var showDebtDialog by remember { mutableStateOf(false) }
    var showCodeExportDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val activeDirective = state.activeDirectives.lastOrNull()
    val isDirectiveVisible = activeDirective != null && state.bannerDismissedId != activeDirective.id

    val totalAssets = remember(state.assets) { state.assets.sumOf { it.balance } }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("macro_dashboard_screen"),
        containerColor = BackgroundDark,
        topBar = {
            // PROMINENT PERSISTENT TRACKING BAR
            PersistentTrackingBar(
                totalNetWorth = state.totalNetWorth,
                peakNetWorth = state.peakNetWorth,
                netWorthHistory = state.netWorthHistory,
                liquidCash = state.liquidCash,
                debt = state.debt,
                macro = state.macro,
                resilienceScore = state.adaptiveResilienceScore
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Simulation Control & Facility Bar (includes User Auth & Tutorial triggers)
            item {
                SimulationControlBar(
                    gameStatus = state.gameStatus,
                    simulationSpeed = state.simulationSpeed,
                    currentScenarioName = state.currentScenario.name,
                    currentUser = currentUser,
                    onTogglePlay = { viewModel.togglePause() },
                    onSpeedChange = { viewModel.setSimulationSpeed(it) },
                    onOpenTimeline = { showTimelineDialog = true },
                    onOpenDebt = { showDebtDialog = true },
                    onOpenCodeExport = { showCodeExportDialog = true },
                    onOpenTutorial = { viewModel.toggleTutorial(true) },
                    onOpenAuth = { showAuthDialog = true },
                    onOpenHistory = { showHistoryDialog = true },
                    onInjectRandomShock = {
                        val shock = DirectiveInjector.getRandomDirective(state.macro.repoRateApr, state.macro.inflationApr)
                        viewModel.triggerDirective(shock)
                    }
                )
            }

            // 2. Active Governance Directive Banner
            if (isDirectiveVisible && activeDirective != null) {
                item {
                    GovernanceDirectiveBanner(
                        directive = activeDirective,
                        onDismiss = { viewModel.dismissBanner(activeDirective.id) },
                        onQuickShiftClicked = {
                            shiftInitialSource = AssetType.BONDS
                            showCapitalShifter = true
                        }
                    )
                }
            }

            // 3. REAL-TIME ASSET ALLOCATION VISUALIZER (Interactive Donut & Stacked Bars)
            item {
                AssetAllocationVisualizer(
                    assets = state.assets,
                    totalAssets = totalAssets,
                    onSelectAsset = { selectedType ->
                        shiftInitialSource = selectedType
                        showCapitalShifter = true
                    }
                )
            }

            // 4. Tactical Rebalance Presets Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TACTICAL ROTATION PRESETS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "1-Tap Regime Hedging",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip(
                                label = "Flight to Cash",
                                icon = Icons.Default.Shield,
                                color = EmeraldPositive,
                                onClick = { viewModel.applyPreset(RebalancePreset.SAFETY) },
                                modifier = Modifier.weight(1f)
                            )
                            PresetChip(
                                label = "Commodity Shield",
                                icon = Icons.Default.LocalFireDepartment,
                                color = AmberAlert,
                                onClick = { viewModel.applyPreset(RebalancePreset.INFLATION_SHIELD) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip(
                                label = "Rate Hike Hedge",
                                icon = Icons.Default.TrendingUp,
                                color = CyanNeon,
                                onClick = { viewModel.applyPreset(RebalancePreset.RATE_HIKE_HEDGE) },
                                modifier = Modifier.weight(1f)
                            )
                            PresetChip(
                                label = "Growth Surge",
                                icon = Icons.Default.RocketLaunch,
                                color = VioletVenture,
                                onClick = { viewModel.applyPreset(RebalancePreset.TECH_EUPHORIA) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 5. Asset Buckets Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DYNAMIC ASSET BUCKETS (6)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Penalizes Static Allocation",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAlert,
                        fontSize = 10.sp
                    )
                }
            }

            // 6. Asset Buckets Cards List
            items(state.assets, key = { it.type.name }) { asset ->
                AssetBucketCard(
                    asset = asset,
                    onShiftOut = { fraction ->
                        viewModel.shiftCapital(asset.type, AssetType.CASH, fraction)
                    },
                    onOpenShiftModal = {
                        shiftInitialSource = asset.type
                        showCapitalShifter = true
                    }
                )
            }

            // 7. Macroeconomic Shocks Ledger
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MACRO EVENT & DIRECTIVE LEDGER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Tick: ${state.currentTick}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = CyanNeon,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.recentLogs.isEmpty()) {
                            Text(
                                text = "Waiting for initial governance injections...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        } else {
                            state.recentLogs.take(5).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "[M${log.tick}]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (log.isPositive) TextPrimary else CrimsonNegative
                                        )
                                        Text(
                                            text = log.detail,
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs & Sheets
    if (showCapitalShifter) {
        CapitalShifterSheet(
            assets = state.assets,
            initialSource = shiftInitialSource,
            onDismiss = { showCapitalShifter = false },
            onConfirmShift = { from, to, fraction ->
                viewModel.shiftCapital(from, to, fraction)
            }
        )
    }

    if (showTimelineDialog) {
        TimelineSelectorDialog(
            currentScenario = state.currentScenario,
            onSelectScenario = { scenario ->
                viewModel.switchScenario(scenario)
            },
            onDismiss = { showTimelineDialog = false }
        )
    }

    if (showDebtDialog) {
        DebtLiquidityDialog(
            debt = state.debt,
            liquidCash = state.liquidCash,
            macro = state.macro,
            onPayDownDebt = { viewModel.payDownDebt(it) },
            onDrawEmergencyLiquidity = { viewModel.drawEmergencyLiquidity(it) },
            onDismiss = { showDebtDialog = false }
        )
    }

    if (showCodeExportDialog) {
        CodePrototypeViewerDialog(
            onDismiss = { showCodeExportDialog = false }
        )
    }

    if (showTutorial) {
        InteractiveTutorialDialog(
            onDismiss = { viewModel.toggleTutorial(false) },
            onExecutePracticeShift = { from, to, frac ->
                viewModel.shiftCapital(from, to, frac)
            },
            onTriggerPracticeShock = {
                val shock = DirectiveInjector.getRandomDirective(state.macro.repoRateApr, state.macro.inflationApr)
                viewModel.triggerDirective(shock)
            }
        )
    }

    if (showAuthDialog) {
        AuthProfileDialog(
            currentUser = currentUser,
            savedSlot = savedSlot,
            onSignUp = { u, e, p -> viewModel.signUp(u, e, p) },
            onLogIn = { u, p -> viewModel.logIn(u, p) },
            onLogOut = { viewModel.logOut() },
            onSaveGame = { viewModel.saveCurrentGame() },
            onResumeGame = { slot -> viewModel.resumeGame(slot) },
            onDismiss = { showAuthDialog = false }
        )
    }

    if (showHistoryDialog) {
        HistoricalTurnsDialog(
            turnsHistory = roomHistory,
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (state.gameStatus == GameStatus.BANKRUPT) {
        GameOverDialog(
            ticksSurvived = state.currentTick,
            peakNetWorth = state.peakNetWorth,
            resilienceScore = state.adaptiveResilienceScore,
            scenarioName = state.currentScenario.name,
            onRestart = { viewModel.restartCurrentScenario() },
            onSwitchTimeline = {
                showTimelineDialog = true
            }
        )
    }
}

@Composable
private fun SimulationControlBar(
    gameStatus: GameStatus,
    simulationSpeed: Float,
    currentScenarioName: String,
    currentUser: UserProfile?,
    onTogglePlay: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenDebt: () -> Unit,
    onOpenCodeExport: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenHistory: () -> Unit,
    onInjectRandomShock: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Play/Pause, Speed, and Tutorial Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (gameStatus == GameStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Speed toggle
                Surface(
                    onClick = {
                        val nextSpeed = when (simulationSpeed) {
                            1.0f -> 2.0f
                            2.0f -> 3.0f
                            else -> 1.0f
                        }
                        onSpeedChange(nextSpeed)
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(0.5.dp, BorderSubtle)
                ) {
                    Text(
                        text = "${simulationSpeed.toInt()}x",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                // Tutorial Button
                IconButton(
                    onClick = onOpenTutorial,
                    modifier = Modifier.size(32.dp).testTag("open_tutorial_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Tutorial",
                        tint = AmberAlert,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Center: Timeline Selector Button
            Surface(
                onClick = onOpenTimeline,
                shape = RoundedCornerShape(6.dp),
                color = SurfaceVariantDark,
                border = BorderStroke(0.5.dp, BorderSubtle),
                modifier = Modifier.testTag("select_timeline_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Text(
                        text = currentScenarioName.take(13),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Right: Inject Shock, Debt Facility, Code Export, and User Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Manual Directive Injector Trigger
                IconButton(
                    onClick = onInjectRandomShock,
                    modifier = Modifier.size(30.dp).testTag("inject_shock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Inject Shock",
                        tint = AmberAlert,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Debt Management Facility
                IconButton(
                    onClick = onOpenDebt,
                    modifier = Modifier.size(30.dp).testTag("open_debt_facility_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PriceCheck,
                        contentDescription = "Debt Facility",
                        tint = AmberAlert,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Code & Prototype Export
                IconButton(
                    onClick = onOpenCodeExport,
                    modifier = Modifier.size(30.dp).testTag("open_code_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code Export",
                        tint = CyanNeon,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Room DB Turn History
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.size(30.dp).testTag("open_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Room History",
                        tint = CyanNeon,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // User Profile & Save/Resume Dialog
                Surface(
                    onClick = onOpenAuth,
                    shape = CircleShape,
                    color = if (currentUser != null) CyanNeon.copy(alpha = 0.2f) else SurfaceVariantDark,
                    border = BorderStroke(1.dp, if (currentUser != null) CyanNeon else BorderSubtle),
                    modifier = Modifier.size(30.dp).testTag("open_auth_button")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (currentUser != null) {
                            Text(text = currentUser.avatarEmoji, fontSize = 14.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariantDark,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
