package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WalletEntity::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class MiningDatabase : RoomDatabase() {
    abstract fun miningDao(): MiningDao

    companion object {
        @Volatile
        private var INSTANCE: MiningDatabase? = null

        fun getDatabase(context: Context): MiningDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiningDatabase::class.java,
                    "satomine_secure_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
