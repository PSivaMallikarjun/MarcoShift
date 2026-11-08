package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val scenarioId: String,
    val scenarioName: String,
    val currentTick: Int,
    val liquidCash: Double,
    val totalNetWorth: Double,
    val peakNetWorth: Double,
    val repoRateApr: Double,
    val inflationApr: Double,
    val capitalGainsTax: Double,
    val debtPrincipal: Double,
    val debtMonthlyServiceCost: Double,
    val debtSecondsUntilDue: Int,
    val debtDefaultStrikes: Int,
    val regimeName: String,
    val adaptiveResilienceScore: Int,
    val status: String,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
