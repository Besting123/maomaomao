package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.myapplication.R
import com.example.myapplication.ui.components.CatModel3DViewer
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════
// CompanionScreen — 游戏化陪伴主界面
// ═══════════════════════════════════════════

@Composable
fun CompanionScreen(viewModel: MainViewModel? = null) {
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var catFeedback by remember { mutableStateOf("小黑正在轻轻呼吸，适合远距离陪伴。") }
    var catAnimationIndex by remember { mutableStateOf(0) }
    var showDataPanel by remember { mutableStateOf(false) }
    // 粒子特效
    var particles by remember { mutableStateOf(listOf<Particle>()) }
    val coroutineScope = rememberCoroutineScope()

    fun handleAction(actionName: String) {
        viewModel?.interactWithCat(actionName, "小黑", 5)
        catFeedback = when (actionName) {
            "安抚" -> "小黑眯起眼睛，尾巴放松地摆了一下。"
            "观察" -> "你保持了安全距离，小黑没有出现应激反应。"
            "补水" -> "补水提醒已点亮，优先关注泌尿健康。"
            "添粮" -> "已记录一次轻量添粮，注意不要过度投喂。"
            else -> "已完成一次互动。"
        }
        catAnimationIndex = when (actionName) {
            "安抚" -> 1; "添粮", "补水" -> 2; else -> 0
        }
        // 生成粒子
        val emoji = when (actionName) {
            "安抚" -> "❤️"; "补水" -> "💧"; "添粮" -> "🐟"; else -> "⭐"
        }
        val newParticles = List(6) { Particle(emoji = emoji) }
        particles = particles + newParticles
        coroutineScope.launch {
            delay(2000L)
            particles = particles.filterNot { it in newParticles }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Layer 0: 场景背景 ──
        GameSceneBackground()

        // ── Layer 1: 3D 猫咪模型 ──
        CatModel3DViewer(
            modifier = Modifier.fillMaxSize(),
            modelAssetPath = "models/cat.glb",
            label = "",
            animationIndex = catAnimationIndex,
            isFullScreen = true,
            onDoubleTap = { handleAction("安抚") }
        )

        // ── Layer 2: 粒子特效 ──
        FloatingParticles(particles = particles)

        // ── Layer 3: 对话气泡 ──
        SpeechBubble(
            text = catFeedback,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-100).dp)
        )

        // ── Layer 4: 顶部游戏 HUD ──
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 左：宠物名 + 等级
                PetInfoHeader(
                    petName = "小黑",
                    level = uiState?.petLevel ?: 5,
                    exp = uiState?.petExp ?: 320,
                    expToNext = uiState?.petExpToNext ?: 500
                )
                // 右：代币
                TokenDisplay(tokens = uiState?.tokenBalance ?: 350)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 状态条
            PetStatusBars(
                hunger = uiState?.hungerValue ?: 0.7f,
                happiness = uiState?.happinessValue ?: 0.85f,
                health = uiState?.healthValue ?: 0.92f
            )
        }

        // ── Layer 5: 底部道具栏 ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 数据中心按钮
            IconButton(
                onClick = { showDataPanel = true },
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Outlined.Analytics, "数据中心", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
            GameActionBar(onAction = { handleAction(it) })
        }

        // ── Layer 6: 数据面板 ──
        AnimatedVisibility(
            visible = showDataPanel,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DataPanelOverlay(
                onClose = { showDataPanel = false },
                uiState = uiState,
                offlineClaimed = false,
                onClaimClick = {}
            )
        }
    }
}

// ═══════════════════════════════════════════
// 场景背景 — Canvas 绘制温馨室内
// ═══════════════════════════════════════════

@Composable
fun GameSceneBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val floorY = h * 0.65f

        // 墙壁：深紫到暖棕渐变
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2A1F3D), Color(0xFF3D2B4A), Color(0xFF4A3528)),
                startY = 0f, endY = floorY
            ),
            size = androidx.compose.ui.geometry.Size(w, floorY)
        )

        // 墙脚线
        drawRect(
            color = Color(0xFF5C3D2E),
            topLeft = Offset(0f, floorY - 4f),
            size = androidx.compose.ui.geometry.Size(w, 8f)
        )

        // 地板：暖棕到琥珀渐变
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF5C3D2E), Color(0xFF7A5230), Color(0xFF8B6914)),
                startY = floorY, endY = h
            ),
            topLeft = Offset(0f, floorY),
            size = androidx.compose.ui.geometry.Size(w, h - floorY)
        )

        // 柔光：墙壁上的暖光晕
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x30FFA726), Color.Transparent),
                center = Offset(w * 0.5f, floorY * 0.4f),
                radius = w * 0.5f
            ),
            center = Offset(w * 0.5f, floorY * 0.4f),
            radius = w * 0.5f
        )
    }
}

// ═══════════════════════════════════════════
// 宠物信息头 — 名字 + 等级 + 经验条
// ═══════════════════════════════════════════

@Composable
fun PetInfoHeader(petName: String, level: Int, exp: Int, expToNext: Int) {
    val animatedExp by animateFloatAsState(
        targetValue = exp.toFloat() / expToNext,
        animationSpec = tween(800), label = "exp"
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🐱", fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                petName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(shadow = Shadow(Color.Black.copy(0.6f), blurRadius = 4f))
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFB74D), Color(0xFFFFA726))))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Lv.$level", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
        Spacer(Modifier.height(4.dp))
        // 经验条
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedExp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFB74D), Color(0xFFFF9800))))
            )
        }
        Text("$exp / $expToNext EXP", fontSize = 9.sp, color = Color.White.copy(0.5f), modifier = Modifier.padding(top = 2.dp))
    }
}

// ═══════════════════════════════════════════
// 代币显示
// ═══════════════════════════════════════════

@Composable
fun TokenDisplay(tokens: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🐾", fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text("$tokens", color = Color(0xFFFFD54F), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ═══════════════════════════════════════════
// 状态条 — 饱食 / 心情 / 健康
// ═══════════════════════════════════════════

@Composable
fun PetStatusBars(hunger: Float, happiness: Float, health: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatusBarRow("🍖", "饱食", hunger, Color(0xFFFFA726))
        StatusBarRow("💗", "心情", happiness, Color(0xFFF06292))
        StatusBarRow("🌿", "健康", health, Color(0xFF66BB6A))
    }
}

@Composable
fun StatusBarRow(icon: String, label: String, value: Float, color: Color) {
    val animatedValue by animateFloatAsState(
        targetValue = value, animationSpec = tween(600), label = label
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(28.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedValue)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color)))
            )
        }
        Spacer(Modifier.width(6.dp))
        Text("${(animatedValue * 100).toInt()}%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
    }
}

// ═══════════════════════════════════════════
// 对话气泡
// ═══════════════════════════════════════════

@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        visible = true
        delay(4000L)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.5f) + fadeIn(),
        exit = fadeOut(tween(800)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, fontSize = 13.sp, color = Color(0xFF383833), fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════
// 粒子特效
// ═══════════════════════════════════════════

data class Particle(
    val emoji: String,
    val startX: Float = Random.nextFloat(),
    val startY: Float = 0.55f + Random.nextFloat() * 0.1f,
    val driftX: Float = (Random.nextFloat() - 0.5f) * 0.15f,
    val speed: Float = 0.3f + Random.nextFloat() * 0.4f
)

@Composable
fun FloatingParticles(particles: List<Particle>) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "p"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val age = (progress * p.speed * 3f) % 1f
            val x = (p.startX + p.driftX * age) * size.width
            val y = (p.startY - age * 0.4f) * size.height
            val alpha = (1f - age).coerceIn(0f, 1f)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = 48f
                    this.alpha = (alpha * 255).toInt()
                }
                drawText(p.emoji, x, y, paint)
            }
        }
    }
}

// ═══════════════════════════════════════════
// 底部游戏道具栏
// ═══════════════════════════════════════════

@Composable
fun GameActionBar(onAction: (String) -> Unit) {
    var lastActionAt by remember { mutableStateOf(0L) }
    fun submitAction(name: String) {
        val now = System.currentTimeMillis()
        if (now - lastActionAt >= 450L) { lastActionAt = now; onAction(name) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        GameItemButton("🤚", "安抚", Color(0xFFF06292)) { submitAction("安抚") }
        GameItemButton("👀", "观察", Color(0xFF64B5F6)) { submitAction("观察") }
        GameItemButton("💧", "补水", Color(0xFF4DD0E1)) { submitAction("补水") }
        GameItemButton("🐟", "添粮", Color(0xFFFFB74D)) { submitAction("添粮") }
    }
}

@Composable
fun GameItemButton(emoji: String, label: String, glowColor: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 800f), label = "btn"
    )
    LaunchedEffect(pressed) { if (pressed) { delay(150); pressed = false } }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .size(56.dp)
                .shadow(8.dp, CircleShape, ambientColor = glowColor.copy(0.4f), spotColor = glowColor.copy(0.3f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.25f), Color.White.copy(alpha = 0.08f))
                    )
                )
                .border(1.dp, glowColor.copy(alpha = 0.4f), CircleShape)
                .clickable { pressed = true; onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            style = androidx.compose.ui.text.TextStyle(shadow = Shadow(Color.Black.copy(0.8f), blurRadius = 6f))
        )
    }
}

// ═══════════════════════════════════════════
// 数据面板（保留原有逻辑）
// ═══════════════════════════════════════════

@Composable
fun DataPanelOverlay(onClose: () -> Unit, uiState: com.example.myapplication.ui.viewmodel.MainAppState?, offlineClaimed: Boolean, onClaimClick: () -> Unit) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(onClick = onClose)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable { }
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.LightGray))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("陪伴数据中心", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 24.dp)
            ) {
                StatsGridSection()
                Spacer(Modifier.height(24.dp))
                TipCalloutSection()
                Spacer(Modifier.height(24.dp))
                CompanionRecordSection(records = uiState?.companionRecords ?: emptyList())
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun StatsGridSection() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Favorite, "Fav", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("善意值", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
                Spacer(Modifier.height(8.dp))
                Text("1,280", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("已累计救助 12 次", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        }
        Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Info, "Water", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Text("补水包", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("08", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                    Text(" 份", fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary.copy(0.5f), modifier = Modifier.padding(bottom = 2.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text("预计消耗 2 份/日", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        }
    }
}

@Composable
fun TipCalloutSection() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.tertiaryContainer.copy(0.3f)).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.Info, "Idea", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("科学饮食建议", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Text("当前环境湿度较低，建议补水优先。近期小黑已有3次零食记录，为了预防肥胖，不建议高频喂食零食。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.8f), lineHeight = 18.sp)
        }
    }
}

@Composable
fun CompanionRecordSection(records: List<com.example.myapplication.ui.viewmodel.CompanionRecord>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.AutoMirrored.Outlined.List, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Text("今日陪伴记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (records.isEmpty()) {
                    Text("今日暂无互动记录", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    records.take(5).forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val c = when (r.colorType) { 1 -> MaterialTheme.colorScheme.primary; 2 -> MaterialTheme.colorScheme.secondary; else -> MaterialTheme.colorScheme.tertiary }
                            Box(Modifier.size(8.dp).background(c, CircleShape))
                            Text("${r.time.substringAfter(" ")} ${r.description}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
