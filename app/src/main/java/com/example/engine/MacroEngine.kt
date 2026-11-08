package com.example.engine

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MacroEngine(
    initialScenario: TimelineScenario = PredefinedScenarios.STAGFLATION_1970S
) {
    private var scenario: TimelineScenario = initialScenario
    private var currentTick: Int = 0

    private var repoRate: Double = initialScenario.initialRepoRate
    private var inflation: Double = initialScenario.initialInflation
    private var capitalGainsTax: Double = initialScenario.initialCapitalGainsTax
    private var regimeName: String = "Normal Cyclical Conditions"
    private var regimeDescription: String = "Macroeconomic climate reacting to global directive injectors."

    private val assetBalances = mutableMapOf<AssetType, Double>()
    private val activeDirectives = mutableListOf<GovernanceDirective>()
    private val netWorthHistory = mutableListOf<Double>()
    private val logs = mutableListOf<GameEventLog>()

    private var debtPrincipal: Double = initialScenario.initialDebt
    private var debtSecondsRemaining: Int = 12
    private var debtStrikes: Int = 0
    private val maxStrikes: Int = 3

    private var peakNetWorth: Double = 0.0
    private var gameStatus: GameStatus = GameStatus.RUNNING

    private var ticksSinceLastDirective: Int = 0
    private var nextDirectiveInterval: Int = 16 // random between 14-22 ticks
    private var adaptiveResilienceScore: Int = 100

    init {
        loadScenario(initialScenario)
    }

    fun loadScenario(newScenario: TimelineScenario) {
        scenario = newScenario
        currentTick = 0
        repoRate = newScenario.initialRepoRate
        inflation = newScenario.initialInflation
        capitalGainsTax = newScenario.initialCapitalGainsTax
        debtPrincipal = newScenario.initialDebt
        debtSecondsRemaining = 12
        debtStrikes = 0
        gameStatus = GameStatus.RUNNING
        ticksSinceLastDirective = 0
        nextDirectiveInterval = 14
        adaptiveResilienceScore = 100
        activeDirectives.clear()
        logs.clear()
        netWorthHistory.clear()

        assetBalances.clear()
        AssetType.values().forEach { type ->
            assetBalances[type] = newScenario.initialAllocations[type] ?: 0.0
        }

        updateRegimeMetadata()
        val total = calculateTotalNetWorth()
        peakNetWorth = total
        netWorthHistory.add(total)

        addLog(
            title = "Timeline Initiated: ${newScenario.name}",
            detail = "Starting Capital: \$${formatCurrency(total)} | Debt: \$${formatCurrency(debtPrincipal)}",
            isPositive = true
        )
    }

    fun restoreSavedState(
        savedScenario: TimelineScenario,
        tick: Int,
        savedDebtPrincipal: Double,
        balances: Map<AssetType, Double>
    ) {
        scenario = savedScenario
        currentTick = tick
        debtPrincipal = savedDebtPrincipal
        debtSecondsRemaining = 12
        debtStrikes = 0
        gameStatus = GameStatus.RUNNING
        activeDirectives.clear()
        assetBalances.clear()
        AssetType.values().forEach { type ->
            assetBalances[type] = balances[type] ?: 0.0
        }
        updateRegimeMetadata()
        val nw = calculateTotalNetWorth()
        if (nw > peakNetWorth) peakNetWorth = nw
        netWorthHistory.add(nw)
        addLog(
            title = "Session Resumed: ${savedScenario.name}",
            detail = "Restored from Month $tick | Net Worth: \$${formatCurrency(nw)}",
            isPositive = true
        )
    }

    fun tick(): MacroShiftUiState {
        if (gameStatus != GameStatus.RUNNING) {
            return buildUiState()
        }

        currentTick++
        ticksSinceLastDirective++

        // 1. Process active directives expiration and tick countdown
        val iterator = activeDirectives.iterator()
        while (iterator.hasNext()) {
            val directive = iterator.next()
            val remaining = directive.remainingTicks - 1
            if (remaining <= 0) {
                // Revert directive rate impacts
                repoRate = max(0.005, repoRate - (directive.repoRateDeltaBps / 10000.0))
                inflation = max(0.005, inflation - (directive.inflationDeltaBps / 10000.0))
                capitalGainsTax = max(0.10, capitalGainsTax - directive.capitalGainsTaxDelta)
                iterator.remove()
                addLog(
                    title = "Directive Expired: ${directive.headline}",
                    detail = "Temporary macro pressures subsided.",
                    isPositive = true
                )
            } else {
                val index = activeDirectives.indexOf(directive)
                if (index != -1) {
                    activeDirectives[index] = directive.copy(remainingTicks = remaining)
                }
            }
        }

        // 2. Automated random "Governance Directive Injector" check
        if (ticksSinceLastDirective >= nextDirectiveInterval) {
            injectRandomDirective()
            ticksSinceLastDirective = 0
            nextDirectiveInterval = Random.nextInt(14, 24)
        }

        // 3. Compute baseline yield and apply compound tick growth
        val currentEffectiveYields = mutableMapOf<AssetType, Double>()
        AssetType.values().forEach { type ->
            val apr = computeEffectiveApr(type)
            currentEffectiveYields[type] = apr

            // Compound per-tick rate (1 game tick = 1 month of yield equivalent)
            val tickRate = apr / 12.0
            val oldBal = assetBalances[type] ?: 0.0
            val gain = oldBal * tickRate
            assetBalances[type] = max(0.0, oldBal + gain)
        }

        // 4. Inflation drag on Liquid Cash
        // Real inflation erodes the value of cash left unhedged
        val cashBal = assetBalances[AssetType.CASH] ?: 0.0
        val monthlyInflation = inflation / 12.0
        if (monthlyInflation > 0.003) {
            val drag = cashBal * (monthlyInflation * 0.4) // purchasing drag
            assetBalances[AssetType.CASH] = max(0.0, cashBal - drag)
        }

        // 5. Debt Service Countdown and deduction
        debtSecondsRemaining--
        val monthlyDebtDue = calculateMonthlyDebtDue()
        if (debtSecondsRemaining <= 0) {
            debtSecondsRemaining = 12
            val currentCash = assetBalances[AssetType.CASH] ?: 0.0
            if (currentCash >= monthlyDebtDue) {
                assetBalances[AssetType.CASH] = currentCash - monthlyDebtDue
                addLog(
                    title = "Debt Service Paid",
                    detail = "Settled coupon: \$${formatCurrency(monthlyDebtDue)} from Liquid Cash.",
                    isPositive = true
                )
            } else {
                // Default strike!
                debtStrikes++
                val shortfall = monthlyDebtDue - currentCash
                assetBalances[AssetType.CASH] = 0.0
                // Penalty added to principal
                debtPrincipal += shortfall * 1.25
                addLog(
                    title = "DEFAULT STRIKE $debtStrikes / $maxStrikes!",
                    detail = "Insufficient cash to service \$${formatCurrency(monthlyDebtDue)} liability! \$${formatCurrency(shortfall * 1.25)} penalized into debt.",
                    isPositive = false
                )

                if (debtStrikes >= maxStrikes) {
                    gameStatus = GameStatus.BANKRUPT
                    addLog(
                        title = "SOVEREIGN BANKRUPTCY DECLARED",
                        detail = "Inability to service debt liabilities resulted in total insolvency.",
                        isPositive = false
                    )
                }
            }
        }

        // 6. Net worth check
        val netWorth = calculateTotalNetWorth()
        if (netWorth > peakNetWorth) {
            peakNetWorth = netWorth
        }
        netWorthHistory.add(netWorth)
        if (netWorthHistory.size > 30) {
            netWorthHistory.removeAt(0)
        }

        if (netWorth <= 0.0) {
            gameStatus = GameStatus.BANKRUPT
        }

        // 7. Update adaptive score
        updateAdaptiveResilience(currentEffectiveYields)
        updateRegimeMetadata()

        return buildUiState()
    }

    private fun injectRandomDirective() {
        val directive = DirectiveInjector.getRandomDirective(repoRate, inflation)
        activeDirectives.add(directive)

        // Apply rate changes
        repoRate = max(0.005, repoRate + (directive.repoRateDeltaBps / 10000.0))
        inflation = max(0.005, inflation + (directive.inflationDeltaBps / 10000.0))
        capitalGainsTax = min(0.60, max(0.10, capitalGainsTax + directive.capitalGainsTaxDelta))

        // Apply instant principal shocks
        directive.assetShocks.forEach { (asset, shockFactor) ->
            val current = assetBalances[asset] ?: 0.0
            val shockAmount = current * shockFactor
            assetBalances[asset] = max(0.0, current + shockAmount)
        }

        addLog(
            title = directive.headline,
            detail = directive.description,
            isPositive = directive.urgency == DirectiveUrgency.MODERATE
        )
    }

    fun triggerManualDirective(directive: GovernanceDirective) {
        val custom = directive.copy(id = "manual_${System.currentTimeMillis()}", remainingTicks = directive.durationTicks)
        activeDirectives.add(custom)
        repoRate = max(0.005, repoRate + (custom.repoRateDeltaBps / 10000.0))
        inflation = max(0.005, inflation + (custom.inflationDeltaBps / 10000.0))
        capitalGainsTax = min(0.60, max(0.10, capitalGainsTax + custom.capitalGainsTaxDelta))

        custom.assetShocks.forEach { (asset, shockFactor) ->
            val current = assetBalances[asset] ?: 0.0
            val shockAmount = current * shockFactor
            assetBalances[asset] = max(0.0, current + shockAmount)
        }

        addLog(
            title = custom.headline,
            detail = custom.description,
            isPositive = custom.urgency == DirectiveUrgency.MODERATE
        )
    }

    fun shiftCapital(from: AssetType, to: AssetType, fraction: Double): Boolean {
        if (from == to || fraction <= 0.0 || fraction > 1.0) return false
        val fromBalance = assetBalances[from] ?: 0.0
        if (fromBalance <= 0.0) return false

        val transferAmount = fromBalance * fraction
        // Exit penalty / slippage based on illiquid assets and capital gains tax if moving out of equities/crypto
        val taxFee = if (from == AssetType.TECH_EQUITIES || from == AssetType.CRYPTO) {
            transferAmount * (capitalGainsTax * 0.15) // applied on gains
        } else 0.0

        val liquidityFriction = transferAmount * ((1.0 - from.liquidityRating) * 0.02)
        val netTransferred = transferAmount - taxFee - liquidityFriction

        assetBalances[from] = max(0.0, fromBalance - transferAmount)
        assetBalances[to] = (assetBalances[to] ?: 0.0) + netTransferred

        addLog(
            title = "Capital Shift: ${from.ticker} -> ${to.ticker}",
            detail = "Shifted \$${formatCurrency(transferAmount)} (${(fraction * 100).toInt()}%). Net added: \$${formatCurrency(netTransferred)}.",
            isPositive = true
        )

        // Give boost to resilience score for proactive shifting
        adaptiveResilienceScore = min(100, adaptiveResilienceScore + 4)
        return true
    }

    fun rebalancePreset(targetAllocations: Map<AssetType, Double>) {
        val totalAssets = assetBalances.values.sum()
        if (totalAssets <= 0.0) return

        targetAllocations.forEach { (type, targetPct) ->
            val targetAmount = totalAssets * targetPct
            assetBalances[type] = targetAmount
        }

        addLog(
            title = "Portfolio Rapid Rebalance",
            detail = "Reallocated total capital across assets to hedge against current regime.",
            isPositive = true
        )
        adaptiveResilienceScore = min(100, adaptiveResilienceScore + 8)
    }

    fun payDownDebt(amount: Double): Boolean {
        val cash = assetBalances[AssetType.CASH] ?: 0.0
        if (amount <= 0.0 || cash < amount || debtPrincipal <= 0.0) return false

        val actualPay = min(amount, debtPrincipal)
        assetBalances[AssetType.CASH] = cash - actualPay
        debtPrincipal -= actualPay

        addLog(
            title = "Debt Principal Reduced",
            detail = "Extinguished \$${formatCurrency(actualPay)} of liabilities. Monthly debt service reduced.",
            isPositive = true
        )
        return true
    }

    fun borrowEmergencyLiquidity(amount: Double): Boolean {
        if (amount <= 0.0) return false
        val cash = assetBalances[AssetType.CASH] ?: 0.0
        assetBalances[AssetType.CASH] = cash + amount
        // Surcharge on emergency borrowing
        debtPrincipal += amount * 1.05

        addLog(
            title = "Emergency Liquidity Drawn",
            detail = "Injected \$${formatCurrency(amount)} into Cash (+5% debt origination penalty).",
            isPositive = false
        )
        return true
    }

    fun computeEffectiveApr(type: AssetType): Double {
        var baseApr = when (type) {
            AssetType.CASH -> max(0.005, repoRate - 0.010) // Cash tracks repo rate minus bank spread
            AssetType.BONDS -> {
                // Fixed coupon + sensitivity to repo rate changes
                val coupon = 0.045
                coupon + (0.050 - repoRate) * 0.8
            }
            AssetType.TECH_EQUITIES -> {
                // Equities yield high in low rates, penalized by high repo rate discount
                0.14 - (repoRate * 0.9) - (capitalGainsTax * 0.1)
            }
            AssetType.COMMODITIES -> {
                // Scales with inflation!
                -0.02 + (inflation * 1.6)
            }
            AssetType.REAL_ESTATE -> {
                // Rental yield minus refinancing cost
                0.08 - (repoRate * 0.6)
            }
            AssetType.CRYPTO -> {
                // High risk high beta
                0.25 - (repoRate * 1.5) - (capitalGainsTax * 0.2)
            }
        }

        // Apply active directive yield modifiers
        activeDirectives.forEach { directive ->
            val mod = directive.assetYieldModifiers[type] ?: 0.0
            baseApr += mod
        }

        return baseApr
    }

    private fun calculateMonthlyDebtDue(): Double {
        // Effective debt APR = Repo Rate + Base Spread (3.5%) + (Inflation / 2)
        val debtApr = repoRate + 0.035 + (inflation * 0.35)
        return (debtPrincipal * debtApr) / 12.0
    }

    private fun calculateTotalNetWorth(): Double {
        val assetsTotal = assetBalances.values.sum()
        return assetsTotal - debtPrincipal
    }

    private fun updateRegimeMetadata() {
        regimeName = when {
            inflation > 0.09 && repoRate > 0.07 -> "Aggressive Stagflation Spiral"
            repoRate >= 0.08 -> "Severe Monetary Tightening (QT)"
            inflation >= 0.08 -> "High Inflationary Inundation"
            repoRate <= 0.025 -> "Ultra-Accommodative Liquidity (QE)"
            debtStrikes >= 2 -> "Debt Default Watchlist"
            else -> "Dynamic Macro Cycling"
        }

        regimeDescription = when {
            inflation > 0.09 -> "Commodities and Gold offer high alpha. Cash purchasing power burning rapidly."
            repoRate >= 0.08 -> "Debt liabilities are extremely punishing! Cash yields are high, Bonds suffering duration drag."
            repoRate <= 0.025 -> "Cheap capital fueling Tech and Crypto speculative rally."
            else -> "Balance risk to maintain debt runway while extracting yield."
        }
    }

    private fun updateAdaptiveResilience(yields: Map<AssetType, Double>) {
        // Check if player has high allocation in negative yielding assets
        val totalAssets = assetBalances.values.sum()
        if (totalAssets <= 0.0) return

        var weightedYield = 0.0
        AssetType.values().forEach { type ->
            val bal = assetBalances[type] ?: 0.0
            val fraction = bal / totalAssets
            weightedYield += fraction * (yields[type] ?: 0.0)
        }

        // Target weighted yield > 0.05
        if (weightedYield > 0.07) {
            adaptiveResilienceScore = min(100, adaptiveResilienceScore + 2)
        } else if (weightedYield < 0.01) {
            adaptiveResilienceScore = max(10, adaptiveResilienceScore - 3)
        }
    }

    private fun addLog(title: String, detail: String, isPositive: Boolean) {
        val entry = GameEventLog(
            id = System.currentTimeMillis() + Random.nextInt(1000),
            tick = currentTick,
            title = title,
            detail = detail,
            isPositive = isPositive
        )
        logs.add(0, entry)
        if (logs.size > 25) {
            logs.removeAt(logs.size - 1)
        }
    }

    fun buildUiState(): MacroShiftUiState {
        val total = calculateTotalNetWorth()
        val totalAssets = assetBalances.values.sum()

        val assetList = AssetType.values().map { type ->
            val bal = assetBalances[type] ?: 0.0
            val apr = computeEffectiveApr(type)
            val allocPct = if (totalAssets > 0) (bal / totalAssets) * 100.0 else 0.0
            AssetBucket(
                type = type,
                balance = bal,
                allocationPercent = allocPct,
                effectiveApr = apr,
                lastTickReturn = apr / 12.0
            )
        }

        return MacroShiftUiState(
            gameStatus = gameStatus,
            currentTick = currentTick,
            totalNetWorth = total,
            peakNetWorth = peakNetWorth,
            netWorthHistory = netWorthHistory.toList(),
            liquidCash = assetBalances[AssetType.CASH] ?: 0.0,
            assets = assetList,
            macro = MacroIndicators(
                repoRateApr = repoRate,
                inflationApr = inflation,
                capitalGainsTax = capitalGainsTax,
                regimeName = regimeName,
                regimeDescription = regimeDescription
            ),
            debt = DebtLiability(
                principal = debtPrincipal,
                baseSpreadApr = 0.035,
                monthlyServiceCost = calculateMonthlyDebtDue(),
                secondsUntilDue = debtSecondsRemaining,
                defaultStrikes = debtStrikes,
                maxStrikesAllowed = maxStrikes
            ),
            activeDirectives = activeDirectives.toList(),
            recentLogs = logs.toList(),
            adaptiveResilienceScore = adaptiveResilienceScore,
            currentScenario = scenario
        )
    }

    private fun formatCurrency(amount: Double): String {
        return String.format("%,.0f", amount)
    }
}
