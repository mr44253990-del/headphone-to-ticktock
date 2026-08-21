package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.GestureMapping
import com.example.data.SoundPreset
import com.example.ui.MainViewModel
import com.example.ui.theme.AccentPink
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val db = remember { AppDatabase.getDatabase(context) }
                val repository = remember { AppRepository(db) }
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(application, repository)
                )

                var isAccessibilityEnabled by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    while (true) {
                        isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
                        delay(2000)
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    topBar = {
                        AppHeader(isAccessibilityEnabled)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        DarkBackground,
                                        DeepPurple.copy(alpha = 0.5f),
                                        DarkBackground
                                    )
                                )
                            )
                    ) {
                        // Ambient glowing background orbs for glassmorphism
                        AmbientGlassBackground()

                        MainScreen(
                            viewModel = mainViewModel,
                            isAccessibilityEnabled = isAccessibilityEnabled,
                            onCheckService = { isAccessibilityEnabled = isAccessibilityServiceEnabled(context) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// Background dynamic gradient orbs
@Composable
fun AmbientGlassBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_orbs")
    val orbOffset1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orb1"
    )
    val orbOffset2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orb2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Glowing cyan orb top right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.85f + orbOffset1, size.height * 0.15f),
                radius = 280.dp.toPx()
            )
        )
        // Glowing pink/purple orb bottom left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AccentPink.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.15f + orbOffset2, size.height * 0.70f),
                radius = 320.dp.toPx()
            )
        )
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    if (com.example.service.MediaAccessibilityService.isServiceRunning) {
        return true
    }
    val expectedService = "${context.packageName}/${com.example.service.MediaAccessibilityService::class.java.canonicalName}"
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        if (componentNameString.equals(expectedService, ignoreCase = true)) {
            return true
        }
    }
    return false
}

// TOP APP BAR
@Composable
fun AppHeader(isAccessibilityEnabled: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.8f), AccentPink.copy(alpha = 0.8f))
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .background(DarkBackground.copy(alpha = 0.90f))
            .padding(top = 44.dp, bottom = 14.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = "App Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SmartScroll",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "DJ Bass & Headphone Gesture",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isAccessibilityEnabled) NeonGreen.copy(alpha = 0.15f)
                        else AccentPink.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isAccessibilityEnabled) NeonGreen else AccentPink,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isAccessibilityEnabled) "● সচল (Active)" else "○ নিষ্ক্রিয় (Inactive)",
                    color = if (isAccessibilityEnabled) NeonGreen else AccentPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// GLASSMORPHISM CONTAINER COMPOSABLE
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    borderGlowColor: Color = NeonCyan.copy(alpha = 0.35f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderGlowColor,
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    isAccessibilityEnabled: Boolean,
    onCheckService: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("হেডফোন বাটন", "ভাসমান বল (Vabol)", "ডিজে সাউন্ড")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan,
                    height = 3.dp
                )
            },
            divider = {
                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("tab_$index"),
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> GestureTabContent(viewModel, isAccessibilityEnabled, onCheckService)
                1 -> FloatingBubbleAndAutoScrollTab(viewModel, isAccessibilityEnabled)
                2 -> SoundTabContent(viewModel)
            }
        }
    }
}

// TAB 1: GESTURES & BUTTON CONTROLS
@Composable
fun GestureTabContent(
    viewModel: MainViewModel,
    isAccessibilityEnabled: Boolean,
    onCheckService: () -> Unit
) {
    val context = LocalContext.current
    val mapping by viewModel.activeMapping.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // SERVICE STATUS GLASS CARD
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("status_card"),
            borderGlowColor = if (isAccessibilityEnabled) NeonGreen.copy(alpha = 0.5f) else AccentPink.copy(alpha = 0.5f)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Status",
                        tint = if (isAccessibilityEnabled) NeonGreen else AccentPink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isAccessibilityEnabled) "অ্যাক্সেসিবিলিটি সার্ভিস সক্রিয়" else "সার্ভিস বন্ধ আছে! চালু করুন",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isAccessibilityEnabled) {
                        "হেডফোন বাটন দিয়ে টিকটক, ফেসবুক রিলস এবং শর্টস স্ক্রোল করার জন্য সিস্টেম সম্পূর্ণ রেডি।"
                    } else {
                        "হেডফোনের বাটন দিয়ে স্বয়ংক্রিয়ভাবে ভিডিও স্ক্রোল করতে সার্ভিসটি অন করুন।"
                    },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!isAccessibilityEnabled) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                                Toast.makeText(context, "তালিকায় 'SmartScroll' খুঁজে বের করে অন (ON) করুন।", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "সেটিংস খুলতে ব্যর্থ হয়েছে।", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_enable_service"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("সেটিংস থেকে সার্ভিস অন করুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onCheckService,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("স্ট্যাটাস রিফ্রেশ", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // TOGGLE SWITCH GESTURES
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            borderGlowColor = NeonCyan.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "হেডফোন বাটন কনট্রোলার",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "বাটন ক্লিক দিয়ে ভিডিও স্ক্রোল এবং লাইক অন/অফ করুন",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = mapping.isGestureServiceEnabled,
                    onCheckedChange = { viewModel.toggleGestureService(it) },
                    modifier = Modifier.testTag("switch_gesture_service"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // BUTTON ACTIONS CUSTOMIZATION
        Text(
            text = "বাটন অ্যাকশন কাস্টমাইজেশন (Custom Triggers)",
            color = NeonCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ActionSelectionRow(
            label = "এক ক্লিকে (Single Click)",
            selectedAction = mapping.singlePressAction,
            onActionSelected = { viewModel.updateSinglePressAction(it) }
        )

        ActionSelectionRow(
            label = "ডবল ক্লিকে (Double Click)",
            selectedAction = mapping.doublePressAction,
            onActionSelected = { viewModel.updateDoublePressAction(it) }
        )

        ActionSelectionRow(
            label = "লং প্রেসে (Long Press Hold)",
            selectedAction = mapping.longPressAction,
            onActionSelected = { viewModel.updateLongPressAction(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SWIPE SPEED & HAPTIC FEEDBACK SETTINGS
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            borderGlowColor = NeonGreen.copy(alpha = 0.3f)
        ) {
            Column {
                Text(
                    text = "স্ক্রোলিং গতি ও ভাইব্রেশন ফিডব্যাক",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = "Haptic", tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ক্লিকে ভাইব্রেশন (Haptic Feedback)", color = Color.White, fontSize = 13.sp)
                    }
                    Switch(
                        checked = mapping.isHapticFeedbackEnabled,
                        onCheckedChange = { viewModel.toggleHapticFeedback(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("স্ক্রোল স্পিড (Swipe Speed):", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val speeds = listOf("FAST" to "দ্রুত (Fast)", "NORMAL" to "স্বাভাবিক (Normal)", "SMOOTH" to "মসৃণ (Smooth)")
                    speeds.forEach { (speedKey, speedLabel) ->
                        val isSelected = mapping.swipeSpeed == speedKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) NeonGreen else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateSwipeSpeed(speedKey) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = speedLabel.split(" ")[0],
                                color = if (isSelected) NeonGreen else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // SMART VOLUME BUTTONS
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            borderGlowColor = AccentPink.copy(alpha = 0.3f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume Scroller",
                    tint = AccentPink,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "স্মার্ট ভলিউম স্ক্রোলারও সক্রিয়!",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ফোনের ভলিউম ডাউন (-) চাপলে পরের ভিডিও এবং ভলিউম আপ (+) চাপলে আগের ভিডিওতে চলে যাবে।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// TAB 2: FLOATING BUBBLE ("VABOL") & AUTO-SCROLL TIMER
@Composable
fun FloatingBubbleAndAutoScrollTab(
    viewModel: MainViewModel,
    isAccessibilityEnabled: Boolean
) {
    val mapping by viewModel.activeMapping.collectAsState()
    val scrollState = rememberScrollState()

    // Interactive Demo State
    var demoHeartsVisible by remember { mutableStateOf(false) }
    var demoSwipeDirection by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // FLOATING BUBBLE ("VABOL") MASTER CARD
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            borderGlowColor = NeonCyan.copy(alpha = 0.4f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Floating Bubble",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "অন-স্ক্রিন ভাসমান বল (Vabol Bubble)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "স্ক্রিনের পাশে ভাসমান অ্যাসিস্ট্যান্ট বাটন",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = mapping.isFloatingBubbleEnabled,
                        onCheckedChange = { viewModel.toggleFloatingBubble(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "টিকটক বা ফেসবুক রিলস দেখার সময় স্ক্রিনের পাশে একটি ছোট ড্র্যাগেবল বাটন থাকবে। বাটনটিতে ১টি ট্যাপ করলে পরের ভিডিওতে যাবে, ২টি ট্যাপ করলে লাইক হবে!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // INTERACTIVE IN-APP TEST SANDBOX
        Text(
            text = "ইন্টারেক্টিভ টেস্ট বক্স (Test Your Bubble)",
            color = NeonCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            borderGlowColor = AccentPink.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "নিচের ভাসমান বলটিতে ট্যাপ বা ডাবল ট্যাপ করে পরীক্ষা করুন:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Bubble
                var bubbleScale by remember { mutableFloatStateOf(1f) }
                val animatedScale by animateFloatAsState(targetValue = bubbleScale, label = "b_scale")

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonCyan, DeepPurple, Color.Black)
                            )
                        )
                        .border(2.dp, NeonCyan, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            bubbleScale = 1.25f
                            demoSwipeDirection = "⬇ পরবর্তী ভিডিও স্ক্রোল ট্রিপ!"
                            viewModel.testDirectGesture("SCROLL_DOWN")
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Tap",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                LaunchedEffect(bubbleScale) {
                    if (bubbleScale > 1f) {
                        delay(200)
                        bubbleScale = 1f
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            demoHeartsVisible = true
                            viewModel.testDirectGesture("LIKE")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPink),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = AccentPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ডাবল ট্যাপ লাইক টেস্ট", color = AccentPink, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            demoSwipeDirection = "⬇ পরের ভিডিও স্ক্রোল হচ্ছে"
                            viewModel.testDirectGesture("SCROLL_DOWN")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = "Scroll", tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("স্ক্রোল টেস্ট", color = NeonGreen, fontSize = 11.sp)
                    }
                }

                if (demoSwipeDirection.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = demoSwipeDirection, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                LaunchedEffect(demoHeartsVisible) {
                    if (demoHeartsVisible) {
                        delay(1200)
                        demoHeartsVisible = false
                    }
                }
            }
        }

        // HANDS-FREE AUTO-SCROLL TIMER
        Text(
            text = "অটো-স্ক্রোল টাইমার (Hands-free Auto Scroller)",
            color = NeonGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            borderGlowColor = NeonGreen.copy(alpha = 0.4f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AvTimer,
                            contentDescription = "Auto Scroll Timer",
                            tint = NeonGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "অটোমেটিক স্ক্রোলিং সক্রিয়",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "নির্দিষ্ট সময় পরপর হাত ছাড়াই ভিডিও চেঞ্জ হবে",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = mapping.isAutoScrollEnabled,
                        onCheckedChange = { viewModel.toggleAutoScroll(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ভিডিও পরিবর্তনের সময় বিরতি সিলেক্ট করুন:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intervals = listOf(10, 15, 30, 45, 60)
                    intervals.forEach { sec ->
                        val isSelected = mapping.autoScrollIntervalSeconds == sec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) NeonGreen else Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateAutoScrollInterval(sec) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sec}s",
                                color = if (isSelected) NeonGreen else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionSelectionRow(
    label: String,
    selectedAction: String,
    onActionSelected: (String) -> Unit
) {
    val actionsMap = mapOf(
        "SCROLL_DOWN" to "পরবর্তী ভিডিও (Scroll Next)",
        "SCROLL_UP" to "আগের ভিডিও (Scroll Previous)",
        "LIKE" to "লাইক করা (Double Tap)",
        "PLAY_PAUSE" to "প্লে/পজ (Play & Pause)",
        "NONE" to "কোনোটিই নয় (No Action)"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            actionsMap.forEach { (actionKey, actionLabel) ->
                val isSelected = selectedAction == actionKey
                val borderCol by animateColorAsState(if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f), label = "border")
                val bgCol by animateColorAsState(if (isSelected) NeonCyan.copy(alpha = 0.18f) else CardSurface.copy(alpha = 0.6f), label = "bg")
                val textCol by animateColorAsState(if (isSelected) NeonCyan else TextSecondary, label = "text")

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgCol)
                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                        .clickable { onActionSelected(actionKey) }
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel.split(" ")[0],
                        color = textCol,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// TAB 3: SOUND EFFECTS, BASS, AMPLIFIER & EQUALIZER
@Composable
fun SoundTabContent(viewModel: MainViewModel) {
    val preset by viewModel.activePreset.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // MASTER TOGGLE
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            borderGlowColor = NeonGreen.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Sound FX",
                        tint = NeonGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ডিজে সাউন্ড সেটিংস সক্রিয়",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বাস বুস্টার এবং কাস্টম সাউন্ড মোড",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = { viewModel.toggleSoundEnabled(it) },
                    modifier = Modifier.testTag("switch_sound_effects"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonGreen,
                        checkedTrackColor = NeonGreen.copy(alpha = 0.4f)
                    )
                )
            }
        }

        AnimatedVisibility(visible = isSoundEnabled) {
            Column {
                Text(
                    text = "সাউন্ড প্রি-সেট মোড সিলেক্ট করুন (Sound Modes)",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple("MUSIC", "মিউজিক মোড", Icons.Default.MusicNote),
                        Triple("GAMING", "গেমিং মোড", Icons.Default.SportsEsports),
                        Triple("DJ", "ডিজে বাস মোড", Icons.Default.Audiotrack),
                        Triple("CUSTOM", "কাস্টম মোড", Icons.Default.Tune)
                    )

                    modes.forEach { (modeKey, modeName, icon) ->
                        val isSelected = preset.mode == modeKey
                        val borderCol by animateColorAsState(if (isSelected) NeonGreen else Color.White.copy(alpha = 0.05f), label = "mode_border")
                        val bgCol by animateColorAsState(if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f), label = "mode_bg")
                        val textCol by animateColorAsState(if (isSelected) NeonGreen else TextSecondary, label = "mode_text")

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgCol)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setSoundMode(modeKey) }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = modeName,
                                tint = textCol,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = modeName.split(" ")[0],
                                color = textCol,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // GLOWING AUDIO SPECTRUM CANVAS
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    borderGlowColor = NeonGreen.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (preset.mode == "DJ") "DJ BASS MODE - সর্বাধিক বিট সক্রিয়! 🎧" else "অডিও ওয়েভ স্পেকট্রাম সচল আছে",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AudioPulseWaveform(mode = preset.mode)
                    }
                }

                // SLIDERS: BASS BOOST, VIRTUALIZER, LOUDNESS
                Text(
                    text = "সাউন্ড এনহান্সার কন্ট্রোল (Hardware Gain)",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SoundSliderGlassCard(
                    title = "ডিজে বাস বুস্টার (DJ Bass Booster)",
                    value = preset.bassBoost,
                    onValueChange = { viewModel.updateBassBoost(it) },
                    valueRange = 0..1000,
                    activeColor = NeonGreen
                )

                SoundSliderGlassCard(
                    title = "থ্রিডি ভার্চুয়ালাইজার (3D Surround Sound)",
                    value = preset.virtualizer,
                    onValueChange = { viewModel.updateVirtualizer(it) },
                    valueRange = 0..1000,
                    activeColor = NeonCyan
                )

                SoundSliderGlassCard(
                    title = "সাউন্ড এম্প্লিফায়ার (Loudness Enhancer / Gain)",
                    value = preset.loudness,
                    onValueChange = { viewModel.updateLoudness(it) },
                    valueRange = 0..1000,
                    activeColor = AccentPink
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5-BAND EQUALIZER
                Text(
                    text = "৫-ব্যান্ড ইকুয়ালাইজার গ্রাফ (5-Band Graphic EQ)",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    borderGlowColor = NeonCyan.copy(alpha = 0.35f)
                ) {
                    Column {
                        val bands = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")
                        val levels = listOf(preset.eqBand1, preset.eqBand2, preset.eqBand3, preset.eqBand4, preset.eqBand5)

                        bands.forEachIndexed { index, bandName ->
                            val currentVal = levels[index]
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bandName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(50.dp)
                                )

                                Slider(
                                    value = currentVal.toFloat(),
                                    onValueChange = { viewModel.updateEqBand(index, it.toInt()) },
                                    valueRange = -1500f..1500f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("slider_eq_$index"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonGreen,
                                        activeTrackColor = NeonGreen,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )

                                Text(
                                    text = "${if (currentVal >= 0) "+" else ""}${currentVal / 100} dB",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(50.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundSliderGlassCard(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedRange<Int>,
    activeColor: Color
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        borderGlowColor = activeColor.copy(alpha = 0.3f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "${(value * 100) / valueRange.endInclusive}%", color = activeColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
                modifier = Modifier.testTag("slider_${title.split(" ").last()}"),
                colors = SliderDefaults.colors(
                    thumbColor = activeColor,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }
}

// GLOWING EQUALIZER BARS GRAPH
@Composable
fun AudioPulseWaveform(mode: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    
    val multiplier = when (mode) {
        "DJ" -> 1.8f
        "GAMING" -> 1.3f
        "MUSIC" -> 1.0f
        else -> 0.7f
    }

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 60f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(400, delayMillis = 50), repeatMode = RepeatMode.Reverse), label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 15f, targetValue = 85f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(350, delayMillis = 0), repeatMode = RepeatMode.Reverse), label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 95f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(500, delayMillis = 100), repeatMode = RepeatMode.Reverse), label = "w3"
    )
    val wave4 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 70f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(450, delayMillis = 30), repeatMode = RepeatMode.Reverse), label = "w4"
    )
    val wave5 by infiniteTransition.animateFloat(
        initialValue = 5f, targetValue = 50f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(300, delayMillis = 80), repeatMode = RepeatMode.Reverse), label = "w5"
    )
    val wave6 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 80f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(420, delayMillis = 20), repeatMode = RepeatMode.Reverse), label = "w6"
    )
    val wave7 by infiniteTransition.animateFloat(
        initialValue = 15f, targetValue = 100f * multiplier,
        animationSpec = infiniteRepeatable(animation = tween(380, delayMillis = 10), repeatMode = RepeatMode.Reverse), label = "w7"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 16.dp)
    ) {
        val barCount = 15
        val spacing = size.width / (barCount + 2)
        val barWidth = spacing * 0.6f
        val centerY = size.height / 2f

        val waves = listOf(wave1, wave2, wave3, wave4, wave5, wave6, wave7, wave4, wave6, wave3, wave1, wave5, wave2, wave7, wave3)

        for (i in 0 until barCount) {
            val waveHeight = waves[i % waves.size].dp.toPx()
            val startX = (i + 1) * spacing
            val startY = centerY - (waveHeight / 2f)
            val endY = centerY + (waveHeight / 2f)

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(NeonCyan, NeonGreen, AccentPink)
                ),
                start = Offset(startX, startY),
                end = Offset(startX, endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
