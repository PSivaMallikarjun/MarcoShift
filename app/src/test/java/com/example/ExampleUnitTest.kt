package com.example

import com.example.engine.DirectiveInjector
import com.example.engine.MacroEngine
import com.example.model.AssetType
import com.example.model.PredefinedScenarios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `macro engine initial scenario state`() {
    val engine = MacroEngine(PredefinedScenarios.STAGFLATION_1970S)
    val state = engine.buildUiState()
    assertTrue(state.totalNetWorth > 0)
    assertEquals(6, state.assets.size)
    assertTrue(state.liquidCash > 0)
    assertTrue(state.debt.principal > 0)
  }

  @Test
  fun `macro engine capital shifting reduces source and increases target`() {
    val engine = MacroEngine(PredefinedScenarios.STAGFLATION_1970S)
    val initialCash = engine.buildUiState().liquidCash
    val initialBonds = engine.buildUiState().assets.find { it.type == AssetType.BONDS }?.balance ?: 0.0

    val shifted = engine.shiftCapital(AssetType.BONDS, AssetType.CASH, 0.5)
    assertTrue(shifted)

    val updatedState = engine.buildUiState()
    val updatedCash = updatedState.liquidCash
    val updatedBonds = updatedState.assets.find { it.type == AssetType.BONDS }?.balance ?: 0.0

    assertTrue(updatedCash > initialCash)
    assertTrue(updatedBonds < initialBonds)
  }

  @Test
  fun `governance directive injector returns valid shocks`() {
    val directive = DirectiveInjector.getRandomDirective(0.05, 0.05)
    assertTrue(directive.headline.isNotEmpty())
    assertTrue(directive.description.isNotEmpty())
    assertTrue(directive.durationTicks > 0)
  }

  @Test
  fun `macro engine restore saved state updates balances and tick`() {
    val engine = MacroEngine(PredefinedScenarios.STAGFLATION_1970S)
    val customBalances = mapOf(
      AssetType.CASH to 80_000.0,
      AssetType.BONDS to 10_000.0,
      AssetType.TECH_EQUITIES to 50_000.0,
      AssetType.COMMODITIES to 40_000.0,
      AssetType.REAL_ESTATE to 20_000.0,
      AssetType.CRYPTO to 15_000.0
    )
    engine.restoreSavedState(
      savedScenario = PredefinedScenarios.SOVEREIGN_DEBT_2026,
      tick = 42,
      savedDebtPrincipal = 50_000.0,
      balances = customBalances
    )
    val state = engine.buildUiState()
    assertEquals(42, state.currentTick)
    assertEquals(50_000.0, state.debt.principal, 0.001)
    assertEquals(80_000.0, state.liquidCash, 0.001)
    assertEquals(PredefinedScenarios.SOVEREIGN_DEBT_2026.id, state.currentScenario.id)
  }

  @Test
  fun `room game state entity properties and defaults`() {
    val now = System.currentTimeMillis()
    val state = com.example.data.local.entity.GameState(
      liquidCash = 65000.0,
      debtLiabilities = 12500.0,
      timestamp = now,
      currentTurn = 14,
      totalNetWorth = 210000.0,
      debtPrincipal = 80000.0,
      scenarioId = "stagflation_1970s"
    )
    assertEquals(65000.0, state.liquidCash, 0.001)
    assertEquals(12500.0, state.debtLiabilities, 0.001)
    assertEquals(now, state.timestamp)
    assertEquals(14, state.currentTurn)
    assertEquals(210000.0, state.totalNetWorth, 0.001)
    assertEquals(80000.0, state.debtPrincipal, 0.001)
    assertEquals("stagflation_1970s", state.scenarioId)
  }
}

