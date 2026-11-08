package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_turn_history",
    indices = [Index(value = ["sessionId", "tick"])]
)
data class GameTurnHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val turnId: Long = 0,
    val sessionId: String,
    val tick: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val totalNetWorth: Double,
    val liquidCash: Double,
    val debtPrincipal: Double,
    val monthlyDebtService: Double,
    val repoRateApr: Double,
    val inflationApr: Double,
    val activeDirectiveHeadline: String? = null,
    val eventDescription: String? = null,
    val actionTaken: String? = null
)
