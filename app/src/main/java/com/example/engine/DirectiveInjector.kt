package com.example.engine

import com.example.model.AssetType
import com.example.model.DirectiveUrgency
import com.example.model.GovernanceDirective
import kotlin.random.Random

object DirectiveInjector {

    private val DIRECTIVE_POOL = listOf(
        GovernanceDirective(
            id = "cb_rate_hike_250",
            headline = "National Central Bank Raises Repo Rates by 250 bps",
            description = "Emergency MPC decree to curb stubborn consumer prices. Benchmark rate surges +2.50%. Sovereign long bonds experience massive capital loss (-20%). Tech multiples compress.",
            urgency = DirectiveUrgency.CRITICAL,
            repoRateDeltaBps = 250,
            inflationDeltaBps = -50,
            assetShocks = mapOf(
                AssetType.BONDS to -0.19,
                AssetType.TECH_EQUITIES to -0.09,
                AssetType.REAL_ESTATE to -0.08
            ),
            assetYieldModifiers = mapOf(
                AssetType.CASH to 0.025,
                AssetType.BONDS to 0.015
            ),
            durationTicks = 24
        ),
        GovernanceDirective(
            id = "tax_overhaul_sweeping",
            headline = "Sweeping Capital Gains Tax Overhaul Introduced",
            description = "Parliament passes a 40% retroactive windfall tax on high-beta digital assets and speculative equities. Heavy liquidation wave observed.",
            urgency = DirectiveUrgency.HIGH,
            repoRateDeltaBps = 0,
            inflationDeltaBps = 20,
            capitalGainsTaxDelta = 0.15,
            assetShocks = mapOf(
                AssetType.TECH_EQUITIES to -0.14,
                AssetType.CRYPTO to -0.28
            ),
            assetYieldModifiers = mapOf(
                AssetType.COMMODITIES to 0.04
            ),
            durationTicks = 20
        ),
        GovernanceDirective(
            id = "geopolitical_crude_embargo",
            headline = "Geopolitical Strait Chokepoint: Crude Spikes 40%",
            description = "Strait closure triggers severe supply shock. Energy and raw industrial commodities moon. CPI inflation jumps +300 bps across supply chains.",
            urgency = DirectiveUrgency.CRITICAL,
            repoRateDeltaBps = 75,
            inflationDeltaBps = 320,
            assetShocks = mapOf(
                AssetType.COMMODITIES to 0.36,
                AssetType.TECH_EQUITIES to -0.06
            ),
            assetYieldModifiers = mapOf(
                AssetType.COMMODITIES to 0.12,
                AssetType.CASH to -0.04 // real erosion
            ),
            durationTicks = 22
        ),
        GovernanceDirective(
            id = "emergency_qe_slashing",
            headline = "Emergency Rate Cut of 300 bps + QE Bazooka",
            description = "Financial crisis intervention: Central Bank slashes repo rate by 300 bps and resumes asset purchases. Sovereign bonds skyrocket in capital value; Tech & Crypto rally.",
            urgency = DirectiveUrgency.HIGH,
            repoRateDeltaBps = -300,
            inflationDeltaBps = 80,
            assetShocks = mapOf(
                AssetType.BONDS to 0.22,
                AssetType.TECH_EQUITIES to 0.18,
                AssetType.CRYPTO to 0.35
            ),
            assetYieldModifiers = mapOf(
                AssetType.CASH to -0.03
            ),
            durationTicks = 25
        ),
        GovernanceDirective(
            id = "reit_refinance_crunch",
            headline = "Commercial Property Refinancing Debt Cascade",
            description = "Regional banking defaults trigger commercial mortgage freeze. REIT dividends suspended and property asset values downgraded.",
            urgency = DirectiveUrgency.HIGH,
            repoRateDeltaBps = 50,
            inflationDeltaBps = -30,
            assetShocks = mapOf(
                AssetType.REAL_ESTATE to -0.26
            ),
            assetYieldModifiers = mapOf(
                AssetType.REAL_ESTATE to -0.06
            ),
            durationTicks = 18
        ),
        GovernanceDirective(
            id = "fiat_devaluation_spiral",
            headline = "Sovereign Debt Downgrade & Currency Devaluation",
            description = "Credit rating agency slashes sovereign outlook. Domestic fiat devalued by 15%. Liquid cash purchasing power burns; Gold and Bitcoin surge as flight to hard assets.",
            urgency = DirectiveUrgency.CATASTROPHIC,
            repoRateDeltaBps = 150,
            inflationDeltaBps = 450,
            assetShocks = mapOf(
                AssetType.CASH to -0.12,
                AssetType.COMMODITIES to 0.42,
                AssetType.CRYPTO to 0.30,
                AssetType.BONDS to -0.15
            ),
            assetYieldModifiers = mapOf(
                AssetType.COMMODITIES to 0.15
            ),
            durationTicks = 20
        ),
        GovernanceDirective(
            id = "crypto_regulatory_ban",
            headline = "Global Regulatory Crackdown on High-Beta Venture",
            description = "International taskforce imposes strict AML reserves and bans unregistered leveraged staking protocols. Liquidity drains instantly into Sovereign Paper.",
            urgency = DirectiveUrgency.HIGH,
            repoRateDeltaBps = 0,
            inflationDeltaBps = -20,
            assetShocks = mapOf(
                AssetType.CRYPTO to -0.38,
                AssetType.BONDS to 0.08
            ),
            assetYieldModifiers = mapOf(
                AssetType.CRYPTO to -0.15
            ),
            durationTicks = 16
        ),
        GovernanceDirective(
            id = "tech_productivity_breakthrough",
            headline = "National Industrial AI & Automation Renaissance",
            description = "Productivity metrics surge to 40-year highs. Tech sector revenues explode; deflationary technological dividend dampens CPI pressures.",
            urgency = DirectiveUrgency.MODERATE,
            repoRateDeltaBps = -50,
            inflationDeltaBps = -120,
            assetShocks = mapOf(
                AssetType.TECH_EQUITIES to 0.28
            ),
            assetYieldModifiers = mapOf(
                AssetType.TECH_EQUITIES to 0.08
            ),
            durationTicks = 24
        )
    )

    fun getRandomDirective(currentRepoRate: Double, currentInflation: Double): GovernanceDirective {
        // Pick a directive with contextual weight or random
        val template = DIRECTIVE_POOL.random()
        // Ensure repo rate doesn't go below 0.5%
        val adjustedRepoDelta = if (currentRepoRate + (template.repoRateDeltaBps / 10000.0) < 0.005) {
            -((currentRepoRate - 0.005) * 10000).toInt()
        } else {
            template.repoRateDeltaBps
        }

        return template.copy(
            id = "${template.id}_${System.currentTimeMillis()}",
            repoRateDeltaBps = adjustedRepoDelta,
            remainingTicks = template.durationTicks
        )
    }

    fun getAllDirectivesForCatalog(): List<GovernanceDirective> = DIRECTIVE_POOL
}
