package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.GestureMapping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MediaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "MediaGestureService"
        var isServiceRunning = false
            private set
        private var activeInstance: MediaAccessibilityService? = null

        fun triggerDirectAction(action: String) {
            activeInstance?.triggerAction(action)
        }
    }

    private lateinit var repository: AppRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentMapping = GestureMapping()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var clickCount = 0
    private var lastKeyDownTime: Long = 0
    private var isLongPressDetected = false

    private var longPressRunnable: Runnable? = null
    private var doubleClickRunnable: Runnable? = null
    private var autoScrollRunnable: Runnable? = null

    // Floating Bubble / Vabol view
    private var windowManager: WindowManager? = null
    private var floatingBubbleView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        activeInstance = this
        val db = AppDatabase.getDatabase(this)
        repository = AppRepository(db)
        
        serviceScope.launch {
            repository.activeMapping.collectLatest { mapping ->
                val oldAutoScroll = currentMapping.isAutoScrollEnabled
                val oldInterval = currentMapping.autoScrollIntervalSeconds
                val oldBubble = currentMapping.isFloatingBubbleEnabled

                currentMapping = mapping
                Log.d(TAG, "Mapping updated: $mapping")

                // Update Auto-scroll timer
                if (oldAutoScroll != mapping.isAutoScrollEnabled || oldInterval != mapping.autoScrollIntervalSeconds) {
                    setupAutoScrollTimer()
                }

                // Update Floating Bubble ("Vabol")
                if (oldBubble != mapping.isFloatingBubbleEnabled) {
                    if (mapping.isFloatingBubbleEnabled) {
                        showFloatingBubble()
                    } else {
                        removeFloatingBubble()
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        activeInstance = this
        Log.d(TAG, "Accessibility Service Connected")
        setupAutoScrollTimer()
        if (currentMapping.isFloatingBubbleEnabled) {
            showFloatingBubble()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handled by key event filter and gestures
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service Interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        activeInstance = null
        removeFloatingBubble()
        stopAutoScrollTimer()
        Log.d(TAG, "Service Unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        activeInstance = null
        removeFloatingBubble()
        stopAutoScrollTimer()
        serviceScope.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Service Destroyed")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!currentMapping.isGestureServiceEnabled) {
            return false
        }

        val keyCode = event.keyCode
        val action = event.action

        val isHeadsetKey = keyCode == KeyEvent.KEYCODE_HEADSETHOOK || 
                           keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || 
                           keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || 
                           keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || 
                           keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || 
                           keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS

        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP || 
                          keyCode == KeyEvent.KEYCODE_VOLUME_DOWN

        if (isHeadsetKey) {
            handleHeadsetKeyEvent(event)
            return true
        } else if (isVolumeKey) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    dispatchSwipeDown()
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    dispatchSwipeUp()
                }
                triggerHapticFeedback()
            }
            return true
        }

        return super.onKeyEvent(event)
    }

    private fun handleHeadsetKeyEvent(event: KeyEvent) {
        val action = event.action
        val currentTime = System.currentTimeMillis()

        if (action == KeyEvent.ACTION_DOWN) {
            if (event.repeatCount == 0) {
                lastKeyDownTime = currentTime
                isLongPressDetected = false

                longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                val checkLongPress = Runnable {
                    isLongPressDetected = true
                    triggerHapticFeedback(strong = true)
                    triggerAction(currentMapping.longPressAction)
                }
                longPressRunnable = checkLongPress
                mainHandler.postDelayed(checkLongPress, 550)
            }
        } else if (action == KeyEvent.ACTION_UP) {
            longPressRunnable?.let { mainHandler.removeCallbacks(it) }

            if (!isLongPressDetected) {
                clickCount++
                doubleClickRunnable?.let { mainHandler.removeCallbacks(it) }
                
                if (clickCount == 1) {
                    val singleClickCheck = Runnable {
                        if (clickCount == 1) {
                            triggerHapticFeedback()
                            triggerAction(currentMapping.singlePressAction)
                        }
                        clickCount = 0
                    }
                    doubleClickRunnable = singleClickCheck
                    mainHandler.postDelayed(singleClickCheck, 280)
                } else if (clickCount >= 2) {
                    triggerHapticFeedback(strong = true)
                    triggerAction(currentMapping.doublePressAction)
                    clickCount = 0
                }
            }
        }
    }

    fun triggerAction(actionName: String) {
        Log.d(TAG, "Triggering Action: $actionName")
        when (actionName) {
            "SCROLL_DOWN" -> dispatchSwipeDown()
            "SCROLL_UP" -> dispatchSwipeUp()
            "LIKE" -> dispatchDoubleTap()
            "PLAY_PAUSE" -> dispatchSingleTap()
            else -> { /* No-op */ }
        }
    }

    // AUTO SCROLL TIMER LOOP
    private fun setupAutoScrollTimer() {
        stopAutoScrollTimer()
        if (currentMapping.isAutoScrollEnabled) {
            val intervalMs = (currentMapping.autoScrollIntervalSeconds.coerceAtLeast(5)) * 1000L
            val runnable = object : Runnable {
                override fun run() {
                    if (currentMapping.isAutoScrollEnabled) {
                        dispatchSwipeDown()
                        mainHandler.postDelayed(this, intervalMs)
                    }
                }
            }
            autoScrollRunnable = runnable
            mainHandler.postDelayed(runnable, intervalMs)
            Log.d(TAG, "Auto-scroll started with interval: ${intervalMs}ms")
        }
    }

    private fun stopAutoScrollTimer() {
        autoScrollRunnable?.let {
            mainHandler.removeCallbacks(it)
            autoScrollRunnable = null
        }
    }

    // FLOATING BUBBLE ("VABOL") OVERLAY
    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingBubble() {
        if (floatingBubbleView != null) return
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val sizeInPx = (56 * resources.displayMetrics.density).toInt()
            val params = WindowManager.LayoutParams(
                sizeInPx,
                sizeInPx,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = resources.displayMetrics.widthPixels - sizeInPx - 20
                y = resources.displayMetrics.heightPixels / 2
            }
            bubbleParams = params

            val bubble = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_media_play)
                setBackgroundResource(android.R.drawable.btn_default)
                setPadding(16, 16, 16, 16)
                alpha = 0.85f
            }

            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            var isDragging = false
            var bubbleClickCount = 0
            var lastClickTime: Long = 0

            bubble.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager?.updateViewLayout(bubble, params)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 300) {
                                bubbleClickCount++
                            } else {
                                bubbleClickCount = 1
                            }
                            lastClickTime = now

                            if (bubbleClickCount >= 2) {
                                // Double click on bubble -> LIKE
                                triggerHapticFeedback(strong = true)
                                dispatchDoubleTap()
                                bubbleClickCount = 0
                            } else {
                                // Single tap on bubble -> SCROLL NEXT
                                mainHandler.postDelayed({
                                    if (bubbleClickCount == 1) {
                                        triggerHapticFeedback()
                                        dispatchSwipeDown()
                                        bubbleClickCount = 0
                                    }
                                }, 280)
                            }
                        }
                        true
                    }
                    else -> false
                }
            }

            windowManager?.addView(bubble, params)
            floatingBubbleView = bubble
            Log.d(TAG, "Floating Bubble ('Vabol') initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Could not add floating bubble: ${e.message}")
        }
    }

    private fun removeFloatingBubble() {
        try {
            floatingBubbleView?.let {
                windowManager?.removeView(it)
                floatingBubbleView = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing floating bubble: ${e.message}")
        }
    }

    private fun triggerHapticFeedback(strong: Boolean = false) {
        if (!currentMapping.isHapticFeedbackEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (strong) {
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    } else {
                        VibrationEffect.createOneShot(25, 120)
                    }
                    it.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(if (strong) 50 else 25)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptics not triggered: ${e.message}")
        }
    }

    // DISPATCH GESTURES WITH CUSTOM SPEED
    private fun getSwipeDuration(): Long {
        return when (currentMapping.swipeSpeed) {
            "FAST" -> 160L
            "SMOOTH" -> 380L
            else -> 250L // NORMAL
        }
    }

    private fun dispatchSwipeDown() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val startX = width / 2f
        val startY = height * 0.75f
        val endX = width / 2f
        val endY = height * 0.20f

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, getSwipeDuration()))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun dispatchSwipeUp() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val startX = width / 2f
        val startY = height * 0.20f
        val endX = width / 2f
        val endY = height * 0.75f

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, getSwipeDuration()))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun dispatchSingleTap() {
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2f
        val centerY = displayMetrics.heightPixels / 2f

        val path = Path().apply {
            moveTo(centerX, centerY)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun dispatchDoubleTap() {
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2f
        val centerY = displayMetrics.heightPixels / 2f

        val path1 = Path().apply { moveTo(centerX, centerY) }
        val path2 = Path().apply { moveTo(centerX, centerY) }

        val stroke1 = GestureDescription.StrokeDescription(path1, 0, 45)
        val stroke2 = GestureDescription.StrokeDescription(path2, 120, 45)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(stroke1)
        gestureBuilder.addStroke(stroke2)
        dispatchGesture(gestureBuilder.build(), null, null)
    }
}
