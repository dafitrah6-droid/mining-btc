package com.example.data.repository

import com.example.data.api.BtcPriceRetrofitClient
import com.example.data.api.PriceInfo
import com.example.data.database.MiningDao
import com.example.data.database.TransactionEntity
import com.example.data.database.WalletEntity
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class MiningRepository(private val miningDao: MiningDao) {

    val walletFlow: Flow<WalletEntity?> = miningDao.getWallet()
    val transactionsFlow: Flow<List<TransactionEntity>> = miningDao.getAllTransactions()

    suspend fun getWalletDirect(): WalletEntity? = miningDao.getWalletDirect()

    suspend fun createOrUpdateWallet(wallet: WalletEntity) {
        miningDao.insertWallet(wallet)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        miningDao.insertTransaction(transaction)
    }

    suspend fun addSatoshis(amount: Long, sourceDetail: String, targetAddress: String): String {
        val currentWallet = miningDao.getWalletDirect() ?: WalletEntity()
        val updatedWallet = currentWallet.copy(
            satoshis = currentWallet.satoshis + amount,
            totalSolved = currentWallet.totalSolved + 1
        )
        miningDao.insertWallet(updatedWallet)

        val txHash = generateSecureHash("SOLVE-" + System.currentTimeMillis() + "-" + amount)
        val transaction = TransactionEntity(
            type = "SOLVE",
            amountSats = amount,
            txHash = txHash,
            targetAddress = targetAddress,
            status = "SECURED",
            details = sourceDetail
        )
        miningDao.insertTransaction(transaction)
        return txHash
    }

    suspend fun withdrawSatoshis(amount: Long, invoice: String): Result<String> {
        val currentWallet = miningDao.getWalletDirect() ?: WalletEntity()
        if (currentWallet.satoshis < amount) {
            return Result.failure(Exception("Saldo tidak mencukupi (Insufficient satoshis)"))
        }

        val updatedWallet = currentWallet.copy(
            satoshis = currentWallet.satoshis - amount
        )
        miningDao.insertWallet(updatedWallet)

        val txHash = generateSecureHash("WITHDRAW-" + System.currentTimeMillis() + "-" + amount)
        val transaction = TransactionEntity(
            type = "WITHDRAW",
            amountSats = amount,
            txHash = txHash,
            targetAddress = invoice,
            status = "PROCESSED",
            details = "Lightning Payout (Instan)"
        )
        miningDao.insertTransaction(transaction)
        return Result.success(txHash)
    }

    suspend fun fetchBtcPrice(): Map<String, PriceInfo>? {
        return try {
            BtcPriceRetrofitClient.service.getTicker()
        } catch (e: Exception) {
            null
        }
    }

    // Helper functions for cryptography simulation
    fun generateSecureHash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Highly secure local double encryption simulation representation
    fun encryptDataWithDoubleSha256(data: String, salt: String): String {
        val step1 = generateSecureHash(data + salt)
        return generateSecureHash(step1 + "SatomineDoubleArmorKey_2026")
    }

    suspend fun clearHistory() {
        miningDao.clearTransactions()
        // Reset balance
        val currentWallet = miningDao.getWalletDirect() ?: WalletEntity()
        miningDao.insertWallet(currentWallet.copy(satoshis = 0L, totalSolved = 0))
    }
}
