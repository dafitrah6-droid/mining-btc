package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.database.MiningDatabase
import com.example.data.repository.MiningRepository
import kotlinx.coroutines.*
import java.security.MessageDigest
import kotlin.random.Random

class MiningService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var repository: MiningRepository
    private var isServiceMining = false
    private var miningJob: Job? = null

    companion object {
        const val CHANNEL_ID = "SatoMineBackgroundChannel"
        const val NOTIFICATION_ID = 404
        
        @Volatile
        var isRunning = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, MiningService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MiningService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val database = MiningDatabase.getDatabase(applicationContext)
        repository = MiningRepository(database.miningDao())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification("SatoMine 24/7 Background Core Engine ACTIVE", 0),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                buildNotification("SatoMine 24/7 Background Core Engine ACTIVE", 0)
            )
        }
        
        if (!isServiceMining) {
            isServiceMining = true
            startServiceMiningLoop()
        }

        return START_STICKY
    }

    private fun startServiceMiningLoop() {
        miningJob = serviceScope.launch {
            val digest = MessageDigest.getInstance("SHA-256")
            val header = ByteArray(80)
            Random.nextBytes(header)
            var count = 0L
            var sharesCount = 0

            while (isActive) {
                count++
                header[76] = (count and 0xFF).toByte()
                header[77] = ((count shr 8) and 0xFF).toByte()
                header[78] = ((count shr 16) and 0xFF).toByte()
                header[79] = ((count shr 24) and 0xFF).toByte()
                
                val hash1 = digest.digest(header)
                val doubleHash = digest.digest(hash1)
                
                // Easy difficulty requirement match to simulate background share acceptance rate elegantly
                val solved = doubleHash[0] == 0.toByte() || (doubleHash[0].toInt() and 0xF0) == 0
                if (solved && Random.nextFloat() < 0.03f) {
                    sharesCount++
                    val rewardSats = Random.nextLong(2, 6)
                    val hex = doubleHash.joinToString("") { "%02x".format(it) }
                    
                    // Increment the user's wallet database satoshis balance reactively
                    repository.addSatoshis(
                        amount = rewardSats,
                        sourceDetail = "Proof of Work Background Share (Automatic 24/7 Engine)",
                        targetAddress = hex
                    )
                    
                    updateNotification("Pertambangan Aktif: Berhasil menyelesaikan $sharesCount share (+$rewardSats SATS)")
                }

                if (count % 15000L == 0L) {
                    delay(120) // Yield CPU control to keep the mobile cool and friendly
                }
            }
        }
    }

    private fun buildNotification(contentText: String, shares: Int): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SatoMine 24/7 CPU/Cloud Engine")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setColor(0xFFF7931A.toInt()) // Bitcoin Orange LED theme color style
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = buildNotification(contentText, 0)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "SatoMine Background Mining",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceMining = false
        isRunning = false
        miningJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
