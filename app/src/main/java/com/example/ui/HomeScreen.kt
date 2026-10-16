package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TapPreferences
import com.example.model.TapConfig
import com.example.service.FloatingOverlayService
import com.example.service.TapTapAccessibilityService
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonHeartGold
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceWhite
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokPurple
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSimulator: () -> Unit
) {
    val context = LocalContext.current
    val tapPreferences = remember { TapPreferences(context) }
    var config by remember { mutableStateOf(tapPreferences.loadConfig()) }

    // Live state tracking
    val isAccessibilityActive by TapTapAccessibilityService.isServiceActive.collectAsStateWithLifecycle()
    val isOverlayActive by FloatingOverlayService.isOverlayRunning.collectAsStateWithLifecycle()
    val isTappingActive by FloatingOverlayService.isTappingActive.collectAsStateWithLifecycle()
    val tapCount by FloatingOverlayService.tapCountState.collectAsStateWithLifecycle()

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var dialogPermissionType by remember { mutableStateOf("none") }

    // Polling overlay permission status periodically when screen is in focus
    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
            delay(1000L)
        }
    }

    fun updateConfig(newConfig: TapConfig) {
        config = newConfig
        tapPreferences.saveConfig(newConfig)
    }

    fun startFloatingService() {
        if (!hasOverlayPermission) {
            dialogPermissionType = "overlay"
            showPermissionDialog = true
            return
        }
        if (!isAccessibilityActive) {
            dialogPermissionType = "accessibility"
            showPermissionDialog = true
            return
        }

        val intent = Intent(context, FloatingOverlayService::class.java).apply {
            action = FloatingOverlayService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopFloatingService() {
        val intent = Intent(context, FloatingOverlayService::class.java).apply {
            action = FloatingOverlayService.ACTION_STOP
        }
        context.stopService(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(TikTokPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TapTap",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = OnSurfaceWhite
                            )
                            Text(
                                text = "TikTok Live Auto-Tapper",
                                fontSize = 11.sp,
                                color = TikTokCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Permission Readiness Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Κατάσταση Αδειών Συστήματος",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Accessibility Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAccessibilityActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isAccessibilityActive) TikTokCyan else NeonHeartGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Υπηρεσία Προσβασιμότητας",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceWhite
                                )
                                Text(
                                    text = if (isAccessibilityActive) "Ενεργό (Έτοιμο για taps)" else "Απαιτείται ενεργοποίηση",
                                    fontSize = 11.sp,
                                    color = if (isAccessibilityActive) TikTokCyan else OnSurfaceMuted
                                )
                            }
                        }

                        if (!isAccessibilityActive) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.testTag("enable_accessibility_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Άνοιγμα", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Overlay Permission Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasOverlayPermission) TikTokCyan else NeonHeartGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Πλωτό Παράθυρο (Overlay)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceWhite
                                )
                                Text(
                                    text = if (hasOverlayPermission) "Χορηγήθηκε (Πάνω από TikTok)" else "Απαιτείται άδεια επικάλυψης",
                                    fontSize = 11.sp,
                                    color = if (hasOverlayPermission) TikTokCyan else OnSurfaceMuted
                                )
                            }
                        }

                        if (!hasOverlayPermission) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.testTag("enable_overlay_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Άδεια", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Main Action Card (Launch Floating Widget over TikTok)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TikTokPink.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = TikTokPink,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Πλωτό Μενού για TikTok",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceWhite
                            )
                            Text(
                                text = if (isOverlayActive) "🟢 Το μενού εμφανίζεται πάνω από τις εφαρμογές" else "Εμφανίζει draggable στόχο πάνω από το TikTok Live",
                                fontSize = 12.sp,
                                color = if (isOverlayActive) TikTokCyan else OnSurfaceMuted
                            )
                        }
                    }

                    if (isOverlayActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33000000),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Κατάσταση: ${if (isTappingActive) "⚡ Πατάει..." else "⏸️ Σε αναμονή"}",
                                    color = if (isTappingActive) TikTokPink else OnSurfaceMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Taps: $tapCount",
                                    color = TikTokCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (isOverlayActive) {
                                stopFloatingService()
                            } else {
                                startFloatingService()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("launch_floating_overlay_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverlayActive) Color(0xFFDC2626) else TikTokPink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isOverlayActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOverlayActive) "ΔΙΑΚΟΠΗ ΠΛΩΤΟΥ ΜΕΝΟΥ" else "ΕΚΚΙΝΗΣΗ ΠΛΩΤΟΥ ΜΕΝΟΥ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TikTok Live Simulator Card (Interactive testing arena)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1533)),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(TikTokPink, TikTokCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Προσομοιωτής TikTok Live",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceWhite
                            )
                            Text(
                                text = "Δοκιμάστε ταχύτητες, δείτε καρδιές και μετρήστε τα CPS σας!",
                                fontSize = 12.sp,
                                color = OnSurfaceMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToSimulator,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_simulator_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TikTokCyan),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "ΑΝΟΙΓΜΑ ΠΡΟΣΟΜΟΙΩΤΗ LIVE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D0B14),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Tap Speed & Configuration Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TikTokCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ρυθμίσεις Ταχύτητας & Πατημάτων",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed Presets
                    Text(
                        text = "Προκαθορισμένες Ταχύτητες (Presets):",
                        fontSize = 12.sp,
                        color = OnSurfaceMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = config.tapIntervalMs == 55L && config.safeMode,
                            onClick = {
                                updateConfig(config.copy(tapIntervalMs = 55L, safeMode = true))
                            },
                            label = { Text("TikTok Safe", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TikTokCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TikTokCyan.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = config.tapIntervalMs == 30L && !config.safeMode,
                            onClick = {
                                updateConfig(config.copy(tapIntervalMs = 30L, safeMode = false))
                            },
                            label = { Text("Turbo (33 CPS)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TikTokPink.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = config.tapIntervalMs == 15L && !config.safeMode,
                            onClick = {
                                updateConfig(config.copy(tapIntervalMs = 15L, safeMode = false))
                            },
                            label = { Text("Ultra (60 CPS)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonHeartGold.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interval Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Χρόνος ανάμεσα σε πατήματα:",
                            fontSize = 12.sp,
                            color = OnSurfaceWhite
                        )
                        Text(
                            text = "${config.tapIntervalMs} ms (~${config.estimatedCps.toInt()} CPS)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TikTokPink
                        )
                    }
                    Slider(
                        value = config.tapIntervalMs.toFloat(),
                        onValueChange = { updateConfig(config.copy(tapIntervalMs = it.toLong())) },
                        valueRange = 10f..200f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = TikTokPink,
                            activeTrackColor = TikTokPink,
                            inactiveTrackColor = Color(0xFF352B52)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Safe Mode Anti-ban Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TikTokCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Safe Mode (Anti-Ban)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceWhite
                                )
                            }
                            Text(
                                text = "Προσθέτει τυχαίες μικροκαθυστερήσεις και απόκλιση θέσης για να προσομοιώνει ανθρώπινο δάχτυλο στο TikTok Live.",
                                fontSize = 11.sp,
                                color = OnSurfaceMuted
                            )
                        }
                        Switch(
                            checked = config.safeMode,
                            onCheckedChange = { updateConfig(config.copy(safeMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TikTokCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Number of Targets Toggle (1 target vs 2 targets)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Αριθμός Στόχων στην Οθόνη",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceWhite
                            )
                            Text(
                                text = "Πόσα σημεία θα πατάνε ταυτόχρονα στο Live",
                                fontSize = 11.sp,
                                color = OnSurfaceMuted
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = config.targetCount == 1,
                                onClick = { updateConfig(config.copy(targetCount = 1)) },
                                label = { Text("1 Στόχος") }
                            )
                            FilterChip(
                                selected = config.targetCount == 2,
                                onClick = { updateConfig(config.copy(targetCount = 2)) },
                                label = { Text("2 Στόχοι") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = OnSurfaceMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Δόνηση (Haptic Feedback)",
                                fontSize = 13.sp,
                                color = OnSurfaceWhite
                            )
                        }
                        Switch(
                            checked = config.hapticEnabled,
                            onCheckedChange = { updateConfig(config.copy(hapticEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TikTokPink
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-stop Limit
                    Text(
                        text = "Αυτόματο Σταμάτημα:",
                        fontSize = 12.sp,
                        color = OnSurfaceMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = config.stopAfterCount == 0L,
                            onClick = { updateConfig(config.copy(stopAfterCount = 0L)) },
                            label = { Text("Άπειρο", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = config.stopAfterCount == 1000L,
                            onClick = { updateConfig(config.copy(stopAfterCount = 1000L)) },
                            label = { Text("1.000", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = config.stopAfterCount == 5000L,
                            onClick = { updateConfig(config.copy(stopAfterCount = 5000L)) },
                            label = { Text("5.000", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = config.stopAfterCount == 10000L,
                            onClick = { updateConfig(config.copy(stopAfterCount = 10000L)) },
                            label = { Text("10.000", fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. TikTok Live User Guide / Οδηγός Χρήσης
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📖 Πώς να το χρησιμοποιήσετε στο TikTok Live",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val guideSteps = listOf(
                        "1. Ενεργοποιήστε τις 2 άδειες (Προσβασιμότητα & Πλωτό Παράθυρο).",
                        "2. Πατήστε «ΕΚΚΙΝΗΣΗ ΠΛΩΤΟΥ ΜΕΝΟΥ».",
                        "3. Ανοίξτε την εφαρμογή TikTok και μπείτε σε ένα Live stream.",
                        "4. Σύρετε τον φωτεινό κύκλο στόχευσης (#1) πάνω στην οθόνη του Live stream.",
                        "5. Πατήστε το κουμπί ▶️ Play στο πλωτό μενού.",
                        "6. Οι καρδιές και τα likes στέλνονται αυτόματα και ασταμάτητα!"
                    )

                    guideSteps.forEach { step ->
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            color = OnSurfaceWhite.copy(alpha = 0.85f),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Permission Alert Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = if (dialogPermissionType == "accessibility") "Απαιτείται Προσβασιμότητα" else "Απαιτείται Άδεια Πλωτού Παραθύρου",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (dialogPermissionType == "accessibility") {
                        "Για να μπορεί το TapTap να πατάει αυτόματα στην οθόνη πάνω από το TikTok, χρειάζεται να ενεργοποιήσετε την Υπηρεσία Προσβασιμότητας στις ρυθμίσεις του Android."
                    } else {
                        "Για να εμφανίζεται το πλωτό κουμπί και ο στόχος πάνω από το TikTok, πρέπει να επιτρέψετε την «Εμφάνιση πάνω από άλλες εφαρμογές»."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        if (dialogPermissionType == "accessibility") {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink)
                ) {
                    Text("Μετάβαση στις Ρυθμίσεις")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Ακύρωση")
                }
            }
        )
    }
}
