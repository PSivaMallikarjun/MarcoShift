package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameRepository
import com.example.data.local.MacroDatabase
import com.example.data.local.entity.AssetBalanceEntity
import com.example.data.local.entity.GameSessionEntity
import com.example.data.local.entity.GameTurnHistoryEntity
import com.example.model.MacroShiftUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoomGameState(
    val sessionId: String = "session_default",
    val session: GameSessionEntity? = null,
    val assets: List<AssetBalanceEntity> = emptyList(),
    val historicalTurns: List<GameTurnHistoryEntity> = emptyList(),
    val liquidCash: Double = 0.0,
    val debtPrincipal: Double = 0.0,
    val monthlyDebtLiabilities: Double = 0.0,
    val totalNetWorth: Double = 0.0,
    val currentTick: Int = 0,
    val isInitialized: Boolean = false
)

class MacroGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(MacroDatabase.getInstance(application))
    private val activeSessionId = MutableStateFlow("session_active")

    val sessionFlow: StateFlow<GameSessionEntity?> = activeSessionId
        .flatMapLatest { id -> repository.observeSession(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val assetsFlow: StateFlow<List<AssetBalanceEntity>> = activeSessionId
        .flatMapLatest { id -> repository.observeAssets(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historicalTurnsFlow: StateFlow<List<GameTurnHistoryEntity>> = activeSessionId
        .flatMapLatest { id -> repository.observeTurnHistory(id, limit = 100) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSessionsFlow: StateFlow<List<GameSessionEntity>> = repository.observeAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestGameState: StateFlow<com.example.data.local.entity.GameState?> = repository.observeLatestGameState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveGameState(liquidCash: Double, debtLiabilities: Double, turn: Int = 0, totalNetWorth: Double = 0.0) {
        viewModelScope.launch {
            repository.saveGameState(
                com.example.data.local.entity.GameState(
                    liquidCash = liquidCash,
                    debtLiabilities = debtLiabilities,
                    timestamp = System.currentTimeMillis(),
                    currentTurn = turn,
                    totalNetWorth = totalNetWorth
                )
            )
        }
    }

    val gameState: StateFlow<RoomGameState> = combine(
        sessionFlow,
        assetsFlow,
        historicalTurnsFlow
    ) { session, assets, turns ->
        RoomGameState(
            sessionId = activeSessionId.value,
            session = session,
            assets = assets,
            historicalTurns = turns,
            liquidCash = session?.liquidCash ?: assets.find { it.assetType == "CASH" }?.balance ?: 0.0,
            debtPrincipal = session?.debtPrincipal ?: 0.0,
            monthlyDebtLiabilities = session?.debtMonthlyServiceCost ?: 0.0,
            totalNetWorth = session?.totalNetWorth ?: 0.0,
            currentTick = session?.currentTick ?: 0,
            isInitialized = session != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RoomGameState()
    )

    fun persistTurn(uiState: MacroShiftUiState, actionSummary: String? = null) {
        viewModelScope.launch {
            repository.saveFullGameState(
                sessionId = activeSessionId.value,
                state = uiState,
                actionTaken = actionSummary
            )
        }
    }

    fun switchSession(sessionId: String) {
        activeSessionId.value = sessionId
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }
}
