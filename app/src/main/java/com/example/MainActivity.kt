package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.TransactionEntity
import com.example.data.database.WalletEntity
import com.example.ui.MiningViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF040807) // Deep cosmic dark slate background
                ) {
                    SatomineApp()
                }
            }
        }
    }
}

// Visual color palette constants
object CyberStyle {
    val DarkSlate = Color(0xFF0A0C10)      // Artistic Flair deep dark charcoal slate
    val CardSlate = Color(0xFF16191D)      // Dark card base
    val AccentEmerald = Color(0xFFF7931A)  // Bitcoin Orange Accent
    val NeonBlue = Color(0xFF00E5FF)
    val GoldAmber = Color(0xFFFFB300)
    val AlertRed = Color(0xFFFF3C30)
    val GrayMedium = Color(0xFF2E333D)
    val GrayLight = Color(0xFF9CA3AF)
    val BorderColor = Color(0x1BFFFFFF)    // Subtle white outline border

    val EmeraldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF7931A), Color(0xFFFFB300))
    )
    val DarkGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF16191D), Color(0xFF0A0C10))
    )
    val FireGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFB300), Color(0xFFFF3D00))
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SatomineApp(viewModel: MiningViewModel = viewModel()) {
    val wallet by viewModel.walletState.collectAsState()
    val transactions by viewModel.transactionsState.collectAsState()
    val btcPrice by viewModel.btcPrice.collectAsState()
    val prcChange by viewModel.priceChange24h.collectAsState()
    
    var currentTab by remember { mutableStateOf("MINE") } // "MINE", "VAULT", "WITHDRAW", "AUDIT"

    // Scaffold with full notch & gesture safety
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberStyle.DarkSlate,
        bottomBar = {
            SatomineBottomNavBar(
                currentTab = currentTab,
                onTabSelect = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberStyle.DarkGradient)
        ) {
            // Live Status Header Board
            SatomineHeaderBoard(
                btcPrice = btcPrice,
                priceChange = prcChange,
                satsBalance = wallet?.satoshis ?: 0L,
                onRefreshPrice = { viewModel.fetchLiveBtcPrice() }
            )

            // Dynamic view based on tab selection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (currentTab) {
                    "MINE" -> MinerTabScreen(viewModel)
                    "VAULT" -> VaultTabScreen(viewModel, wallet)
                    "WITHDRAW" -> WithdrawTabScreen(viewModel, wallet?.satoshis ?: 0L)
                    "AUDIT" -> AuditTabScreen(viewModel, transactions)
                }
            }
        }
    }
}

// ---------------- HEADER BOARD ----------------
@Composable
fun SatomineHeaderBoard(
    btcPrice: Double,
    priceChange: Double,
    satsBalance: Long,
    onRefreshPrice: () -> Unit
) {
    val context = LocalContext.current
    val formattedPrice = String.format("%,.2f", btcPrice)
    val formattedChange = String.format("%+.2f%%", priceChange)
    val btcValue = satsBalance.toDouble() / 100_000_000.0

    // Approximate IDR from BTC Price (e.g. 1 BTC = $93,850 * 16,100 IDR)
    val approxIdrValue = btcValue * btcPrice * 16100.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(24.dp)), // Elegant, hyper-rounded corners for Artistic Flair
        colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top row with green pulse dot & AES-256 label and Secure Node v8.4 badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing LED lamp representing military grade vault active connection
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF34D399), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AES-256 ENCRYPTED",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        letterSpacing = 1.8.sp
                    )
                }

                // Node Spec Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SECURE NODE V8.4",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Balances Area with Ambient glow orb overlay representation
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "SALDO TERSEDIA (BTC)",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Huge, high-contrast, black-italic typographic balance
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.testTag("sats_balance_display")
                    ) {
                        val btcStr = String.format("%.8f", btcValue)
                        val mainPart = btcStr.substring(0, btcStr.length - 3)
                        val accentPart = btcStr.substring(btcStr.length - 3)

                        Text(
                            text = mainPart,
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-1.5).sp
                        )
                        Text(
                            text = accentPart,
                            color = CyberStyle.AccentEmerald, // Energetic Orange
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "≈ Rp ${String.format("%,.2f", approxIdrValue)}",
                        color = CyberStyle.AccentEmerald,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Equivalent: $satsBalance SATS (Sekecil apapun dapat ditarik)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Ambient glow behind the balance numbers
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(CyberStyle.AccentEmerald.copy(alpha = 0.12f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

            // Sub row info (Ticker price status / change indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE TICKER",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\$${formattedPrice}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedChange,
                            color = if (priceChange >= 0) Color(0xFF34D399) else Color(0xFFFF3C30),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .background(Color(0x22F7931A), RoundedCornerShape(12.dp))
                        .clickable { onRefreshPrice() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REFRESH POOL",
                        color = CyberStyle.AccentEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = CyberStyle.AccentEmerald,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}


// ---------------- BOTTOM NAVIGATION BAR ----------------
@Composable
fun SatomineBottomNavBar(
    currentTab: String,
    onTabSelect: (String) -> Unit
) {
    NavigationBar(
        containerColor = CyberStyle.CardSlate,
        tonalElevation = 8.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = CyberStyle.BorderColor,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        )
    ) {
        val navItems = listOf(
            Triple("MINE", "Tambang", Icons.Default.PlayArrow),
            Triple("VAULT", "Kubah", Icons.Default.Lock),
            Triple("WITHDRAW", "Tarik LN", Icons.Default.Send),
            Triple("AUDIT", "Audit AI", Icons.Default.Info)
        )

        navItems.forEach { (tabId, label, icon) ->
            val isSelected = currentTab == tabId
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(tabId) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.Black else CyberStyle.GrayLight
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (isSelected) CyberStyle.AccentEmerald else CyberStyle.GrayLight,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CyberStyle.AccentEmerald,
                    selectedIconColor = Color.Black,
                    unselectedIconColor = CyberStyle.GrayLight
                ),
                modifier = Modifier.testTag("nav_item_$tabId")
            )
        }
    }
}

// ---------------- MINE BLOCK TAB SCREEN ----------------
@Composable
fun MinerTabScreen(viewModel: MiningViewModel) {
    val isMining by viewModel.isMining.collectAsState()
    val raceStatus by viewModel.raceStatus.collectAsState()
    val currentProgress by viewModel.currentBlockProgress.collectAsState()
    val opponentProgress by viewModel.opponentBlockProgress.collectAsState()
    val logs by viewModel.miningPoolLogs.collectAsState()
    val currentReward by viewModel.currentBlockRewardSats.collectAsState()
    val opponentName by viewModel.competingNodeName.collectAsState()
    val difficultyHex by viewModel.difficultyHex.collectAsState()

    // Real-Time physical CPU telemetry parameters
    val liveHashRate by viewModel.liveHashRate.collectAsState()
    val minerThreadsCount by viewModel.minerThreadsCount.collectAsState()
    val selectedDifficultyLevel by viewModel.selectedDifficultyLevel.collectAsState()
    val sharesSecuredCount by viewModel.sharesSecuredCount.collectAsState()
    val currentMinedBlockHeight by viewModel.currentMinedBlockHeight.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Target Block Info & Sync Detail
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MINING TEMPLATE #$currentMinedBlockHeight",
                            color = CyberStyle.AccentEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isMining) Color(0x3334D399) else Color(0x22FFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isMining) "RUNNING" else "CPU IDLE",
                                color = if (isMining) Color(0xFF34D399) else CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = difficultyHex,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "BLOCK REWARD JACKPOT",
                                color = CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$currentReward SATS",
                                color = CyberStyle.GoldAmber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ACTIVE POOL SHARES SECURED",
                                color = CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$sharesSecuredCount Shares",
                                color = CyberStyle.NeonBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // TELEMETRY DISPLAY PANEL (Big numbers of core hashrate computations)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAL TELEMETRY CPU HASHRATE",
                        color = CyberStyle.GrayLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            val formattedHash = if (liveHashRate >= 1000.0) {
                                String.format("%.2f KH/s", liveHashRate / 1000.0)
                            } else {
                                String.format("%.0f H/s", liveHashRate)
                            }
                            Text(
                                text = formattedHash,
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Kecepatan murni CPU hardware Anda",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        
                        // Estimated calculated energy consumption
                        Column(horizontalAlignment = Alignment.End) {
                            val estimatedPower = minerThreadsCount * 380 /* 380 mW per core */
                            Text(
                                text = "POWER EST.",
                                color = CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "~${estimatedPower} mW",
                                color = CyberStyle.AlertRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // MULTI-THREAD CORE ALLOCATION TUNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ALOKASI CORE PROSESOR (THREADS)",
                        color = CyberStyle.NeonBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 4, 8).forEach { cores ->
                            val active = minerThreadsCount == cores
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (active) CyberStyle.NeonBlue else Color(0xFF1E2228),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setMinerThreads(cores) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$cores Core",
                                    color = if (active) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // POOL DIFFICULTY ENERGETIC MATCHERS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FILTRASI FILTER KESULITAN SHARE POOL",
                        color = CyberStyle.GoldAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mengatur target kriteria double SHA-256 local share proof-of-work",
                        color = CyberStyle.GrayLight,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "EASY" to "x16",
                            "MEDIUM" to "x256",
                            "HARD" to "x4K",
                            "INSANE" to "x65K"
                        ).forEach { (level, prob) ->
                            val isSelected = selectedDifficultyLevel == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) CyberStyle.GoldAmber else Color(0xFF1E2228),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setDifficultyLevel(level) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = level,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = prob,
                                        color = if (isSelected) Color.Black.copy(alpha = 0.7f) else CyberStyle.GrayLight,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 24/7 BACKGROUND MINING SERVICE CONTROLLER
        item {
            val isBgMining by viewModel.isBackgroundMiningActive.collectAsState()
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isBgMining) CyberStyle.AccentEmerald else CyberStyle.BorderColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERTAMBANGAN CO-PROSESOR 24/7",
                            color = if (isBgMining) CyberStyle.AccentEmerald else CyberStyle.NeonBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isBgMining) Color(0x3334D399) else Color(0x1BFFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isBgMining) "BG-ACTIVE" else "BG-OFF",
                                color = if (isBgMining) Color(0xFF34D399) else CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sistem SatoMine terus menjalankan hash double SHA-256 asli di latar belakang (background) 24/7 secara hemat daya di balik layar.",
                        color = CyberStyle.GrayLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { viewModel.toggleBackgroundMining(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBgMining) CyberStyle.AlertRed else CyberStyle.AccentEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("toggle_bg_mining_btn")
                    ) {
                        Text(
                            text = if (isBgMining) "HENTIKAN PENAMBANGAN BACKGROUND" else "AKTIFKAN PENAMBANGAN BACKGROUND (24/7)",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // CONTROL BOARD / BATTLE METERS
        item {
            AnimatedContent(
                targetState = raceStatus,
                transitionSpec = {
                    slideInVertically { h -> h } + fadeIn() togetherWith
                            slideOutVertically { h -> -h } + fadeOut()
                },
                label = "control_board"
            ) { status ->
                when (status) {
                    "IDLE" -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Miner IDLE",
                                    tint = CyberStyle.AccentEmerald,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Ready to Begin Real CPU Core Hashing?",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "SatoMine akan menjalankan perhitungan double SHA-256 asli di prosesor handphone Anda. Semakin tinggi thread dan difficulty, semakin banyak SATS diperoleh!",
                                    color = CyberStyle.GrayLight,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.startMiningBlockRace() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberStyle.AccentEmerald),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("start_race_btn")
                                ) {
                                    Text(
                                        text = "MULAI MENAMBANG (CPU HASH)",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    "RACING" -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberStyle.NeonBlue, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF071410)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ ADU KECEPATAN CPU AKTIF!",
                                        color = CyberStyle.AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "BLOCK MINING RACE",
                                        color = CyberStyle.AlertRed,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Your share progress
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Node Lokal Anda (User Shares)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${(currentProgress * 100).toInt()}% Block Solve",
                                            color = CyberStyle.AccentEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { currentProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = CyberStyle.AccentEmerald,
                                        trackColor = Color(0xFF1E352C)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Opponent Global Network progress
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Opponent: $opponentName",
                                            color = CyberStyle.GrayLight,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${(opponentProgress * 100).toInt()}% Block solved",
                                            color = CyberStyle.AlertRed,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { opponentProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = CyberStyle.AlertRed,
                                        trackColor = Color(0xFF351F1E)
                                    )
                                }
                            }
                        }
                    }

                    "WON", "LOST" -> {
                        val stateIsWin = status == "WON"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.6.dp,
                                    color = if (stateIsWin) CyberStyle.AccentEmerald else CyberStyle.AlertRed,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (stateIsWin) Color(0xFF0F2B20) else Color(0xFF261010)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (stateIsWin) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = status,
                                    tint = if (stateIsWin) CyberStyle.AccentEmerald else CyberStyle.AlertRed,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (stateIsWin) "BLOCK BERHASIL DIPECAHKAN!" else "BLOCK DIREALY KOMPETITOR!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (stateIsWin)
                                        "Selamat! local node Anda memenangkan perlombaan block dan mengkreditkan +$currentReward Satoshis block jackpot reward ke dompet Anda."
                                    else
                                        "Meskipun kompetitor melompat ke block baru duluan, seluruh SATS yang terselesaikan dari $sharesSecuredCount shares di hardware CPU Anda tetap tersimpan utuh dan aman di saldo dompet Anda!",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.resetRace() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (stateIsWin) CyberStyle.AccentEmerald else CyberStyle.GrayMedium
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("race_dismiss_btn")
                                ) {
                                    Text(
                                        text = if (stateIsWin) "AMBIL BLOK BERIKUTNYA" else "COBA LAGI (REBUT BLOCK)",
                                        fontWeight = FontWeight.Bold,
                                        color = if (stateIsWin) Color.Black else Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tapping Overclock manual boost trigger during active race
        if (raceStatus == "RACING") {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(20.dp, CircleShape, spotColor = CyberStyle.AccentEmerald)
                            .border(2.dp, CyberStyle.AccentEmerald, CircleShape)
                            .clip(CircleShape)
                            .clickable { viewModel.injectUserHashPower() }
                            .testTag("inject_hash_btn"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F261F))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "OVERCLOCK",
                                color = CyberStyle.AccentEmerald,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TAP UNTUK BOOST",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SCROLLING LOG TERMINAL (Authentic stratum protocol ticker displays)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF050B09)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CONSOLE STRATUM MONITORING UTAMA",
                        color = CyberStyle.NeonBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Divider(color = CyberStyle.BorderColor, modifier = Modifier.padding(bottom = 6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        reverseLayout = false
                    ) {
                        if (logs.isEmpty()) {
                            item {
                                Text(
                                    text = "> Standard Node: Terkoneksi. Siap menghitung...\n> Pilih Core dan Kesulitan, lalu klik 'MULAI MENAMBANG' untuk menjalankan kalkulasi SHA-256 riil.",
                                    color = CyberStyle.GrayLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            items(logs) { log ->
                                Text(
                                    text = "> $log",
                                    color = if (log.contains("Core")) CyberStyle.AccentEmerald else if (log.contains("Telemetry")) CyberStyle.NeonBlue else if (log.contains("Stratum")) CyberStyle.GoldAmber else Color.White,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- VAULT / SECURE SECURITY TAB ----------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultTabScreen(viewModel: MiningViewModel, wallet: WalletEntity?) {
    val mnemonic by viewModel.generatedMnemonic.collectAsState()
    val hasBackup by viewModel.hasSecurityBackUp.collectAsState()
    val encryptionInfo by viewModel.encryptionStandard.collectAsState()

    var showMnemonicSecrets by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vault Banner Status Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield",
                        tint = CyberStyle.AccentEmerald,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "BRANKAS TERENKRIPSI AES-256",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tinggi Keamanan: Lapisan SHA-256 Berulang ganda pada penyimpanan lokal SQLite.",
                            fontSize = 11.sp,
                            color = CyberStyle.GrayLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Mnemonics and Private keys Area
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "12-KATA KUNCI PEMULIHAN DOMPET (SEED)",
                        color = CyberStyle.GoldAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kunci ini dienkripsi secara lokal di perangkat Anda. Jangan bagikan kepada siapa pun!",
                        color = CyberStyle.GrayLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (showMnemonicSecrets) {
                        // Display mnemonic in layout grid
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            maxItemsInEachRow = 3,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mnemonic.forEachIndexed { idx, word ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0C1613), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF1E352C), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${idx + 1}. $word",
                                        color = CyberStyle.AccentEmerald,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color(0xFF030807), RoundedCornerShape(8.dp))
                                .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(8.dp))
                                .clickable { showMnemonicSecrets = true }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒 KETUK UNTUK MEMBUKA KUNCI SEED MNEMONIC",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasBackup,
                            onCheckedChange = { viewModel.toggleBackupConfirmed(it) },
                            colors = CheckboxDefaults.colors(checkedColor = CyberStyle.AccentEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saya sudah mencatat 12-kata kunci seed ini dengan aman & offline.",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Stratum Connection specs
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STATION CONNECTION & SPECS",
                        color = CyberStyle.NeonBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SpecificationItem("Active Stratum Pool", wallet?.activeNodeAddress ?: "pool.satomine.secure:3333")
                    SpecificationItem("Double SHA Armor", "ACTIVATED (Double Hashing standard 512-bit secure)")
                    SpecificationItem("Simulated Device Power", "Adaptive Th/s (Sesuai frekuensi ketukan)")
                    SpecificationItem("Encrypted Local Vault", encryptionInfo)
                }
            }
        }

        // Reset system data
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚨 HAPUS DOMPET & RESET SISTEM",
                    color = CyberStyle.AlertRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color(0xFF2B1010), RoundedCornerShape(8.dp))
                        .clickable { viewModel.wipeWalletData() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("wipe_data_btn")
                )
            }
        }
    }
}

@Composable
fun SpecificationItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = CyberStyle.GrayLight, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

// ---------------- LIGHTNING WITHDRAW / FAUCET CLAIM TAB ----------------
@Composable
fun WithdrawTabScreen(viewModel: MiningViewModel, currentSats: Long) {
    val status by viewModel.withdrawalStatus.collectAsState()
    val message by viewModel.withdrawalMessage.collectAsState()
    val txId by viewModel.lastTxId.collectAsState()
    val btcPrice by viewModel.btcPrice.collectAsState()

    var selectedNetwork by remember { mutableStateOf("LIGHTNING") } // "LIGHTNING", "ON_CHAIN", "INDODAX_IDR"

    var satsAmountStr by remember { mutableStateOf("") }
    var destinationAddress by remember { mutableStateOf("") } // Lightning invoice OR OnChain BTC Address OR Bank account number
    
    // Indodax specific states
    var selectedBank by remember { mutableStateOf("BCA") }
    var accountHolderName by remember { mutableStateOf("") }

    // On-Chain fee states
    var feeLevel by remember { mutableStateOf("REGULAR") } // "SLOW", "REGULAR", "PRIORITY"
    val networkFeeSats = when (feeLevel) {
        "SLOW" -> 40L
        "REGULAR" -> 150L
        "PRIORITY" -> 350L
        else -> 150L
    }

    // Satoshis to IDR converter helper (1 BTC = $btcPrice * 16,100 IDR)
    val idrRatePerSatoshi = (btcPrice * 16100.0) / 100_000_000.0
    val amountSats = satsAmountStr.toLongOrNull() ?: 0L
    val calculatedIdrAmount = amountSats * idrRatePerSatoshi

    val btcBalanceValue = currentSats.toDouble() / 100_000_000.0
    val totalIdrBalance = btcBalanceValue * btcPrice * 16100.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Multi-tier Network Selectors (Like Indodax & Coinomi Hub)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "RUANG PENILAIAN LIQUIDITY & JALUR PENARIKAN",
                        color = CyberStyle.GrayLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("LIGHTNING", "⚡ Lightning", Color(0xFFF7931A)),
                            Triple("ON_CHAIN", "🪙 On-Chain", Color(0xFF00E5FF)),
                            Triple("INDODAX_IDR", "🇮🇩 Indodax IDR", Color(0xFFFFB300))
                        ).forEach { (net, label, highlightColor) ->
                            val active = selectedNetwork == net
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (active) highlightColor.copy(alpha = 0.2f) else Color(0xFF1A1D24),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (active) highlightColor else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { 
                                        selectedNetwork = net 
                                        satsAmountStr = ""
                                        destinationAddress = ""
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (active) Color.White else CyberStyle.GrayLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active withdraw form fields based on selected pathway
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    // Header of Selected Section
                    when (selectedNetwork) {
                        "LIGHTNING" -> {
                            Text(
                                text = "PENARIKAN INSTAN LIGHTNING NETWORK (0 FEE)",
                                color = CyberStyle.AccentEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pencairan super cepat tanpa potongan. Minimum penarikan hanya 1 SAT. Cocok untuk micro-earnings wallet (Coinomi, Muun, dll).",
                                color = CyberStyle.GrayLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        "ON_CHAIN" -> {
                            Text(
                                text = "PENARIKAN BITCOIN MAINNET ON-CHAIN DIRECT",
                                color = CyberStyle.NeonBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kirim langsung ke Wallet Publik Bitcoin Anda (Coinomi, Trust, Indodax Address). Membutuhkan biaya transaksi penambang (Miner Fee).",
                                color = CyberStyle.GrayLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        "INDODAX_IDR" -> {
                            Text(
                                text = "PENCAIRAN INDODAX BANK GARIS & RUPIAHD (IDR)",
                                color = CyberStyle.GoldAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Konversi Satoshis hasil menambang Anda langsung menjadi uang Rupiah tunai ke rekening Bank lokal atau E-Wallet Indonesia terdaftar.",
                                color = CyberStyle.GrayLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    // Common Satoshis Amount Field
                    OutlinedTextField(
                        value = satsAmountStr,
                        onValueChange = { satsAmountStr = it },
                        label = { Text("Jumlah Penarikan (SATS)", fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberStyle.AccentEmerald,
                            unfocusedBorderColor = CyberStyle.BorderColor,
                            focusedLabelColor = CyberStyle.AccentEmerald,
                            unfocusedLabelColor = CyberStyle.GrayLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        singleLine = true
                    )

                    // REAL-TIME RUPIAH ESTIMATION BOX
                    if (amountSats > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F1412), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0x3334D399), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimasi Kas Keluar (Rupiah):",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Text(
                                    text = "Rp ${String.format("%,.2f", calculatedIdrAmount)}",
                                    color = CyberStyle.AccentEmerald,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SELECTIVE FIELD BASES
                    when (selectedNetwork) {
                        "LIGHTNING" -> {
                            OutlinedTextField(
                                value = destinationAddress,
                                onValueChange = { destinationAddress = it },
                                label = { Text("Lightning Invoice / LNURL Address", fontFamily = FontFamily.Monospace) },
                                placeholder = { Text("contoh: lnbc10u1pj...", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberStyle.AccentEmerald,
                                    unfocusedBorderColor = CyberStyle.BorderColor,
                                    focusedLabelColor = CyberStyle.AccentEmerald,
                                    unfocusedLabelColor = CyberStyle.GrayLight,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_invoice_input"),
                                singleLine = true
                            )
                        }
                        "ON_CHAIN" -> {
                            OutlinedTextField(
                                value = destinationAddress,
                                onValueChange = { destinationAddress = it },
                                label = { Text("Alamat Bitcoin Dompet (Coinomi/BTC)", fontFamily = FontFamily.Monospace) },
                                placeholder = { Text("bc1q..., 1..., atau 3...", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberStyle.NeonBlue,
                                    unfocusedBorderColor = CyberStyle.BorderColor,
                                    focusedLabelColor = CyberStyle.NeonBlue,
                                    unfocusedLabelColor = CyberStyle.GrayLight,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_btc_address_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Gas/Mining Fee selector
                            Text(
                                text = "BIAYA TRANSAKSI BLOCKCHAIN",
                                color = CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "SLOW" to "Hemat\n40 SATS (~2jam)",
                                    "REGULAR" to "Reguler\n150 SATS (~30m)",
                                    "PRIORITY" to "Prioritas\n350 SATS (~10m)"
                                ).forEach { (lvl, lbl) ->
                                    val active = feeLevel == lvl
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (active) CyberStyle.NeonBlue.copy(alpha = 0.2f) else Color(0xFF1E2228),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (active) CyberStyle.NeonBlue else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { feeLevel = lvl }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lbl,
                                            color = if (active) Color.White else CyberStyle.GrayLight,
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        "INDODAX_IDR" -> {
                            // Bank selector dropdown row
                            Text(
                                text = "PILIH BANK / E-WALLET TUJUAN",
                                color = CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("BCA", "MANDIRI", "BNI", "BRI", "GOPAY", "OVO", "DANA").forEach { bank ->
                                    val active = selectedBank == bank
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (active) CyberStyle.GoldAmber.copy(alpha = 0.2f) else Color(0xFF1E2228),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (active) CyberStyle.GoldAmber else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedBank = bank }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bank,
                                            color = if (active) Color.White else CyberStyle.GrayLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bank account input field
                            OutlinedTextField(
                                value = destinationAddress,
                                onValueChange = { destinationAddress = it },
                                label = { Text("Nomor Rekening / ID E-Wallet", fontFamily = FontFamily.Monospace) },
                                placeholder = { Text("e.g. 801123490 atau No Telepon", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberStyle.GoldAmber,
                                    unfocusedBorderColor = CyberStyle.BorderColor,
                                    focusedLabelColor = CyberStyle.GoldAmber,
                                    unfocusedLabelColor = CyberStyle.GrayLight,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_bank_account_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Account holder name field
                            OutlinedTextField(
                                value = accountHolderName,
                                onValueChange = { accountHolderName = it },
                                label = { Text("Nama Pemilik Rekening / E-Wallet", fontFamily = FontFamily.Monospace) },
                                placeholder = { Text("e.g. Dafitrah", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberStyle.GoldAmber,
                                    unfocusedBorderColor = CyberStyle.BorderColor,
                                    focusedLabelColor = CyberStyle.GoldAmber,
                                    unfocusedLabelColor = CyberStyle.GrayLight,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_account_name_input"),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PRESET QUICK SELECTION BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                satsAmountStr = "25"
                                when (selectedNetwork) {
                                    "LIGHTNING" -> destinationAddress = "lnbc25u1p39puxq..."
                                    "ON_CHAIN" -> destinationAddress = "bc1q58zaxkm9qsn27asdfnklqy98uaxz5"
                                    "INDODAX_IDR" -> {
                                        destinationAddress = "0812948242"
                                        accountHolderName = "Dafitrah"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F2B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text("25 SATS", fontSize = 10.sp, color = CyberStyle.AccentEmerald, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                if (currentSats > 0) {
                                    val safeAmt = if (selectedNetwork == "ON_CHAIN") {
                                        (currentSats - networkFeeSats).coerceAtLeast(0L)
                                    } else {
                                        currentSats
                                    }
                                    satsAmountStr = safeAmt.toString()
                                }
                                when (selectedNetwork) {
                                    "LIGHTNING" -> destinationAddress = "lnbc10u1pj98gsp258as..."
                                    "ON_CHAIN" -> destinationAddress = "bc1q2z89hasdxmksqy82hzka98uhas01"
                                    "INDODAX_IDR" -> {
                                        destinationAddress = "0112480392"
                                        accountHolderName = "Dafitrah"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2833)),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text("MAKS SALDO", fontSize = 10.sp, color = CyberStyle.NeonBlue, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Submission trigger
                    Button(
                        onClick = {
                            val amount = satsAmountStr.toLongOrNull() ?: 0L
                            viewModel.requestAdvancedWithdrawal(
                                amountSats = amount,
                                destination = destinationAddress,
                                networkType = selectedNetwork,
                                networkFeeSats = if (selectedNetwork == "ON_CHAIN") networkFeeSats else 0L,
                                bankName = if (selectedNetwork == "INDODAX_IDR") selectedBank else "",
                                accountHolder = if (selectedNetwork == "INDODAX_IDR") accountHolderName else ""
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedNetwork) {
                                "LIGHTNING" -> CyberStyle.AccentEmerald
                                "ON_CHAIN" -> CyberStyle.NeonBlue
                                "INDODAX_IDR" -> CyberStyle.GoldAmber
                                else -> CyberStyle.AccentEmerald
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("withdraw_submit_btn"),
                        enabled = (status == "IDLE" || status == "SUCCESS" || status == "ERROR") && satsAmountStr.isNotEmpty() && destinationAddress.isNotEmpty()
                    ) {
                        Text(
                            text = "LUNCURKAN PENARIKAN REAL",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Processing Dialog / Tunnel simulation
        if (status != "IDLE") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = when (status) {
                                "SUCCESS" -> CyberStyle.AccentEmerald
                                "ERROR" -> CyberStyle.AlertRed
                                else -> CyberStyle.NeonBlue
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070E0D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (status != "SUCCESS" && status != "ERROR") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CyberStyle.NeonBlue,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "SATOMINE SECURE LEDGER: $status",
                                fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    "SUCCESS" -> CyberStyle.AccentEmerald
                                    "ERROR" -> CyberStyle.AlertRed
                                    else -> CyberStyle.NeonBlue
                                },
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = message,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        if (status == "SUCCESS" && txId.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "TXID SECURE LEDGER:\n$txId",
                                color = CyberStyle.AccentEmerald,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mempool status: disiarkan (broadcasting). Dana ditransfer ke alamat tujuan dengan perlindungan enkripsi AES-256.",
                                color = CyberStyle.GrayLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (status == "SUCCESS" || status == "ERROR") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.resetWithdrawalState() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberStyle.GrayMedium),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Text("TUTUP PANEL STATUS", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- LEDGERS & AI STRATEGY AUDIT (GEMINI) TAB ----------------
@Composable
fun AuditTabScreen(viewModel: MiningViewModel, transactions: List<TransactionEntity>) {
    val aiReport by viewModel.geminiReport.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingAI.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Audit Generation Card (Uses Server-Side Gemini API!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberStyle.NeonBlue, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1214)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GEMINI 3.5 SECURITY AI AUDIT",
                            color = CyberStyle.NeonBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF101D21), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI ACTIVE",
                                color = CyberStyle.NeonBlue,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gunakan asisten kecerdasan buatan Gemini untuk mengaudit status keamanan jaringan blockchain, tingkat kesulitan real-time, dan verifikasi pool.",
                        color = CyberStyle.GrayLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Text result container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF04090A), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        if (isAnalyzing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = CyberStyle.NeonBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Gemini sedang membuat laporan kriptografik...",
                                    color = CyberStyle.GrayLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                text = aiReport,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.runAiSecurityAudit() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberStyle.NeonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("ai_audit_btn"),
                        enabled = !isAnalyzing
                    ) {
                        Text(
                            text = "MULAI AUDIT KEAMANAN AI",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // History logs header
        item {
            Text(
                text = "BUKU BESAR HASH & TRANSAKSI (SQUEEZED LEDGER)",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Flowing Transaction rows
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada transaksi. Mulailah memecahkan block di tab 'Tambang'!",
                            color = CyberStyle.GrayLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionRow(tx)
            }
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val isSolve = tx.type == "SOLVE"
    val sdf = SimpleDateFormat("HH:mm - dd MMM 2026", Locale.getDefault())
    val formattedDate = sdf.format(Date(tx.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberStyle.BorderColor, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberStyle.CardSlate),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (isSolve) Color(0xFF142C1E) else Color(0xFF381A1A),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSolve) Icons.Default.PlayArrow else Icons.Default.KeyboardArrowUp,
                            contentDescription = tx.type,
                            tint = if (isSolve) CyberStyle.AccentEmerald else CyberStyle.AlertRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSolve) "Blok Terpecahkan" else "Penarikan Lightning",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp
                    )
                }

                Text(
                    text = if (isSolve) "+${tx.amountSats} SAT" else "-${tx.amountSats} SAT",
                    color = if (isSolve) CyberStyle.AccentEmerald else CyberStyle.AlertRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.5.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tx.details,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TXID: ${tx.txHash.substring(0, 8)}...${tx.txHash.substring(tx.txHash.length - 8)}",
                    color = CyberStyle.GrayLight,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formattedDate,
                    color = CyberStyle.GrayLight,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
