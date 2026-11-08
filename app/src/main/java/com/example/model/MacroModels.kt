package com.example.model

enum class AssetType(
    val displayName: String,
    val ticker: String,
    val description: String,
    val durationYears: Double,
    val liquidityRating: Double // 1.0 = instant, 0.2 = slow/haircut
) {
    CASH(
        displayName = "Liquid Cash & T-Bills",
        ticker = "USD/BIL",
        description = "Risk-free nominal yield, but directly eroded by inflation. Mandatory to service debt!",
        durationYears = 0.0,
        liquidityRating = 1.0
    ),
    BONDS(
        displayName = "Sovereign Long Bonds",
        ticker = "UST-20Y",
        description = "Fixed coupon yields. Highly vulnerable to repo rate hikes (capital drop = -Duration * Rate Delta).",
        durationYears = 8.5,
        liquidityRating = 0.85
    ),
    TECH_EQUITIES(
        displayName = "Growth Tech Equities",
        ticker = "QQQ/TECH",
        description = "High growth beta (+15-30%), but crushed by high discount rates and capital gains tax hikes.",
        durationYears = 3.0,
        liquidityRating = 0.80
    ),
    COMMODITIES(
        displayName = "Commodities & Gold",
        ticker = "GLD/OIL",
        description = "Ultimate hedge against runaway inflation and geopolitical shocks. Negative carry in disinflation.",
        durationYears = 0.5,
        liquidityRating = 0.70
    ),
    REAL_ESTATE(
        displayName = "Commercial REITs",
        ticker = "VNQ/REIT",
        description = "Steady rental yields (+7-10%), but heavily penalized by rising debt refinancing rates.",
        durationYears = 5.0,
        liquidityRating = 0.40
    ),
    CRYPTO(
        displayName = "Venture & Digital Assets",
        ticker = "BTC/SOL",
        description = "Extreme upside potential (+50-100%), but hyper-sensitive to liquidity freezes and regulatory crackdowns.",
        durationYears = 1.0,
        liquidityRating = 0.65
    )
}

data class AssetBucket(
    val type: AssetType,
    val balance: Double,
    val allocationPercent: Double = 0.0,
    val effectiveApr: Double, // dynamic annualized yield
    val lastTickReturn: Double = 0.0,
    val totalGainLoss: Double = 0.0
)

data class MacroIndicators(
    val repoRateApr: Double, // Central bank benchmark rate (e.g. 0.055 = 5.50%)
    val inflationApr: Double, // CPI YoY (e.g. 0.072 = 7.20%)
    val capitalGainsTax: Double, // e.g. 0.20 = 20%
    val regimeName: String,
    val regimeDescription: String,
    val liquidityIndex: Double = 1.0 // 1.0 normal, < 0.8 crunch
)

data class DebtLiability(
    val principal: Double,
    val baseSpreadApr: Double = 0.035, // 3.5% risk premium over repo
    val monthlyServiceCost: Double,
    val secondsUntilDue: Int,
    val cycleLengthSeconds: Int = 12,
    val defaultStrikes: Int = 0,
    val maxStrikesAllowed: Int = 3
) {
    val effectiveApr: Double
        get() = baseSpreadApr

    val isDangerZone: Boolean
        get() = defaultStrikes >= 2 || secondsUntilDue <= 3
}

enum class DirectiveUrgency {
    MODERATE,
    HIGH,
    CRITICAL,
    CATASTROPHIC
}

data class GovernanceDirective(
    val id: String,
    val headline: String,
    val description: String,
    val urgency: DirectiveUrgency,
    val repoRateDeltaBps: Int, // e.g. +250 or -300
    val inflationDeltaBps: Int,
    val capitalGainsTaxDelta: Double = 0.0,
    val assetShocks: Map<AssetType, Double> = emptyMap(), // immediate principal % shock
    val assetYieldModifiers: Map<AssetType, Double> = emptyMap(), // additive APR
    val durationTicks: Int = 20,
    val remainingTicks: Int = 20,
    val timestampFormatted: String = ""
)

data class TimelineScenario(
    val id: String,
    val name: String,
    val era: String,
    val subtitle: String,
    val description: String,
    val initialCash: Double,
    val initialDebt: Double,
    val initialRepoRate: Double,
    val initialInflation: Double,
    val initialCapitalGainsTax: Double,
    val tickIntervalMs: Long = 1000L,
    val initialAllocations: Map<AssetType, Double>
)

enum class GameStatus {
    NOT_STARTED,
    RUNNING,
    PAUSED,
    BANKRUPT,
    VICTORY
}

data class GameEventLog(
    val id: Long,
    val tick: Int,
    val title: String,
    val detail: String,
    val isPositive: Boolean
)

data class MacroShiftUiState(
    val gameStatus: GameStatus = GameStatus.RUNNING,
    val currentTick: Int = 0,
    val totalNetWorth: Double = 100_000.0,
    val peakNetWorth: Double = 100_000.0,
    val netWorthHistory: List<Double> = emptyList(),
    val liquidCash: Double = 30_000.0,
    val assets: List<AssetBucket> = emptyList(),
    val macro: MacroIndicators = MacroIndicators(
        repoRateApr = 0.0525,
        inflationApr = 0.0640,
        capitalGainsTax = 0.20,
        regimeName = "Sticky Inflationary Plateau",
        regimeDescription = "Central Bank remains hawkish. Cash is losing real purchasing power."
    ),
    val debt: DebtLiability = DebtLiability(
        principal = 80_000.0,
        baseSpreadApr = 0.035,
        monthlyServiceCost = 650.0,
        secondsUntilDue = 12
    ),
    val activeDirectives: List<GovernanceDirective> = emptyList(),
    val recentLogs: List<GameEventLog> = emptyList(),
    val adaptiveResilienceScore: Int = 100, // 0 - 100
    val currentScenario: TimelineScenario = PredefinedScenarios.STAGFLATION_1970S,
    val simulationSpeed: Float = 1.0f,
    val bannerDismissedId: String? = null
)

object PredefinedScenarios {
    val STAGFLATION_1970S = TimelineScenario(
        id = "stagflation_1970s",
        name = "1970s Great Stagflation",
        era = "Volcker Rate Shock Era",
        subtitle = "Runaway CPI, Oil Embargoes & 20% Repo Rates",
        description = "Inflation spirals relentlessly. The Central Bank aggressively raises rates in 250-400 bps tranches. Long bonds face massive capital destruction. Gold and commodities skyrocket. Static capital will be crushed.",
        initialCash = 35_000.0,
        initialDebt = 90_000.0,
        initialRepoRate = 0.085,
        initialInflation = 0.125,
        initialCapitalGainsTax = 0.25,
        initialAllocations = mapOf(
            AssetType.CASH to 35_000.0,
            AssetType.BONDS to 30_000.0,
            AssetType.TECH_EQUITIES to 25_000.0,
            AssetType.COMMODITIES to 20_000.0,
            AssetType.REAL_ESTATE to 15_000.0,
            AssetType.CRYPTO to 5_000.0
        )
    )

    val LIQUIDITY_CRISIS_2008 = TimelineScenario(
        id = "liquidity_2008",
        name = "2008 Liquidity Cascade",
        era = "Subprime & Credit Freeze",
        subtitle = "Collateral Haircuts & Interbank Paralyzation",
        description = "Credit markets freeze. Real Estate values plunge. Commercial debt refinancing requires huge cash infusions. Only rapid shift to Sovereign Bonds and Liquid Reserves will protect you.",
        initialCash = 20_000.0,
        initialDebt = 140_000.0,
        initialRepoRate = 0.050,
        initialInflation = 0.038,
        initialCapitalGainsTax = 0.18,
        initialAllocations = mapOf(
            AssetType.CASH to 20_000.0,
            AssetType.BONDS to 15_000.0,
            AssetType.TECH_EQUITIES to 35_000.0,
            AssetType.COMMODITIES to 10_000.0,
            AssetType.REAL_ESTATE to 50_000.0,
            AssetType.CRYPTO to 5_000.0
        )
    )

    val SOVEREIGN_DEBT_2026 = TimelineScenario(
        id = "sovereign_2026",
        name = "2026 Sovereign Shock",
        era = "AI Bubble & Debt Overhang",
        subtitle = "Aggressive Tax Directives & Crypto Volatility",
        description = "Governments face historic fiscal deficits, introducing sudden capital gains overhauls and windfall taxes on Tech and Crypto. Fast yields reward lightning-fast capital rotation.",
        initialCash = 40_000.0,
        initialDebt = 110_000.0,
        initialRepoRate = 0.0475,
        initialInflation = 0.055,
        initialCapitalGainsTax = 0.22,
        initialAllocations = mapOf(
            AssetType.CASH to 40_000.0,
            AssetType.BONDS to 20_000.0,
            AssetType.TECH_EQUITIES to 45_000.0,
            AssetType.COMMODITIES to 15_000.0,
            AssetType.REAL_ESTATE to 20_000.0,
            AssetType.CRYPTO to 25_000.0
        )
    )

    val PROCEDURAL_CHAOS = TimelineScenario(
        id = "chaos_infinite",
        name = "Endless Macro Chaos",
        era = "Dynamic Macro Engine",
        subtitle = "Unpredictable Geopolitical Directives",
        description = "Algorithmic governance shocks fire off at high speed. Survive as many fiscal quarters as possible by anticipating policy pivots.",
        initialCash = 50_000.0,
        initialDebt = 100_000.0,
        initialRepoRate = 0.045,
        initialInflation = 0.045,
        initialCapitalGainsTax = 0.20,
        initialAllocations = mapOf(
            AssetType.CASH to 50_000.0,
            AssetType.BONDS to 25_000.0,
            AssetType.TECH_EQUITIES to 30_000.0,
            AssetType.COMMODITIES to 20_000.0,
            AssetType.REAL_ESTATE to 25_000.0,
            AssetType.CRYPTO to 15_000.0
        )
    )

    val ALL = listOf(STAGFLATION_1970S, LIQUIDITY_CRISIS_2008, SOVEREIGN_DEBT_2026, PROCEDURAL_CHAOS)
}
