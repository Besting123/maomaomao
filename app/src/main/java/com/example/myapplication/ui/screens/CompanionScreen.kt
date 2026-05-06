package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

data class CompanionActionUi(
    val emoji: String,
    val label: String,
    val subtitle: String,
    val color: Color,
    val animationName: String,
    val lessonTitle: String,
    val lesson: String,
    val doTip: String,
    val avoidTip: String
)

@Composable
fun CompanionScreen(viewModel: MainViewModel? = null) {
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val colorScheme = MaterialTheme.colorScheme
    val actions = remember(colorScheme) {
        listOf(
            CompanionActionUi(
                "🤚", "安抚", "慢慢靠近，降低紧张", colorScheme.primary,
                "Pet",
                "什么时候可以安抚？",
                "只有猫咪主动靠近、尾巴放松、没有后退时，才适合短时间轻柔安抚。",
                "先伸手停住，让猫自己决定是否靠近。",
                "不要追、抱、摸肚子或强行贴近。"
            ),
            CompanionActionUi(
                "👀", "观察", "保持距离，记录状态", colorScheme.secondary,
                "Observe",
                "观察比接触更安全",
                "耳朵、尾巴、瞳孔和身体姿态能判断猫咪是否紧张；多数校园猫更适合远距离记录。",
                "保持 2 米以上距离，记录精神、步态和食欲。",
                "不要围堵、闪光拍照或多人聚集。"
            ),
            CompanionActionUi(
                "💧", "补水", "优先关注饮水健康", colorScheme.tertiary,
                "Drink",
                "补水通常优先于投喂",
                "流浪猫常见风险是饮水不足。补水点清洁、稳定，比临时零食更有帮助。",
                "确认水碗干净，优先补充清水。",
                "不要倒牛奶、饮料或不明液体。"
            ),
            CompanionActionUi(
                "🐟", "添粮", "少量记录，避免过喂", colorScheme.primaryContainer,
                "Eat",
                "为什么不能随便投喂？",
                "重复投喂会导致肥胖、挑食和区域聚集。添粮应少量、定点、记录频率。",
                "只做轻量补充，并观察是否已有食物。",
                "不要喂高盐零食、骨头、剩饭或大量猫条。"
            )
        )
    }
    var selectedAction by remember { mutableStateOf(actions[1]) }
    var catFeedback by remember { mutableStateOf("小黑在休息区慢慢放松，适合安静陪伴。") }
    var showDataPanel by remember { mutableStateOf(false) }

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
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompanionAtmosphereBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CompanionTopBar(
                tokens = uiState?.tokenBalance ?: 350,
                level = uiState?.petLevel ?: 5,
                onOpenData = { showDataPanel = true }
            )
            CompanionHeroCard(
                feedback = catFeedback,
                selectedAction = selectedAction,
                actions = actions,
                hunger = uiState?.hungerValue ?: 0.7f,
                happiness = uiState?.happinessValue ?: 0.85f,
                health = uiState?.healthValue ?: 0.92f,
                onAction = ::handleAction,
                onDoubleTap = { handleAction(actions.first { it.label == "安抚" }) }
            )
            TodayCompanionInsight(
                selectedAction = selectedAction,
                hunger = uiState?.hungerValue ?: 0.7f,
                happiness = uiState?.happinessValue ?: 0.85f,
                health = uiState?.healthValue ?: 0.92f
            )
            CareVitalsCard(
                hunger = uiState?.hungerValue ?: 0.7f,
                happiness = uiState?.happinessValue ?: 0.85f,
                health = uiState?.healthValue ?: 0.92f,
                exp = uiState?.petExp ?: 320,
                expToNext = uiState?.petExpToNext ?: 500
            )
            RecentCompanionRecords(records = uiState?.companionRecords ?: emptyList())
            Spacer(modifier = Modifier.height(24.dp))
        }

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
    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    colorScheme.background,
                    SurfaceContainerLow,
                    colorScheme.primaryContainer.copy(alpha = 0.38f)
                ),
                startY = 0f,
                endY = h
            )
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(colorScheme.primaryContainer.copy(alpha = 0.6f), Color.Transparent)),
            radius = w * 0.56f,
            center = Offset(w * 0.74f, h * 0.18f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(colorScheme.tertiaryContainer.copy(alpha = 0.5f), Color.Transparent)),
            radius = w * 0.48f,
            center = Offset(w * 0.15f, h * 0.54f)
        )
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, SurfaceContainer.copy(alpha = 0.7f))),
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
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(shadow = Shadow(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), blurRadius = 8f))
            )
            Text("Lv.$level 小黑 · 今日建议补水优先", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassPill(text = "🐾 $tokens")
            IconButton(
                onClick = onOpenData,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Outlined.Analytics, contentDescription = "数据中心", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun GlassPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CompanionHeroCard(
    feedback: String,
    selectedAction: CompanionActionUi,
    actions: List<CompanionActionUi>,
    hunger: Float,
    happiness: Float,
    health: Float,
    onAction: (CompanionActionUi) -> Unit,
    onDoubleTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp)
            .shadow(22.dp, RoundedCornerShape(36.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(36.dp))
            .background(SurfaceContainerLowest.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(36.dp))
    ) {
        CatModel3DViewer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(420.dp),
            modelAssetPath = "models/mao-lihua-animated.glb",
            label = "3D 真实猫咪建模",
            isFullScreen = false,
            mode = CatViewerMode.COMPANION,
            animationName = selectedAction.animationName,
            onDoubleTap = onDoubleTap
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(18.dp)
                .fillMaxWidth(0.62f)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerLowest.copy(alpha = 0.92f))
                .border(1.dp, selectedAction.color.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(horizontal = 13.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("${selectedAction.emoji} ${selectedAction.label}反馈", color = selectedAction.color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                text = feedback,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp
            )
            Text(
                text = "学习点：${selectedAction.lessonTitle}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            HeroStatusPill("饱食", hunger, MaterialTheme.colorScheme.primary)
            HeroStatusPill("心情", happiness, MaterialTheme.colorScheme.secondary)
            HeroStatusPill("健康", health, MaterialTheme.colorScheme.tertiary)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 112.dp)
                .width(84.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(selectedAction.color.copy(alpha = 0.28f))
        )

        HeroActionDock(
            actions = actions,
            selectedAction = selectedAction,
            onAction = onAction,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        )
    }
}

@Composable
fun HeroStatusPill(label: String, value: Float, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceContainerLowest.copy(alpha = 0.88f))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("${(value * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun HeroActionDock(actions: List<CompanionActionUi>, selectedAction: CompanionActionUi, onAction: (CompanionActionUi) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceContainer.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("陪伴操作台", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(selectedAction.subtitle, fontSize = 10.sp, color = selectedAction.color, fontWeight = FontWeight.Bold)
            }
            Text("-5 🐾", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            actions.forEach { action ->
                HeroActionButton(
                    action = action,
                    selected = action.label == selectedAction.label,
                    onClick = { onAction(action) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HeroActionButton(action: CompanionActionUi, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.95f else 1f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 650f), label = "hero-${action.label}")
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(120L)
            pressed = false
        }
    }
    Column(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) action.color.copy(alpha = 0.18f) else SurfaceContainerLowest)
            .border(1.dp, if (selected) action.color.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable { pressed = true; onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(action.emoji, fontSize = 18.sp)
        Text(action.label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        if (selected) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(action.color.copy(alpha = 0.78f))
            )
        }
    }
}

@Composable
fun CareVitalsCard(hunger: Float, happiness: Float, health: Float, exp: Int, expToNext: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest.copy(alpha = 0.96f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("陪伴状态", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("轻量照护，不打扰真实猫咪", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("EXP $exp/$expToNext", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            VitalMeter("🍖", "饱食", hunger, MaterialTheme.colorScheme.primary)
            VitalMeter("💗", "心情", happiness, MaterialTheme.colorScheme.secondary)
            VitalMeter("🌿", "健康", health, MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun VitalMeter(icon: String, label: String, value: Float, color: Color) {
    val animatedValue by animateFloatAsState(targetValue = value, animationSpec = tween(600), label = label)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(icon, fontSize = 16.sp)
        Text(label, modifier = Modifier.width(38.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceContainerHighest)
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
fun TodayCompanionInsight(selectedAction: CompanionActionUi, hunger: Float, happiness: Float, health: Float) {
    val statusHint = when {
        selectedAction.label == "添粮" && hunger > 0.82f -> "当前饱食度较高，更适合观察或补水，避免重复投喂。"
        selectedAction.label == "安抚" && happiness < 0.68f -> "心情较低时先观察距离和应激信号，不要急于触碰。"
        selectedAction.label == "补水" && health < 0.85f -> "健康值偏低时，补水提醒和异常记录比临时投喂更有意义。"
        selectedAction.label == "观察" -> "观察是最低压力的陪伴方式，适合作为所有互动前的第一步。"
        else -> selectedAction.lesson
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(selectedAction.color.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                    Text(selectedAction.emoji, fontSize = 20.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(selectedAction.lessonTitle, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(statusHint, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                WelfareTipCard(title = "建议这样做", text = selectedAction.doTip, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                WelfareTipCard(title = "避免这样做", text = selectedAction.avoidTip, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WelfareTipCard(title: String, text: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text, fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun DataPanelOverlay(onClose: () -> Unit, uiState: MainAppState?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
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
