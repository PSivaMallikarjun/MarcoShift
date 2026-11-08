package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AssetBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetBalanceDao {

    @Query("SELECT * FROM asset_balances WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getAssetsForSessionFlow(sessionId: String): Flow<List<AssetBalanceEntity>>

    @Query("SELECT * FROM asset_balances WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun getAssetsForSession(sessionId: String): List<AssetBalanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetBalanceEntity>)

    @Query("DELETE FROM asset_balances WHERE sessionId = :sessionId")
    suspend fun deleteAssetsForSession(sessionId: String)
}
