package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.CatModel3DViewer
import com.example.myapplication.ui.components.CatViewerMode
import com.example.myapplication.ui.theme.SurfaceContainer
import com.example.myapplication.ui.theme.SurfaceContainerLow
import com.example.myapplication.ui.theme.SurfaceContainerLowest
import com.example.myapplication.ui.theme.SurfaceContainerHighest
import com.example.myapplication.ui.viewmodel.CompanionRecord
import com.example.myapplication.ui.viewmodel.MainAppState
import com.example.myapplication.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

data class CompanionActionUi(
    val emoji: String,
    val label: String,
    val subtitle: String,
    val color: Color
)

data class Particle(
    val emoji: String,
    val startX: Float = Random.nextFloat(),
    val startY: Float = 0.44f + Random.nextFloat() * 0.18f,
    val driftX: Float = (Random.nextFloat() - 0.5f) * 0.18f,
    val speed: Float = 0.3f + Random.nextFloat() * 0.42f
)

@Composable
fun CompanionScreen(viewModel: MainViewModel? = null) {
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val actions = remember {
        listOf(
            CompanionActionUi("🤚", "安抚", "慢慢靠近，降低紧张", Color(0xFFF48FB1)),
            CompanionActionUi("👀", "观察", "保持距离，记录状态", Color(0xFF81D4FA)),
            CompanionActionUi("💧", "补水", "优先关注饮水健康", Color(0xFF4DD0E1)),
            CompanionActionUi("🐟", "添粮", "少量记录，避免过喂", Color(0xFFFFCC80))
        )
    }
    var selectedAction by remember { mutableStateOf(actions[1]) }
    var catFeedback by remember { mutableStateOf("小黑在休息区慢慢放松，适合安静陪伴。") }
    var showDataPanel by remember { mutableStateOf(false) }
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    val coroutineScope = rememberCoroutineScope()

    fun handleAction(action: CompanionActionUi) {
        val actionSucceeded = viewModel?.interactWithCat(action.label, "小黑", 5) ?: true
        if (!actionSucceeded) {
            selectedAction = action
            catFeedback = "小鱼干不足，先完成签到或学习任务再来陪伴小黑吧。"
            return
        }

        selectedAction = action
        catFeedback = when (action.label) {
            "安抚" -> "你放低动作，小黑眯起眼睛，尾巴轻轻摆了一下。"
            "观察" -> "你保持了安全距离，小黑状态稳定，没有出现应激反应。"
            "补水" -> "补水提醒已点亮，今天优先关注饮水和泌尿健康。"
            "添粮" -> "已记录一次轻量添粮，系统提醒不要高频投喂零食。"
            else -> "已完成一次温和陪伴。"
        }
        val newParticles = List(8) { Particle(emoji = action.emoji) }
        particles = particles + newParticles
        coroutineScope.launch {
            delay(2200L)
            particles = particles.filterNot { it in newParticles }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompanionAtmosphereBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompanionTopBar(
                tokens = uiState?.tokenBalance ?: 350,
                level = uiState?.petLevel ?: 5,
                onOpenData = { showDataPanel = true }
            )
            CompanionHeroCard(
                feedback = catFeedback,
                selectedAction = selectedAction,
                onDoubleTap = { handleAction(actions.first { it.label == "安抚" }) }
            )
            CareVitalsCard(
                hunger = uiState?.hungerValue ?: 0.7f,
                happiness = uiState?.happinessValue ?: 0.85f,
                health = uiState?.healthValue ?: 0.92f,
                exp = uiState?.petExp ?: 320,
                expToNext = uiState?.petExpToNext ?: 500
            )
            CareActionPanel(
                actions = actions,
                selectedAction = selectedAction,
                onAction = ::handleAction
            )
            TodayCompanionInsight(selectedAction = selectedAction)
            RecentCompanionRecords(records = uiState?.companionRecords ?: emptyList())
            Spacer(modifier = Modifier.height(24.dp))
        }

        FloatingParticles(particles = particles)

        AnimatedVisibility(
            visible = showDataPanel,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DataPanelOverlay(
                onClose = { showDataPanel = false },
                uiState = uiState
            )
        }
    }
}

@Composable
fun CompanionAtmosphereBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF151827), Color(0xFF283447), Color(0xFF5A3A2B)),
                startY = 0f,
                endY = h
            )
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x55FFB74D), Color.Transparent)),
            radius = w * 0.56f,
            center = Offset(w * 0.74f, h * 0.18f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x5538BDF8), Color.Transparent)),
            radius = w * 0.48f,
            center = Offset(w * 0.15f, h * 0.54f)
        )
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA160F16))),
            topLeft = Offset.Zero,
            size = size
        )
    }
}

@Composable
fun CompanionTopBar(tokens: Int, level: Int, onOpenData: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "云陪伴 · 安静模式",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.45f), blurRadius = 8f))
            )
            Text("Lv.$level 小黑 · 今日建议补水优先", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassPill(text = "🐾 $tokens")
            IconButton(
                onClick = onOpenData,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            ) {
                Icon(Icons.Outlined.Analytics, contentDescription = "数据中心", tint = Color.White)
            }
        }
    }
}

@Composable
fun GlassPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color(0xFFFFE082), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CompanionHeroCard(feedback: String, selectedAction: CompanionActionUi, onDoubleTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .shadow(28.dp, RoundedCornerShape(36.dp), spotColor = Color.Black.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(36.dp))
    ) {
        CatModel3DViewer(
            modifier = Modifier.fillMaxSize(),
            modelAssetPath = "models/cat.glb",
            label = "",
            isFullScreen = false,
            mode = CatViewerMode.COMPANION,
            onDoubleTap = onDoubleTap
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.32f))
                .border(1.dp, selectedAction.color.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(horizontal = 13.dp, vertical = 9.dp)
        ) {
            Text("${selectedAction.emoji} ${selectedAction.label}反馈", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .padding(16.dp)
        ) {
            Text(
                text = feedback,
                color = Color(0xFF35241C),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CareVitalsCard(hunger: Float, happiness: Float, health: Float, exp: Int, expToNext: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("陪伴状态", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2F241F))
                    Text("轻量照护，不打扰真实猫咪", fontSize = 12.sp, color = Color(0xFF7A675E))
                }
                Text("EXP $exp/$expToNext", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            VitalMeter("🍖", "饱食", hunger, Color(0xFFFFA726))
            VitalMeter("💗", "心情", happiness, Color(0xFFF06292))
            VitalMeter("🌿", "健康", health, Color(0xFF66BB6A))
        }
    }
}

@Composable
fun VitalMeter(icon: String, label: String, value: Float, color: Color) {
    val animatedValue by animateFloatAsState(targetValue = value, animationSpec = tween(600), label = label)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(icon, fontSize = 16.sp)
        Text(label, modifier = Modifier.width(38.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B3A31))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE8DDD2))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedValue.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.65f), color)))
            )
        }
        Text("${(animatedValue * 100).toInt()}%", modifier = Modifier.width(36.dp), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun CareActionPanel(actions: List<CompanionActionUi>, selectedAction: CompanionActionUi, onAction: (CompanionActionUi) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("选择陪伴方式", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("每次消耗 5 小鱼干", color = Color.White.copy(alpha = 0.62f), fontSize = 12.sp)
                }
                GlassPill(text = selectedAction.label)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                actions.chunked(2).forEach { rowActions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        rowActions.forEach { action ->
                            CareActionButton(
                                action = action,
                                selected = action.label == selectedAction.label,
                                onClick = { onAction(action) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareActionButton(action: CompanionActionUi, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.96f else 1f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 650f), label = action.label)
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(120L)
            pressed = false
        }
    }

    Row(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) action.color.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.1f))
            .border(1.dp, if (selected) action.color.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(22.dp))
            .clickable {
                pressed = true
                onClick()
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(action.color.copy(alpha = 0.55f), Color.White.copy(alpha = 0.06f))))
                .border(1.dp, action.color.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(action.emoji, fontSize = 22.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(action.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Text(action.subtitle, color = Color.White.copy(alpha = 0.62f), fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
fun TodayCompanionInsight(selectedAction: CompanionActionUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest.copy(alpha = 0.94f))
    ) {
        Row(modifier = Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(selectedAction.color.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                Text(selectedAction.emoji, fontSize = 20.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("今日陪伴建议", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text("以观察和补水为主，安抚只在猫咪主动靠近时进行。添粮保持少量记录，避免把云陪伴变成重复投喂。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun RecentCompanionRecords(records: List<CompanionRecord>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("最近陪伴", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            if (records.isEmpty()) {
                Text("还没有新的陪伴记录", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                records.take(3).forEach { record ->
                    CompanionRecordRow(record = record)
                }
            }
        }
    }
}

@Composable
fun CompanionRecordRow(record: CompanionRecord) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val dotColor = when (record.colorType) {
            1 -> MaterialTheme.colorScheme.primary
            2 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.tertiary
        }
        Box(modifier = Modifier.size(9.dp).background(dotColor, CircleShape))
        Column {
            Text("${record.time.substringAfter(" ")} · ${record.action}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(record.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
        }
    }
}

@Composable
fun FloatingParticles(particles: List<Particle>) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "particle-progress"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawParticle(particle = particle, progress = progress)
        }
    }
}

fun DrawScope.drawParticle(particle: Particle, progress: Float) {
    val age = (progress * particle.speed * 3f) % 1f
    val x = (particle.startX + particle.driftX * age + sin(age * 6f) * 0.02f) * size.width
    val y = (particle.startY - age * 0.38f) * size.height
    val alpha = (1f - age).coerceIn(0f, 1f)
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = 46f
            this.alpha = (alpha * 255).toInt()
        }
        drawText(particle.emoji, x, y, paint)
    }
}

@Composable
fun DataPanelOverlay(onClose: () -> Unit, uiState: MainAppState?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClose)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable { }
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("陪伴数据中心", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("查看今日互动与健康建议", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                DataMetricGrid(uiState = uiState)
                DataCallout()
                RecentCompanionRecords(records = uiState?.companionRecords ?: emptyList())
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DataMetricGrid(uiState: MainAppState?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        DataMetricCard(icon = Icons.Outlined.Favorite, label = "心情", value = "${((uiState?.happinessValue ?: 0.85f) * 100).toInt()}%", modifier = Modifier.weight(1f))
        DataMetricCard(icon = Icons.Outlined.Info, label = "记录", value = "${uiState?.companionRecords?.size ?: 0} 次", modifier = Modifier.weight(1f))
    }
}

@Composable
fun DataMetricCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SurfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DataCallout() {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighest.copy(alpha = 0.7f))) {
        Text(
            text = "科学建议：今天优先补水与远距离观察。若猫咪耳朵后压、尾巴快速摆动，请停止互动。",
            modifier = Modifier.padding(16.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )
    }
}
