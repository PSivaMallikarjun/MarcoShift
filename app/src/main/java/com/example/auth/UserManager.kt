package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AssetBucket
import com.example.model.AssetType
import com.example.model.MacroShiftUiState
import com.example.model.PredefinedScenarios
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class UserProfile(
    val username: String,
    val email: String,
    val avatarEmoji: String = "⚡",
    val title: String = "Macro Strategist",
    val createdAt: Long = System.currentTimeMillis()
)

data class SavedGameSlot(
    val slotId: String,
    val username: String,
    val timestamp: Long,
    val scenarioId: String,
    val scenarioName: String,
    val tick: Int,
    val netWorth: Double,
    val liquidCash: Double,
    val debtPrincipal: Double,
    val assetBalances: Map<AssetType, Double>
)

class UserManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("macroshift_user_prefs", Context.MODE_PRIVATE)

    fun getCurrentUser(): UserProfile? {
        val username = prefs.getString("current_user_name", null) ?: return null
        val email = prefs.getString("current_user_email", "") ?: ""
        val avatar = prefs.getString("current_user_avatar", "⚡") ?: "⚡"
        val title = prefs.getString("current_user_title", "Macro Strategist") ?: "Macro Strategist"
        return UserProfile(username = username, email = email, avatarEmoji = avatar, title = title)
    }

    fun signUp(username: String, email: String, passwordRaw: String): Result<UserProfile> {
        val cleanName = username.trim()
        if (cleanName.length < 3) return Result.failure(IllegalArgumentException("Username must be at least 3 characters"))
        if (!email.contains("@")) return Result.failure(IllegalArgumentException("Please enter a valid email"))
        if (passwordRaw.length < 4) return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))

        val existingHash = prefs.getString("user_pwd_$cleanName", null)
        if (existingHash != null) {
            return Result.failure(IllegalStateException("Username already registered. Please log in."))
        }

        val pwdHash = hashPassword(passwordRaw)
        val profile = UserProfile(username = cleanName, email = email.trim())

        prefs.edit()
            .putString("user_pwd_$cleanName", pwdHash)
            .putString("user_email_$cleanName", email.trim())
            .putString("current_user_name", cleanName)
            .putString("current_user_email", email.trim())
            .putString("current_user_avatar", "⚡")
            .putString("current_user_title", "Macro Strategist")
            .apply()

        return Result.success(profile)
    }

    fun logIn(username: String, passwordRaw: String): Result<UserProfile> {
        val cleanName = username.trim()
        val storedHash = prefs.getString("user_pwd_$cleanName", null)
            ?: return Result.failure(IllegalArgumentException("User not found. Please sign up."))

        val inputHash = hashPassword(passwordRaw)
        if (storedHash != inputHash) {
            return Result.failure(IllegalArgumentException("Incorrect password."))
        }

        val email = prefs.getString("user_email_$cleanName", "") ?: ""
        val avatar = prefs.getString("current_user_avatar", "⚡") ?: "⚡"
        val title = prefs.getString("current_user_title", "Macro Strategist") ?: "Macro Strategist"
        val profile = UserProfile(username = cleanName, email = email, avatarEmoji = avatar, title = title)

        prefs.edit()
            .putString("current_user_name", cleanName)
            .putString("current_user_email", email)
            .apply()

        return Result.success(profile)
    }

    fun logOut() {
        prefs.edit().remove("current_user_name").remove("current_user_email").apply()
    }

    fun saveGame(state: MacroShiftUiState, slotName: String = "QuickSave"): SavedGameSlot {
        val user = getCurrentUser()?.username ?: "Guest"
        val balances = state.assets.associate { it.type to it.balance }
        val slot = SavedGameSlot(
            slotId = "slot_${System.currentTimeMillis()}",
            username = user,
            timestamp = System.currentTimeMillis(),
            scenarioId = state.currentScenario.id,
            scenarioName = state.currentScenario.name,
            tick = state.currentTick,
            netWorth = state.totalNetWorth,
            liquidCash = state.liquidCash,
            debtPrincipal = state.debt.principal,
            assetBalances = balances
        )

        // Serialize to JSON
        val json = JSONObject().apply {
            put("slotId", slot.slotId)
            put("username", slot.username)
            put("timestamp", slot.timestamp)
            put("scenarioId", slot.scenarioId)
            put("scenarioName", slot.scenarioName)
            put("tick", slot.tick)
            put("netWorth", slot.netWorth)
            put("liquidCash", slot.liquidCash)
            put("debtPrincipal", slot.debtPrincipal)
            val balObj = JSONObject()
            balances.forEach { (type, bal) -> balObj.put(type.name, bal) }
            put("balances", balObj)
        }

        prefs.edit().putString("saved_slot_${user}", json.toString()).apply()
        return slot
    }

    fun getSavedGame(): SavedGameSlot? {
        val user = getCurrentUser()?.username ?: "Guest"
        val jsonStr = prefs.getString("saved_slot_${user}", null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            val balObj = obj.getJSONObject("balances")
            val balances = mutableMapOf<AssetType, Double>()
            AssetType.values().forEach { type ->
                if (balObj.has(type.name)) {
                    balances[type] = balObj.getDouble(type.name)
                }
            }
            SavedGameSlot(
                slotId = obj.getString("slotId"),
                username = obj.getString("username"),
                timestamp = obj.getLong("timestamp"),
                scenarioId = obj.getString("scenarioId"),
                scenarioName = obj.getString("scenarioName"),
                tick = obj.getInt("tick"),
                netWorth = obj.getDouble("netWorth"),
                liquidCash = obj.getDouble("liquidCash"),
                debtPrincipal = obj.getDouble("debtPrincipal"),
                assetBalances = balances
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
