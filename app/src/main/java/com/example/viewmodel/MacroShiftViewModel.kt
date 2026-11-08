package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.SavedGameSlot
import com.example.auth.UserManager
import com.example.auth.UserProfile
import com.example.engine.DirectiveInjector
import com.example.engine.MacroEngine
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RebalancePreset(val displayName: String, val description: String) {
    SAFETY("Flight to Cash & TIPS", "100% Cash / Defensive hedge"),
    INFLATION_SHIELD("Commodity Fortress", "50% Gold/Oil, 25% Real Estate, 25% Cash"),
    RATE_HIKE_HEDGE("Hawkish Protection", "60% Cash, 30% Equities, 10% Commodities"),
    TECH_EUPHORIA("Growth Aggression", "50% Tech, 30% Crypto, 20% Cash"),
    BALANCED_60_40("Dynamic All-Weather", "35% Bonds, 25% Tech, 20% Cash, 20% Real Estate")
}

class MacroShiftViewModel(application: Application) : AndroidViewModel(application) {

    private val userManager = UserManager(application)
    private val gameRepository = com.example.data.local.GameRepository(com.example.data.local.MacroDatabase.getInstance(application))
    private val engine = MacroEngine(PredefinedScenarios.STAGFLATION_1970S)

    private val _uiState = MutableStateFlow(engine.buildUiState())
    val uiState: StateFlow<MacroShiftUiState> = _uiState.asStateFlow()

    val roomHistoryFlow: StateFlow<List<com.example.data.local.entity.GameTurnHistoryEntity>> =
        gameRepository.observeTurnHistory("session_active", limit = 100)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<com.example.data.local.entity.GameTurnHistoryEntity>()
            )

    private val _currentUser = MutableStateFlow<UserProfile?>(userManager.getCurrentUser())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _savedSlot = MutableStateFlow<SavedGameSlot?>(userManager.getSavedGame())
    val savedSlot: StateFlow<SavedGameSlot?> = _savedSlot.asStateFlow()

    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    private var loopJob: Job? = null
    private var simulationSpeed: Float = 1.0f

    init {
        startGameLoop()
    }

    private fun startGameLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (isActive) {
                val delayTime = (1000f / simulationSpeed).toLong().coerceAtLeast(200L)
                delay(delayTime)
                if (_uiState.value.gameStatus == GameStatus.RUNNING) {
                    val nextState = engine.tick()
                    _uiState.update { nextState.copy(simulationSpeed = simulationSpeed) }
                    if (nextState.currentTick % 4 == 0) {
                        gameRepository.saveFullGameState("session_active", nextState)
                    }
                }
            }
        }
    }

    fun togglePause() {
        _uiState.update { current ->
            val newStatus = if (current.gameStatus == GameStatus.RUNNING) {
                GameStatus.PAUSED
            } else if (current.gameStatus == GameStatus.PAUSED) {
                GameStatus.RUNNING
            } else {
                current.gameStatus
            }
            current.copy(gameStatus = newStatus)
        }
    }

    fun setSimulationSpeed(speed: Float) {
        simulationSpeed = speed
        _uiState.update { it.copy(simulationSpeed = speed) }
        startGameLoop()
    }

    fun switchScenario(scenario: TimelineScenario) {
        engine.loadScenario(scenario)
        val s = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
        _uiState.value = s
        viewModelScope.launch {
            gameRepository.saveFullGameState("session_active", s, "Switched Scenario: ${scenario.name}")
        }
        startGameLoop()
    }

    fun restartCurrentScenario() {
        engine.loadScenario(_uiState.value.currentScenario)
        val s = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
        _uiState.value = s
        viewModelScope.launch {
            gameRepository.saveFullGameState("session_active", s, "Restarted Scenario")
        }
        startGameLoop()
    }

    fun shiftCapital(from: AssetType, to: AssetType, fraction: Double) {
        val success = engine.shiftCapital(from, to, fraction)
        if (success) {
            val s = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
            _uiState.value = s
            viewModelScope.launch {
                gameRepository.saveFullGameState(
                    "session_active",
                    s,
                    "Shifted ${(fraction * 100).toInt()}% ${from.ticker} -> ${to.ticker}"
                )
            }
        }
    }

    fun applyPreset(preset: RebalancePreset) {
        val targets: Map<AssetType, Double> = when (preset) {
            RebalancePreset.SAFETY -> mapOf(
                AssetType.CASH to 0.70,
                AssetType.COMMODITIES to 0.20,
                AssetType.BONDS to 0.10,
                AssetType.TECH_EQUITIES to 0.0,
                AssetType.REAL_ESTATE to 0.0,
                AssetType.CRYPTO to 0.0
            )
            RebalancePreset.INFLATION_SHIELD -> mapOf(
                AssetType.COMMODITIES to 0.50,
                AssetType.REAL_ESTATE to 0.25,
                AssetType.CASH to 0.20,
                AssetType.TECH_EQUITIES to 0.05,
                AssetType.BONDS to 0.0,
                AssetType.CRYPTO to 0.0
            )
            RebalancePreset.RATE_HIKE_HEDGE -> mapOf(
                AssetType.CASH to 0.55,
                AssetType.TECH_EQUITIES to 0.25,
                AssetType.COMMODITIES to 0.20,
                AssetType.BONDS to 0.0,
                AssetType.REAL_ESTATE to 0.0,
                AssetType.CRYPTO to 0.0
            )
            RebalancePreset.TECH_EUPHORIA -> mapOf(
                AssetType.TECH_EQUITIES to 0.50,
                AssetType.CRYPTO to 0.25,
                AssetType.CASH to 0.15,
                AssetType.REAL_ESTATE to 0.10,
                AssetType.BONDS to 0.0,
                AssetType.COMMODITIES to 0.0
            )
            RebalancePreset.BALANCED_60_40 -> mapOf(
                AssetType.BONDS to 0.30,
                AssetType.TECH_EQUITIES to 0.30,
                AssetType.CASH to 0.20,
                AssetType.REAL_ESTATE to 0.10,
                AssetType.COMMODITIES to 0.10,
                AssetType.CRYPTO to 0.0
            )
        }
        engine.rebalancePreset(targets)
        val s = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
        _uiState.value = s
        viewModelScope.launch {
            gameRepository.saveFullGameState("session_active", s, "Applied Preset: ${preset.displayName}")
        }
    }

    fun payDownDebt(amount: Double) {
        val success = engine.payDownDebt(amount)
        if (success) {
            val s = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
            _uiState.value = s
            viewModelScope.launch {
                gameRepository.saveFullGameState("session_active", s, "Debt Repaid: \$${String.format("%,.0f", amount)}")
            }
        }
    }

    fun drawEmergencyLiquidity(amount: Double) {
        val success = engine.borrowEmergencyLiquidity(amount)
        if (success) {
            val s = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
            _uiState.value = s
            viewModelScope.launch {
                gameRepository.saveFullGameState("session_active", s, "Emergency Liquidity Drawn: \$${String.format("%,.0f", amount)}")
            }
        }
    }

    fun triggerDirective(directive: GovernanceDirective) {
        engine.triggerManualDirective(directive)
        _uiState.value = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
    }

    fun dismissBanner(directiveId: String) {
        _uiState.update { it.copy(bannerDismissedId = directiveId) }
    }

    fun toggleTutorial(show: Boolean) {
        _showTutorial.value = show
    }

    // User Authentication & Save Game
    fun signUp(username: String, email: String, passwordRaw: String): String? {
        val result = userManager.signUp(username, email, passwordRaw)
        return if (result.isSuccess) {
            _currentUser.value = result.getOrNull()
            _savedSlot.value = userManager.getSavedGame()
            null
        } else {
            result.exceptionOrNull()?.message ?: "Sign up failed"
        }
    }

    fun logIn(username: String, passwordRaw: String): String? {
        val result = userManager.logIn(username, passwordRaw)
        return if (result.isSuccess) {
            _currentUser.value = result.getOrNull()
            _savedSlot.value = userManager.getSavedGame()
            null
        } else {
            result.exceptionOrNull()?.message ?: "Login failed"
        }
    }

    fun logOut() {
        userManager.logOut()
        _currentUser.value = null
        _savedSlot.value = null
    }

    fun saveCurrentGame() {
        val slot = userManager.saveGame(_uiState.value)
        _savedSlot.value = slot
    }

    fun resumeGame(slot: SavedGameSlot) {
        val scenario = PredefinedScenarios.ALL.find { it.id == slot.scenarioId } ?: PredefinedScenarios.STAGFLATION_1970S
        engine.restoreSavedState(
            savedScenario = scenario,
            tick = slot.tick,
            savedDebtPrincipal = slot.debtPrincipal,
            balances = slot.assetBalances
        )
        _uiState.value = engine.buildUiState().copy(simulationSpeed = simulationSpeed)
        startGameLoop()
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
    }
}
