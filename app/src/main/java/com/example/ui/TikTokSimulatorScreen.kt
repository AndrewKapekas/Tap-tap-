package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TapConfig
import com.example.ui.components.HeartParticle
import com.example.ui.components.SingleAnimatedHeart
import com.example.ui.components.createRandomHeart
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonHeartCyan
import com.example.ui.theme.NeonHeartGold
import com.example.ui.theme.NeonHeartPink
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LiveComment(
    val id: Long,
    val username: String,
    val text: String,
    val avatarBg: Color
)

@Composable
fun TikTokSimulatorScreen(
    initialConfig: TapConfig,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var likesCount by remember { mutableLongStateOf(28400L) }
    var comboCount by remember { mutableIntStateOf(0) }
    var currentCps by remember { mutableFloatStateOf(0f) }
    var isAutoTapping by remember { mutableStateOf(false) }

    // Speed configuration
    var targetCps by remember { mutableFloatStateOf(initialConfig.estimatedCps.coerceIn(5f, 60f)) }
    var isSafeModeActive by remember { mutableStateOf(initialConfig.safeMode) }

    // Heart particles
    val activeHearts = remember { mutableStateListOf<HeartParticle>() }
    var heartIdCounter by remember { mutableLongStateOf(0L) }

    // Tap timestamp tracker for calculating real-time CPS
    val tapTimestamps = remember { mutableListOf<Long>() }

    // Simulated live chat comments
    val comments = remember {
        mutableStateListOf(
            LiveComment(1L, "Nikos99", "Γεια σου Έλενα! Πάμε δυνατά! 🔥", NeonHeartPink),
            LiveComment(2L, "TikTokFan_GR", "Ταπ ταπ όλοι στην οθόνη! ❤️❤️❤️", TikTokCyan),
            LiveComment(3L, "Katerina_L", "Πάμε για 100K likes σήμερα!!", NeonHeartGold),
            LiveComment(4L, "Alex_Athens", "Tap tap non stop!", NeonHeartPink)
        )
    }

    val simulatedFeed = listOf(
        "Πάμε παιδιά ταπ ταπ στην οθόνη! ❤️",
        "Μπράβο σε όλους! 🚀",
        "Sending 5x Roses 🌹🌹🌹",
        "Τρομερή ταχύτητα στα likes!",
        "Double tap to boost the live! 🔥",
        "Πάμε για το battle! ⚔️",
        "Keep tapping guys! ❤️❤️❤️",
        "100K Likes incoming!! 💥"
    )

    // Background comment spawner
    LaunchedEffect(Unit) {
        var commentId = 5L
        while (isActive) {
            delay(Random.nextLong(1400L, 2600L))
            val randomUser = "User_${Random.nextInt(100, 999)}"
            val randomText = simulatedFeed[Random.nextInt(simulatedFeed.size)]
            val randomColor = listOf(NeonHeartPink, TikTokCyan, NeonHeartGold)[Random.nextInt(3)]
            comments.add(LiveComment(commentId++, randomUser, randomText, randomColor))
            if (comments.size > 20) {
                comments.removeAt(0)
            }
        }
    }

    // Periodic CPS calculator (rolling 1000ms window)
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(100L)
            val now = System.currentTimeMillis()
            synchronized(tapTimestamps) {
                tapTimestamps.removeAll { now - it > 1000L }
                currentCps = tapTimestamps.size.toFloat()
            }
            if (currentCps == 0f && comboCount > 0) {
                comboCount = 0
            }
        }
    }

    fun triggerSingleTap(x: Float, y: Float) {
        likesCount++
        comboCount++
        synchronized(tapTimestamps) {
            tapTimestamps.add(System.currentTimeMillis())
        }
        val p = createRandomHeart(heartIdCounter++, x, y)
        if (activeHearts.size < 40) {
            activeHearts.add(p)
        }

        // Haptic feedback
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(3L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(3L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {}
    }

    // In-Simulator Auto-Tap coroutine
    LaunchedEffect(isAutoTapping, targetCps, isSafeModeActive) {
        if (!isAutoTapping) return@LaunchedEffect

        var simHeartX = 650f
        var simHeartY = 1200f

        while (isActive && isAutoTapping) {
            val intervalMs = (1000f / targetCps).toLong().coerceIn(10L, 500L)
            val jitterX = if (isSafeModeActive) Random.nextFloat() * 20f - 10f else 0f
            val jitterY = if (isSafeModeActive) Random.nextFloat() * 20f - 10f else 0f
            val extraDelay = if (isSafeModeActive) Random.nextLong(-8L, 9L) else 0L

            triggerSingleTap(simHeartX + jitterX, simHeartY + jitterY)

            val finalDelay = (intervalMs + extraDelay).coerceAtLeast(8L)
            delay(finalDelay)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val defaultHeartOriginX = screenWidth * 0.75f
        val defaultHeartOriginY = screenHeight * 0.65f

        // 1. Live Stream Simulation Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1430),
                            Color(0xFF120C1F),
                            Color(0xFF090610)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        triggerSingleTap(offset.x, offset.y)
                    }
                }
        ) {
            // Ambient streamer lighting glow effect
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                TikTokPink.copy(alpha = 0.15f),
                                TikTokCyan.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Centered Streamer Avatar & Visual Cue
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, TikTokPink, CircleShape)
                        .background(Color(0xFF2B2245)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "TikTok Streamer",
                        tint = TikTokPink,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Πατήστε οπουδήποτε για Ταπ Ταπ!",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Animated Floating Hearts Overlay
        for (heart in activeHearts) {
            SingleAnimatedHeart(
                particle = heart,
                onFinished = { finishedId ->
                    activeHearts.removeAll { it.id == finishedId }
                }
            )
        }

        // 3. TikTok Live Header (Top Bar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Streamer Badge
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0x991E1633),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("sim_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Πίσω",
                                tint = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(TikTokPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@elena_live",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE50914))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "19.8K θεατές",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Likes & CPS Stat Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // CPS Meter
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (currentCps > 20f) TikTokPink.copy(alpha = 0.25f) else Color(0x99241B3B),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "CPS",
                                tint = if (currentCps > 20f) TikTokCyan else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f CPS".format(currentCps),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Reset button
                    IconButton(
                        onClick = {
                            likesCount = 0L
                            comboCount = 0
                            activeHearts.clear()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sim_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Επαναφορά",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Goal Progress Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xAA161224)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = TikTokPink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Σύνολο Likes: ${"%,d".format(likesCount)}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Στόχος: 100K",
                            color = TikTokCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = (likesCount.toFloat() / 100000f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TikTokPink,
                        trackColor = Color(0xFF2D2447)
                    )
                }
            }
        }

        // 4. Floating Combo Multiplier
        AnimatedVisibility(
            visible = comboCount > 15,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = TikTokPink,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = NeonHeartGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "x$comboCount COMBO!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 5. Simulated TikTok Live Chat Stream (Bottom Left)
        val chatListState = rememberLazyListState()
        LaunchedEffect(comments.size) {
            if (comments.isNotEmpty()) {
                chatListState.animateScrollToItem(comments.size - 1)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 175.dp)
                .fillMaxWidth(0.68f)
        ) {
            LazyColumn(
                state = chatListState,
                modifier = Modifier.height(130.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(comments, key = { it.id }) { comment ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x99171226)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${comment.username}: ",
                                color = comment.avatarBg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = comment.text,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Simulator Bottom Control Panel
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE1A152E)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Auto-Tap Action Button
                Button(
                    onClick = {
                        isAutoTapping = !isAutoTapping
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sim_toggle_autotap_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAutoTapping) Color(0xFFDC2626) else TikTokPink
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isAutoTapping) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAutoTapping) "ΔΙΑΚΟΠΗ AUTO TAP TAP" else "ΕΝΑΡΞΗ AUTO TAP TAP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Speed Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = targetCps == 15f && isSafeModeActive,
                        onClick = {
                            targetCps = 15f
                            isSafeModeActive = true
                        },
                        label = { Text("Safe 15 CPS") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TikTokCyan
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TikTokCyan.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = targetCps == 25f && !isSafeModeActive,
                        onClick = {
                            targetCps = 25f
                            isSafeModeActive = false
                        },
                        label = { Text("Fast 25") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TikTokPink.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = targetCps == 45f,
                        onClick = {
                            targetCps = 45f
                            isSafeModeActive = false
                        },
                        label = { Text("Turbo 45") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonHeartGold.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ταχύτητα: ${targetCps.toInt()} / sec",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.width(110.dp)
                    )
                    Slider(
                        value = targetCps,
                        onValueChange = { targetCps = it },
                        valueRange = 5f..60f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = TikTokPink,
                            activeTrackColor = TikTokPink,
                            inactiveTrackColor = Color(0xFF352B52)
                        )
                    )
                }
            }
        }
    }
}
