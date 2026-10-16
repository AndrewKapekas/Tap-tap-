package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.TapPreferences
import com.example.model.TapConfig
import com.example.model.TargetPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var tapPreferences: TapPreferences
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tapJob: Job? = null

    private var controlView: View? = null
    private val targetViews = mutableListOf<View>()
    private val targetPoints = mutableListOf<TargetPoint>()

    private var currentConfig = TapConfig()
    private var totalTaps = 0L
    private var isTapping = false
    private var startTimeMs = 0L

    private var playPauseButton: ImageView? = null
    private var counterTextView: TextView? = null
    private var statusTextView: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        tapPreferences = TapPreferences(this)
        currentConfig = tapPreferences.loadConfig()

        startForegroundNotification()
        _isOverlayRunning.value = true

        if (Settings.canDrawOverlays(this)) {
            setupInitialTarget()
            setupControlWidget()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentConfig = tapPreferences.loadConfig()
            }
            ACTION_STOP -> {
                stopSelf()
            }
            ACTION_TOGGLE_TAP -> {
                toggleTapping()
            }
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "taptap_overlay_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun setupInitialTarget() {
        val displayMetrics = resources.displayMetrics
        val initialX = displayMetrics.widthPixels * 0.5f
        val initialY = displayMetrics.heightPixels * 0.45f

        val targetCount = currentConfig.targetCount.coerceIn(1, 4)
        for (i in 0 until targetCount) {
            val offsetX = (i * 80).toFloat()
            val point = TargetPoint(i + 1, initialX + offsetX, initialY)
            targetPoints.add(point)
            createTargetOverlayView(point)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createTargetOverlayView(point: TargetPoint) {
        val sizeDp = 56
        val sizePx = (sizeDp * resources.displayMetrics.density).toInt()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = point.x.toInt() - (sizePx / 2)
            y = point.y.toInt() - (sizePx / 2)
        }

        val targetContainer = FrameLayout(this).apply {
            // Circular glowing background with border
            val ringDrawable = GradientDrawable().apply {
                this.shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#4D000000"))
                setStroke(4, Color.parseColor("#FE2C55"))
            }
            background = ringDrawable

            val label = TextView(this@FloatingOverlayService).apply {
                text = "${point.id}"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
            }
            val centerDot = View(this@FloatingOverlayService).apply {
                val dotDrawable = GradientDrawable().apply {
                    this.shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#25F4EE"))
                }
                background = dotDrawable
                val dotSize = (8 * resources.displayMetrics.density).toInt()
                layoutParams = FrameLayout.LayoutParams(dotSize, dotSize, Gravity.CENTER)
            }

            addView(label, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            ))
            addView(centerDot)
        }

        var initialTouchX = 0f
        var initialTouchY = 0f
        var initialWindowX = 0
        var initialWindowY = 0

        targetContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialWindowX = params.x
                    initialWindowY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialWindowX + (event.rawX - initialTouchX).toInt()
                    params.y = initialWindowY + (event.rawY - initialTouchY).toInt()
                    point.x = (params.x + sizePx / 2).toFloat()
                    point.y = (params.y + sizePx / 2).toFloat()
                    windowManager.updateViewLayout(targetContainer, params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(targetContainer, params)
        targetViews.add(targetContainer)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupControlWidget() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val paddingHorizontal = (12 * density).toInt()
        val paddingVertical = (8 * density).toInt()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#E6120E1E"))
                cornerRadius = 24 * density
                setStroke(2, Color.parseColor("#443A63"))
            }
            background = bg
        }

        // Drag handle icon
        val dragHandle = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_sort_by_size)
            setColorFilter(Color.parseColor("#A19DB8"))
            layoutParams = LinearLayout.LayoutParams((28 * density).toInt(), (28 * density).toInt()).apply {
                marginEnd = (8 * density).toInt()
            }
        }

        // Play/Pause button
        val playBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_media_play)
            setColorFilter(Color.parseColor("#25F4EE"))
            layoutParams = LinearLayout.LayoutParams((36 * density).toInt(), (36 * density).toInt()).apply {
                marginEnd = (10 * density).toInt()
            }
            setOnClickListener {
                toggleTapping()
            }
        }
        playPauseButton = playBtn

        // Counter and status display
        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (12 * density).toInt()
            }
        }

        val counterText = TextView(this).apply {
            text = "0 taps"
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        counterTextView = counterText

        val statusText = TextView(this).apply {
            text = "Ready"
            setTextColor(Color.parseColor("#A19DB8"))
            textSize = 10f
        }
        statusTextView = statusText

        textContainer.addView(counterText)
        textContainer.addView(statusText)

        // Close button
        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.parseColor("#FF5C5C"))
            layoutParams = LinearLayout.LayoutParams((26 * density).toInt(), (26 * density).toInt())
            setOnClickListener {
                stopSelf()
            }
        }

        panel.addView(dragHandle)
        panel.addView(playBtn)
        panel.addView(textContainer)
        panel.addView(closeBtn)

        // Drag logic for the control panel
        var initialTouchX = 0f
        var initialTouchY = 0f
        var initialWindowX = 0
        var initialWindowY = 0

        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialWindowX = params.x
                    initialWindowY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialWindowX + (event.rawX - initialTouchX).toInt()
                    params.y = initialWindowY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(panel, params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(panel, params)
        controlView = panel
    }

    private fun toggleTapping() {
        if (isTapping) {
            stopTapping()
        } else {
            startTapping()
        }
    }

    private fun startTapping() {
        if (isTapping) return
        isTapping = true
        startTimeMs = System.currentTimeMillis()
        _isTappingActive.value = true

        playPauseButton?.setImageResource(android.R.drawable.ic_media_pause)
        playPauseButton?.setColorFilter(Color.parseColor("#FE2C55"))
        statusTextView?.text = "Tapping..."
        statusTextView?.setTextColor(Color.parseColor("#25F4EE"))

        tapJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive && isTapping) {
                val accessibilityService = TapTapAccessibilityService.instance
                if (accessibilityService != null && targetPoints.isNotEmpty()) {
                    for (point in targetPoints) {
                        val finalX = if (currentConfig.safeMode) {
                            point.x + Random.nextInt(-currentConfig.jitterPx, currentConfig.jitterPx + 1)
                        } else {
                            point.x
                        }
                        val finalY = if (currentConfig.safeMode) {
                            point.y + Random.nextInt(-currentConfig.jitterPx, currentConfig.jitterPx + 1)
                        } else {
                            point.y
                        }

                        accessibilityService.performTap(finalX, finalY, currentConfig.tapDurationMs)
                        totalTaps++

                        if (currentConfig.hapticEnabled && totalTaps % 5 == 0L) {
                            triggerHapticFeedback()
                        }
                    }

                    // Update UI counters on main thread periodically
                    if (totalTaps % 2 == 0L) {
                        Handler(Looper.getMainLooper()).post {
                            counterTextView?.text = "$totalTaps taps"
                            _tapCountState.value = totalTaps
                        }
                    }
                }

                // Check stop conditions
                if (currentConfig.stopAfterCount > 0 && totalTaps >= currentConfig.stopAfterCount) {
                    Handler(Looper.getMainLooper()).post { stopTapping() }
                    break
                }

                if (currentConfig.stopAfterMinutes > 0) {
                    val elapsedMinutes = (System.currentTimeMillis() - startTimeMs) / 60000L
                    if (elapsedMinutes >= currentConfig.stopAfterMinutes) {
                        Handler(Looper.getMainLooper()).post { stopTapping() }
                        break
                    }
                }

                val delayBase = currentConfig.tapIntervalMs
                val extraDelay = if (currentConfig.safeMode && currentConfig.randomDelayMs > 0) {
                    Random.nextInt(-currentConfig.randomDelayMs, currentConfig.randomDelayMs + 1)
                } else {
                    0
                }
                val finalDelay = (delayBase + extraDelay).coerceAtLeast(8L)
                delay(finalDelay)
            }
        }
    }

    private fun stopTapping() {
        isTapping = false
        _isTappingActive.value = false
        tapJob?.cancel()
        tapJob = null

        playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
        playPauseButton?.setColorFilter(Color.parseColor("#25F4EE"))
        statusTextView?.text = "Paused"
        statusTextView?.setTextColor(Color.parseColor("#A19DB8"))
    }

    @Suppress("DEPRECATION")
    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(4L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(4L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(4L)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTapping()
        serviceScope.cancel()

        controlView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        for (target in targetViews) {
            try { windowManager.removeView(target) } catch (_: Exception) {}
        }
        targetViews.clear()
        targetPoints.clear()

        _isOverlayRunning.value = false
        _isTappingActive.value = false
    }

    companion object {
        const val ACTION_START = "com.example.taptap.ACTION_START"
        const val ACTION_STOP = "com.example.taptap.ACTION_STOP"
        const val ACTION_TOGGLE_TAP = "com.example.taptap.ACTION_TOGGLE_TAP"

        private val _isOverlayRunning = MutableStateFlow(false)
        val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

        private val _isTappingActive = MutableStateFlow(false)
        val isTappingActive: StateFlow<Boolean> = _isTappingActive.asStateFlow()

        private val _tapCountState = MutableStateFlow(0L)
        val tapCountState: StateFlow<Long> = _tapCountState.asStateFlow()
    }
}
