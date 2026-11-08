package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Query("SELECT * FROM game_sessions WHERE sessionId = :sessionId")
    fun getSessionFlow(sessionId: String): Flow<GameSessionEntity?>

    @Query("SELECT * FROM game_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): GameSessionEntity?

    @Query("SELECT * FROM game_sessions ORDER BY lastUpdatedTimestamp DESC")
    fun getAllSessionsFlow(): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: GameSessionEntity)

    @Query("DELETE FROM game_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
