package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_balances",
    indices = [Index(value = ["sessionId", "assetType"], unique = true)]
)
data class AssetBalanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val assetType: String,
    val ticker: String,
    val displayName: String,
    val balance: Double,
    val allocationPercent: Double,
    val effectiveApr: Double,
    val lastTickReturn: Double,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
