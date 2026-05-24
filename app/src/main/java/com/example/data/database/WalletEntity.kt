package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val satoshis: Long = 0L,
    val totalSolved: Int = 0,
    val hashPower: Double = 1.0, // Th/s
    val mnemonicSecret: String = "",
    val securePin: String = "",
    val isInitialized: Boolean = false,
    val activeNodeAddress: String = "stratum+tcp://pool.satomine.secure:3333"
)
