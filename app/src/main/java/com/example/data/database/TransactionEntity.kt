package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,               // "SOLVE", "WITHDRAW"
    val amountSats: Long,           // Amount in satoshis
    val txHash: String,             // SHA-256 styled Transaction Hash
    val targetAddress: String,      // LN invoice or Block address
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,             // "SECURED", "PROCESSED", "PENDING"
    val details: String             // e.g. "Solved Block #846,923" or "Lightning Payout"
)
