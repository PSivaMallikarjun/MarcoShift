package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AssetBalanceDao
import com.example.data.local.dao.GameSessionDao
import com.example.data.local.dao.GameStateDao
import com.example.data.local.dao.GameTurnHistoryDao
import com.example.data.local.entity.AssetBalanceEntity
import com.example.data.local.entity.GameSessionEntity
import com.example.data.local.entity.GameState
import com.example.data.local.entity.GameTurnHistoryEntity

@Database(
    entities = [
        GameSessionEntity::class,
        AssetBalanceEntity::class,
        GameTurnHistoryEntity::class,
        GameState::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MacroDatabase : RoomDatabase() {

    abstract fun gameSessionDao(): GameSessionDao
    abstract fun assetBalanceDao(): AssetBalanceDao
    abstract fun gameTurnHistoryDao(): GameTurnHistoryDao
    abstract fun gameStateDao(): GameStateDao

    companion object {
        @Volatile
        private var INSTANCE: MacroDatabase? = null

        fun getInstance(context: Context): MacroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MacroDatabase::class.java,
                    "macroshift_room.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
