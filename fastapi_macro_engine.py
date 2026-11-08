"""
MacroShift: Dynamic Macro-Economic Engine & State Manager
=========================================================
High-performance FastAPI state manager script accepting multi-timeline configurations,
tracking asset balances against global policy events, featuring an automated
random Governance Directive Injector, user authentication, and game save/resume slots.

Usage:
    pip install fastapi uvicorn pydantic
    python fastapi_macro_engine.py
    # API docs available at http://localhost:8000/docs
"""

import asyncio
import hashlib
import random
import time
from enum import Enum
from typing import Dict, List, Optional
from pydantic import BaseModel, Field
from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect, Depends, Header
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="MacroShift Macroeconomic Engine",
    description="Dynamic state manager for capital shifting, governance directive shocks, user authentication, and save/resume state",
    version="1.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class AssetType(str, Enum):
    CASH = "CASH"
    BONDS = "BONDS"
    TECH_EQUITIES = "TECH_EQUITIES"
    COMMODITIES = "COMMODITIES"
    REAL_ESTATE = "REAL_ESTATE"
    CRYPTO = "CRYPTO"


class DirectiveUrgency(str, Enum):
    MODERATE = "MODERATE"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"
    CATASTROPHIC = "CATASTROPHIC"


class GovernanceDirective(BaseModel):
    id: str
    headline: str
    description: str
    urgency: DirectiveUrgency
    repo_rate_delta_bps: int
    inflation_delta_bps: int
    capital_gains_tax_delta: float = 0.0
    asset_shocks: Dict[AssetType, float] = Field(default_factory=dict)
    asset_yield_modifiers: Dict[AssetType, float] = Field(default_factory=dict)
    duration_ticks: int = 20
    remaining_ticks: int = 20


class AssetBucketState(BaseModel):
    type: AssetType
    name: str
    ticker: str
    balance: float
    allocation_pct: float
    effective_apr: float
    last_tick_gain: float


class MacroIndicators(BaseModel):
    repo_rate_apr: float
    inflation_apr: float
    capital_gains_tax: float
    regime_name: str
    regime_description: str


class DebtState(BaseModel):
    principal: float
    monthly_service_cost: float
    seconds_until_due: int
    default_strikes: int
    max_strikes: int = 3
    is_danger: bool


class GameStateResponse(BaseModel):
    status: str
    tick: int
    net_worth: float
    peak_net_worth: float
    liquid_cash: float
    macro: MacroIndicators
    debt: DebtState
    assets: List[AssetBucketState]
    active_directives: List[GovernanceDirective]
    adaptive_resilience: int


# ==========================================
# User Authentication Models & Store
# ==========================================
class UserRegisterRequest(BaseModel):
    username: str
    email: str
    password: str

class UserLoginRequest(BaseModel):
    username: str
    password: str

class UserProfileResponse(BaseModel):
    username: str
    email: str
    title: str = "Macro Strategist"
    has_saved_game: bool = False

class SaveGameRequest(BaseModel):
    slot_name: str = "CloudSlot_1"

# In-memory secure user store (production would use PostgreSQL or Firestore)
USERS_DB = {}
SAVED_SLOTS_DB = {}

def hash_pw(pwd: str) -> str:
    return hashlib.sha256(pwd.encode()).hexdigest()


# ==========================================
# Automated Governance Directive Injector
# ==========================================
class GovernanceDirectiveInjector:
    DIRECTIVE_CATALOG = [
        {
            "id": "cb_rate_hike_250",
            "headline": "National Central Bank Raises Repo Rates by 250 bps",
            "description": "Emergency MPC decree to curb consumer prices. Sovereign long bonds drop -19%. Debt cost spikes.",
            "urgency": DirectiveUrgency.CRITICAL,
            "repo_rate_delta_bps": 250,
            "inflation_delta_bps": -50,
            "asset_shocks": {AssetType.BONDS: -0.19, AssetType.TECH_EQUITIES: -0.09},
            "asset_yield_modifiers": {AssetType.CASH: 0.025, AssetType.BONDS: 0.015},
            "duration_ticks": 20
        },
        {
            "id": "capital_gains_overhaul",
            "headline": "Sweeping Capital Gains Tax Overhaul Introduced",
            "description": "40% retroactive windfall tax on high-beta venture and tech equities. Panic selling ignited.",
            "urgency": DirectiveUrgency.HIGH,
            "repo_rate_delta_bps": 0,
            "inflation_delta_bps": 20,
            "capital_gains_tax_delta": 0.15,
            "asset_shocks": {AssetType.TECH_EQUITIES: -0.14, AssetType.CRYPTO: -0.28},
            "asset_yield_modifiers": {AssetType.COMMODITIES: 0.04},
            "duration_ticks": 20
        },
        {
            "id": "crude_embargo_geopolitical",
            "headline": "Geopolitical Strait Chokepoint: Crude Spikes 40%",
            "description": "Supply chains strangled. Energy and raw commodities skyrocket. Inflation jumps +320 bps.",
            "urgency": DirectiveUrgency.CRITICAL,
            "repo_rate_delta_bps": 75,
            "inflation_delta_bps": 320,
            "asset_shocks": {AssetType.COMMODITIES: 0.36, AssetType.TECH_EQUITIES: -0.06},
            "asset_yield_modifiers": {AssetType.COMMODITIES: 0.12, AssetType.CASH: -0.04},
            "duration_ticks": 22
        },
        {
            "id": "emergency_qe_cut",
            "headline": "Emergency 300 bps Rate Slash + Quantitative Easing",
            "description": "Central bank injects emergency liquidity. Sovereign bonds surge +22%; Tech & Crypto moon.",
            "urgency": DirectiveUrgency.HIGH,
            "repo_rate_delta_bps": -300,
            "inflation_delta_bps": 80,
            "asset_shocks": {AssetType.BONDS: 0.22, AssetType.TECH_EQUITIES: 0.18, AssetType.CRYPTO: 0.35},
            "asset_yield_modifiers": {AssetType.CASH: -0.03},
            "duration_ticks": 24
        }
    ]

    @classmethod
    def generate_random(cls, current_repo: float) -> GovernanceDirective:
        raw = random.choice(cls.DIRECTIVE_CATALOG)
        adj_repo = raw["repo_rate_delta_bps"]
        if current_repo + (adj_repo / 10000.0) < 0.005:
            adj_repo = -int((current_repo - 0.005) * 10000)
        return GovernanceDirective(
            id=f"{raw['id']}_{int(time.time())}",
            headline=raw["headline"],
            description=raw["description"],
            urgency=raw["urgency"],
            repo_rate_delta_bps=adj_repo,
            inflation_delta_bps=raw["inflation_delta_bps"],
            capital_gains_tax_delta=raw.get("capital_gains_tax_delta", 0.0),
            asset_shocks=raw.get("asset_shocks", {}),
            asset_yield_modifiers=raw.get("asset_yield_modifiers", {}),
            duration_ticks=raw["duration_ticks"],
            remaining_ticks=raw["duration_ticks"]
        )


# ==========================================
# Modular Object-Oriented Macro Engine
# ==========================================
class MacroEngine:
    def __init__(self, timeline: str = "stagflation_1970s"):
        self.timeline = timeline
        self.tick_count = 0
        self.repo_rate = 0.085
        self.inflation = 0.125
        self.capital_gains_tax = 0.25
        self.debt_principal = 90000.0
        self.debt_seconds = 12
        self.debt_strikes = 0
        self.status = "RUNNING"
        self.resilience_score = 100

        self.balances: Dict[AssetType, float] = {
            AssetType.CASH: 35000.0,
            AssetType.BONDS: 30000.0,
            AssetType.TECH_EQUITIES: 25000.0,
            AssetType.COMMODITIES: 20000.0,
            AssetType.REAL_ESTATE: 15000.0,
            AssetType.CRYPTO: 5000.0
        }
        self.active_directives: List[GovernanceDirective] = []
        self.peak_net_worth = self.get_net_worth()

    def get_net_worth(self) -> float:
        return sum(self.balances.values()) - self.debt_principal

    def calculate_effective_apr(self, asset: AssetType) -> float:
        base = {
            AssetType.CASH: max(0.005, self.repo_rate - 0.01),
            AssetType.BONDS: 0.045 + (0.050 - self.repo_rate) * 0.8,
            AssetType.TECH_EQUITIES: 0.14 - (self.repo_rate * 0.9) - (self.capital_gains_tax * 0.1),
            AssetType.COMMODITIES: -0.02 + (self.inflation * 1.6),
            AssetType.REAL_ESTATE: 0.08 - (self.repo_rate * 0.6),
            AssetType.CRYPTO: 0.25 - (self.repo_rate * 1.5) - (self.capital_gains_tax * 0.2)
        }[asset]

        for d in self.active_directives:
            base += d.asset_yield_modifiers.get(asset, 0.0)
        return base

    def shift_capital(self, from_asset: AssetType, to_asset: AssetType, fraction: float):
        if from_asset == to_asset or fraction <= 0 or fraction > 1:
            return False
        curr_bal = self.balances.get(from_asset, 0.0)
        amount = curr_bal * fraction
        tax = amount * (self.capital_gains_tax * 0.15) if from_asset in [AssetType.TECH_EQUITIES, AssetType.CRYPTO] else 0
        net = amount - tax
        self.balances[from_asset] = max(0.0, curr_bal - amount)
        self.balances[to_asset] = self.balances.get(to_asset, 0.0) + net
        self.resilience_score = min(100, self.resilience_score + 5)
        return True

    def restore_slot(self, slot_data: dict):
        self.tick_count = slot_data["tick"]
        self.debt_principal = slot_data["debt_principal"]
        self.debt_seconds = 12
        self.debt_strikes = 0
        self.status = "RUNNING"
        self.active_directives.clear()
        for k, v in slot_data["balances"].items():
            self.balances[AssetType(k)] = v

    def tick(self) -> GameStateResponse:
        if self.status != "RUNNING":
            return self.get_state()

        self.tick_count += 1

        # 1. Directives countdown & expiration
        expired = []
        for d in self.active_directives:
            d.remaining_ticks -= 1
            if d.remaining_ticks <= 0:
                self.repo_rate = max(0.005, self.repo_rate - (d.repo_rate_delta_bps / 10000.0))
                self.inflation = max(0.005, self.inflation - (d.inflation_delta_bps / 10000.0))
                self.capital_gains_tax = max(0.10, self.capital_gains_tax - d.capital_gains_tax_delta)
                expired.append(d)
        for e in expired:
            self.active_directives.remove(e)

        # 2. Random shock injector every 16 ticks
        if self.tick_count % 16 == 0:
            directive = GovernanceDirectiveInjector.generate_random(self.repo_rate)
            self.active_directives.append(directive)
            self.repo_rate = max(0.005, self.repo_rate + (directive.repo_rate_delta_bps / 10000.0))
            self.inflation = max(0.005, self.inflation + (directive.inflation_delta_bps / 10000.0))
            self.capital_gains_tax = min(0.60, max(0.10, self.capital_gains_tax + directive.capital_gains_tax_delta))
            for asset, shock in directive.asset_shocks.items():
                self.balances[asset] = max(0.0, self.balances[asset] * (1.0 + shock))

        # 3. Compounding asset growth
        for asset in AssetType:
            apr = self.calculate_effective_apr(asset)
            tick_growth = self.balances[asset] * (apr / 12.0)
            self.balances[asset] = max(0.0, self.balances[asset] + tick_growth)

        # 4. Inflation drag on cash
        if self.inflation > 0.04:
            self.balances[AssetType.CASH] -= self.balances[AssetType.CASH] * (self.inflation / 12.0 * 0.35)

        # 5. Debt servicing
        self.debt_seconds -= 1
        debt_apr = self.repo_rate + 0.035 + (self.inflation * 0.35)
        monthly_cost = (self.debt_principal * debt_apr) / 12.0

        if self.debt_seconds <= 0:
            self.debt_seconds = 12
            if self.balances[AssetType.CASH] >= monthly_cost:
                self.balances[AssetType.CASH] -= monthly_cost
            else:
                self.debt_strikes += 1
                shortfall = monthly_cost - self.balances[AssetType.CASH]
                self.balances[AssetType.CASH] = 0.0
                self.debt_principal += shortfall * 1.25
                if self.debt_strikes >= 3:
                    self.status = "BANKRUPT"

        nw = self.get_net_worth()
        if nw > self.peak_net_worth:
            self.peak_net_worth = nw
        if nw <= 0:
            self.status = "BANKRUPT"

        return self.get_state()

    def get_state(self) -> GameStateResponse:
        total_assets = sum(self.balances.values())
        debt_apr = self.repo_rate + 0.035 + (self.inflation * 0.35)
        monthly_cost = (self.debt_principal * debt_apr) / 12.0

        asset_states = [
            AssetBucketState(
                type=t,
                name=t.value.replace("_", " ").title(),
                ticker=t.value[:4],
                balance=self.balances[t],
                allocation_pct=(self.balances[t] / total_assets * 100.0) if total_assets > 0 else 0.0,
                effective_apr=self.calculate_effective_apr(t),
                last_tick_gain=self.calculate_effective_apr(t) / 12.0
            ) for t in AssetType
        ]

        return GameStateResponse(
            status=self.status,
            tick=self.tick_count,
            net_worth=self.get_net_worth(),
            peak_net_worth=self.peak_net_worth,
            liquid_cash=self.balances[AssetType.CASH],
            macro=MacroIndicators(
                repo_rate_apr=self.repo_rate,
                inflation_apr=self.inflation,
                capital_gains_tax=self.capital_gains_tax,
                regime_name="Monetary Pressure" if self.repo_rate > 0.07 else "Normal Cycle",
                regime_description="Dynamic macroeconomic state loop."
            ),
            debt=DebtState(
                principal=self.debt_principal,
                monthly_service_cost=monthly_cost,
                seconds_until_due=self.debt_seconds,
                default_strikes=self.debt_strikes,
                is_danger=self.debt_strikes >= 2 or self.debt_seconds <= 3
            ),
            assets=asset_states,
            active_directives=self.active_directives,
            adaptive_resilience=self.resilience_score
        )


engine_instance = MacroEngine()


class ShiftRequest(BaseModel):
    from_asset: AssetType
    to_asset: AssetType
    fraction: float = Field(..., gt=0.0, le=1.0)


# ==========================================
# Authentication & Game State Persistence APIs
# ==========================================
@app.post("/api/auth/signup")
def sign_up(req: UserRegisterRequest):
    if req.username in USERS_DB:
        raise HTTPException(status_code=400, detail="Username already registered")
    USERS_DB[req.username] = {
        "username": req.username,
        "email": req.email,
        "password_hash": hash_pw(req.password)
    }
    return {"message": "User registered successfully", "username": req.username}

@app.post("/api/auth/login")
def log_in(req: UserLoginRequest):
    user = USERS_DB.get(req.username)
    if not user or user["password_hash"] != hash_pw(req.password):
        raise HTTPException(status_code=401, detail="Invalid username or password")
    # Return session token
    token = f"token_{req.username}_{int(time.time())}"
    return {"token": token, "username": req.username, "email": user["email"]}

@app.post("/api/game/save")
def save_game(req: SaveGameRequest, x_username: Optional[str] = Header(None)):
    user = x_username or "Guest"
    SAVED_SLOTS_DB[user] = {
        "timestamp": int(time.time()),
        "tick": engine_instance.tick_count,
        "debt_principal": engine_instance.debt_principal,
        "balances": {k.value: v for k, v in engine_instance.balances.items()}
    }
    return {"status": "saved", "slot": req.slot_name, "tick": engine_instance.tick_count}

@app.get("/api/game/resume")
def resume_game(x_username: Optional[str] = Header(None)):
    user = x_username or "Guest"
    slot = SAVED_SLOTS_DB.get(user)
    if not slot:
        raise HTTPException(status_code=404, detail="No saved game found for user")
    engine_instance.restore_slot(slot)
    return {"status": "resumed", "state": engine_instance.get_state()}


# ==========================================
# Core Game Loop APIs
# ==========================================
@app.get("/api/state", response_model=GameStateResponse)
def get_state():
    return engine_instance.get_state()


@app.post("/api/tick", response_model=GameStateResponse)
def trigger_tick():
    return engine_instance.tick()


@app.post("/api/shift")
def shift_capital(req: ShiftRequest):
    success = engine_instance.shift_capital(req.from_asset, req.to_asset, req.fraction)
    if not success:
        raise HTTPException(status_code=400, detail="Invalid capital shift transfer")
    return {"status": "ok", "state": engine_instance.get_state()}


@app.post("/api/restart")
def restart(timeline: str = "stagflation_1970s"):
    global engine_instance
    engine_instance = MacroEngine(timeline)
    return engine_instance.get_state()


@app.websocket("/ws")
async def websocket_loop(websocket: WebSocket):
    await websocket.accept()
    try:
        while True:
            state = engine_instance.tick()
            await websocket.send_text(state.json())
            await asyncio.sleep(1.0)
    except WebSocketDisconnect:
        pass

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("fastapi_macro_engine:app", host="0.0.0.0", port=8000, reload=True)
