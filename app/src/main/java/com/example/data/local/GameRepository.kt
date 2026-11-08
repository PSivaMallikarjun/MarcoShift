package com.example.data.local

import com.example.data.local.entity.AssetBalanceEntity
import com.example.data.local.entity.GameSessionEntity
import com.example.data.local.entity.GameState
import com.example.data.local.entity.GameTurnHistoryEntity
import com.example.model.AssetBucket
import com.example.model.GameStatus
import com.example.model.MacroShiftUiState
import kotlinx.coroutines.flow.Flow

class GameRepository(private val database: MacroDatabase) {

    private val sessionDao = database.gameSessionDao()
    private val assetDao = database.assetBalanceDao()
    private val historyDao = database.gameTurnHistoryDao()
    private val gameStateDao = database.gameStateDao()

    fun observeSession(sessionId: String): Flow<GameSessionEntity?> {
        return sessionDao.getSessionFlow(sessionId)
    }

    fun observeAllSessions(): Flow<List<GameSessionEntity>> {
        return sessionDao.getAllSessionsFlow()
    }

    fun observeAssets(sessionId: String): Flow<List<AssetBalanceEntity>> {
        return assetDao.getAssetsForSessionFlow(sessionId)
    }

    fun observeTurnHistory(sessionId: String, limit: Int = 50): Flow<List<GameTurnHistoryEntity>> {
        return historyDao.getRecentTurnsFlow(sessionId, limit)
    }

    fun observeLatestGameState(): Flow<GameState?> {
        return gameStateDao.getLatestGameStateFlow()
    }

    suspend fun saveGameState(gameState: GameState): Long {
        return gameStateDao.insertGameState(gameState)
    }

    suspend fun updateGameState(gameState: GameState): Int {
        return gameStateDao.updateGameState(gameState)
    }

    suspend fun getLatestGameState(): GameState? {
        return gameStateDao.getLatestGameState()
    }

    suspend fun getGameStateById(id: Long): GameState? {
        return gameStateDao.getGameStateById(id)
    }

    suspend fun deleteGameState(id: Long): Int {
        return gameStateDao.deleteGameStateById(id)
    }

    suspend fun saveFullGameState(
        sessionId: String,
        state: MacroShiftUiState,
        actionTaken: String? = null
    ) {
        // 1. Save or update GameSessionEntity
        val sessionEntity = GameSessionEntity(
            sessionId = sessionId,
            scenarioId = state.currentScenario.id,
            scenarioName = state.currentScenario.name,
            currentTick = state.currentTick,
            liquidCash = state.liquidCash,
            totalNetWorth = state.totalNetWorth,
            peakNetWorth = state.peakNetWorth,
            repoRateApr = state.macro.repoRateApr,
            inflationApr = state.macro.inflationApr,
            capitalGainsTax = state.macro.capitalGainsTax,
            debtPrincipal = state.debt.principal,
            debtMonthlyServiceCost = state.debt.monthlyServiceCost,
            debtSecondsUntilDue = state.debt.secondsUntilDue,
            debtDefaultStrikes = state.debt.defaultStrikes,
            regimeName = state.macro.regimeName,
            adaptiveResilienceScore = state.adaptiveResilienceScore,
            status = state.gameStatus.name,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        sessionDao.insertOrUpdateSession(sessionEntity)

        // 2. Save Asset balances
        val assetEntities = state.assets.map { bucket ->
            AssetBalanceEntity(
                sessionId = sessionId,
                assetType = bucket.type.name,
                ticker = bucket.type.ticker,
                displayName = bucket.type.displayName,
                balance = bucket.balance,
                allocationPercent = bucket.allocationPercent,
                effectiveApr = bucket.effectiveApr,
                lastTickReturn = bucket.lastTickReturn
            )
        }
        assetDao.insertAssets(assetEntities)

        // 3. Record GameTurnHistoryEntity
        val activeDirective = state.activeDirectives.lastOrNull()
        val turnEntity = GameTurnHistoryEntity(
            sessionId = sessionId,
            tick = state.currentTick,
            timestamp = System.currentTimeMillis(),
            totalNetWorth = state.totalNetWorth,
            liquidCash = state.liquidCash,
            debtPrincipal = state.debt.principal,
            monthlyDebtService = state.debt.monthlyServiceCost,
            repoRateApr = state.macro.repoRateApr,
            inflationApr = state.macro.inflationApr,
            activeDirectiveHeadline = activeDirective?.headline,
            eventDescription = state.recentLogs.firstOrNull()?.detail,
            actionTaken = actionTaken
        )
        historyDao.insertTurn(turnEntity)

        // 4. Save GameState entity storing user progress, liquidCash, debtLiabilities, and timestamp
        val gameStateEntity = GameState(
            liquidCash = state.liquidCash,
            debtLiabilities = state.debt.monthlyServiceCost,
            timestamp = System.currentTimeMillis(),
            currentTurn = state.currentTick,
            totalNetWorth = state.totalNetWorth,
            debtPrincipal = state.debt.principal,
            scenarioId = state.currentScenario.id,
            repoRateApr = state.macro.repoRateApr,
            inflationApr = state.macro.inflationApr,
            defaultStrikes = state.debt.defaultStrikes,
            isBankrupt = state.gameStatus == GameStatus.BANKRUPT
        )
        gameStateDao.insertGameState(gameStateEntity)
    }

    suspend fun getSession(sessionId: String): GameSessionEntity? {
        return sessionDao.getSessionById(sessionId)
    }

    suspend fun getAssets(sessionId: String): List<AssetBalanceEntity> {
        return assetDao.getAssetsForSession(sessionId)
    }

    suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSession(sessionId)
        assetDao.deleteAssetsForSession(sessionId)
        historyDao.clearHistoryForSession(sessionId)
        gameStateDao.clearAllGameStates()
    }
}
