package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GameState
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for the [GameState] entity.
 * Supports full CRUD operations:
 * - Create: [insertGameState], [insertAll]
 * - Read: [getLatestGameState], [getLatestGameStateFlow], [getGameStateById], [getAllGameStates]
 * - Update: [updateGameState], [updateCashAndDebt]
 * - Delete: [deleteGameState], [deleteGameStateById], [clearAllGameStates]
 */
@Dao
interface GameStateDao {

    // ==========================================
    // CREATE (Insert)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameState(gameState: GameState): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gameStates: List<GameState>): List<Long>

    // ==========================================
    // READ (Query / Retrieve Latest)
    // ==========================================

    /**
     * Retrieves the most recent GameState record as a reactive Flow.
     * Automatically emits updates when the table changes.
     */
    @Query("SELECT * FROM game_state ORDER BY id DESC LIMIT 1")
    fun getLatestGameStateFlow(): Flow<GameState?>

    /**
     * One-shot query to retrieve the latest GameState record by auto-incrementing ID.
     */
    @Query("SELECT * FROM game_state ORDER BY id DESC LIMIT 1")
    suspend fun getLatestGameState(): GameState?

    /**
     * Retrieves a specific GameState record by its primary key ID.
     */
    @Query("SELECT * FROM game_state WHERE id = :id LIMIT 1")
    suspend fun getGameStateById(id: Long): GameState?

    /**
     * Reactive query for a specific GameState record by primary key ID.
     */
    @Query("SELECT * FROM game_state WHERE id = :id LIMIT 1")
    fun getGameStateByIdFlow(id: Long): Flow<GameState?>

    /**
     * Retrieves all historical GameState records ordered by timestamp descending.
     */
    @Query("SELECT * FROM game_state ORDER BY timestamp DESC")
    fun getAllGameStatesFlow(): Flow<List<GameState>>

    /**
     * One-shot query to get all GameState records.
     */
    @Query("SELECT * FROM game_state ORDER BY timestamp DESC")
    suspend fun getAllGameStates(): List<GameState>

    // ==========================================
    // UPDATE
    // ==========================================

    /**
     * Updates an existing GameState record matching its primary key.
     * @return Number of rows updated.
     */
    @Update
    suspend fun updateGameState(gameState: GameState): Int

    /**
     * Targeted update of liquidCash, debtLiabilities, and timestamp for a given state ID.
     */
    @Query("UPDATE game_state SET liquidCash = :liquidCash, debtLiabilities = :debtLiabilities, timestamp = :timestamp WHERE id = :id")
    suspend fun updateCashAndDebt(id: Long, liquidCash: Double, debtLiabilities: Double, timestamp: Long): Int

    // ==========================================
    // DELETE
    // ==========================================

    /**
     * Deletes a specific GameState record.
     * @return Number of rows deleted.
     */
    @Delete
    suspend fun deleteGameState(gameState: GameState): Int

    /**
     * Deletes a GameState record by its primary key ID.
     */
    @Query("DELETE FROM game_state WHERE id = :id")
    suspend fun deleteGameStateById(id: Long): Int

    /**
     * Clears all GameState records in the table.
     */
    @Query("DELETE FROM game_state")
    suspend fun clearAllGameStates(): Int
}
