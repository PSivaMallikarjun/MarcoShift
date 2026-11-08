package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val liquidCash: Double,
    val debtLiabilities: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val currentTurn: Int = 0,
    val totalNetWorth: Double = 0.0,
    val debtPrincipal: Double = 0.0,
    val scenarioId: String = "stagflation_1970s",
    val repoRateApr: Double = 0.05,
    val inflationApr: Double = 0.06,
    val defaultStrikes: Int = 0,
    val isBankrupt: Boolean = false
)
