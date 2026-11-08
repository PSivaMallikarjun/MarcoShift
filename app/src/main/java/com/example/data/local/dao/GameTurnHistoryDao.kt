package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.GameTurnHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameTurnHistoryDao {

    @Query("SELECT * FROM game_turn_history WHERE sessionId = :sessionId ORDER BY tick DESC")
    fun getHistoryForSessionFlow(sessionId: String): Flow<List<GameTurnHistoryEntity>>

    @Query("SELECT * FROM game_turn_history WHERE sessionId = :sessionId ORDER BY tick DESC LIMIT :limit")
    fun getRecentTurnsFlow(sessionId: String, limit: Int = 30): Flow<List<GameTurnHistoryEntity>>

    @Query("SELECT * FROM game_turn_history WHERE sessionId = :sessionId ORDER BY tick DESC")
    suspend fun getAllTurns(sessionId: String): List<GameTurnHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurn(turn: GameTurnHistoryEntity)

    @Query("DELETE FROM game_turn_history WHERE sessionId = :sessionId")
    suspend fun clearHistoryForSession(sessionId: String)
}
