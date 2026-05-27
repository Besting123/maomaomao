package com.example.myapplication.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.myapplication.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import androidx.navigation.NavController

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
fun CompanionScreen(navController: NavController? = null, viewModel: MainViewModel? = null, modifier: Modifier = Modifier.fillMaxSize()) {
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val colorScheme = MaterialTheme.colorScheme
    val actions = remember(colorScheme) {
        listOf(
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
                "🤚", "安抚", "慢慢靠近，降低紧张", colorScheme.primary,
                "Pet",
                "什么时候可以安抚？",
                "只有猫咪主动靠近、尾巴放松、没有后退时，才适合短时间轻柔安抚。",
                "先伸手停住，让猫自己决定是否靠近。",
                "不要追、抱、摸肚子或强行贴近。"
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
    var selectedAction by remember { mutableStateOf(actions.first()) }
    var selectedCatName by remember { mutableStateOf("小黑") }
    var showCatPicker by remember { mutableStateOf(false) }
    val selectableCats = remember { listOf("小黑", "大橘", "奶油") }
    var catFeedback by remember { mutableStateOf("小黑在休息区慢慢放松，适合先观察，再决定是否补水或安抚。") }

    fun handleAction(action: CompanionActionUi) {
        val actionSucceeded = viewModel?.interactWithCat(action.label, selectedCatName, 5) ?: true
        if (!actionSucceeded) {
            selectedAction = action
            catFeedback = "小鱼干不足，先完成签到或学习任务再来陪伴${selectedCatName}吧。"
            return
        }

        selectedAction = action
        catFeedback = when (action.label) {
            "安抚" -> "你放低动作，$selectedCatName 眯起眼睛，尾巴轻轻摆了一下。"
            "观察" -> "你保持了安全距离，$selectedCatName 状态稳定，没有出现应激反应。"
            "补水" -> "$selectedCatName 的补水提醒已点亮，今天优先关注饮水和泌尿健康。"
            "添粮" -> "已记录一次轻量添粮，系统提醒不要高频投喂零食。"
            else -> "已完成一次温和陪伴。"
        }
    }

    fun chooseCat(catName: String) {
        selectedCatName = catName
        viewModel?.selectProfileCat(catName)
        catFeedback = "$catName 已切换为当前陪伴对象，先观察状态再互动。"
        showCatPicker = false
    }

    Box(modifier = modifier) {
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
                catName = selectedCatName,
                onChooseCat = { showCatPicker = true }
            )
            CompanionHeroCard(
                feedback = catFeedback,
                catName = selectedCatName,
                selectedAction = selectedAction,
                actions = actions,
                hunger = uiState?.hungerValue ?: 0.7f,
                happiness = uiState?.happinessValue ?: 0.85f,
                health = uiState?.healthValue ?: 0.92f,
                onAction = ::handleAction,
                onDoubleTap = { handleAction(actions.first()) },
                onOpenProfile = {
                    viewModel?.selectProfileCat(selectedCatName)
                    navController?.navigate("catProfile")
                }
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

        if (showCatPicker) {
            CatPickerDialog(
                cats = selectableCats,
                selectedCatName = selectedCatName,
                onSelect = ::chooseCat,
                onDismiss = { showCatPicker = false }
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
fun CompanionTopBar(tokens: Int, level: Int, catName: String, onChooseCat: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(50))
                    .clickable { onChooseCat() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(
                    text = "选择猫咪 · $catName",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(shadow = Shadow(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), blurRadius = 8f))
                )
            }
            Text("Lv.$level · 默认先观察，再决定补水或安抚", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassPill(text = "🐟 $tokens")
        }
    }
}

@Composable
fun CatPickerDialog(cats: List<String>, selectedCatName: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择猫咪", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cats.forEach { catName ->
                    TextButton(onClick = { onSelect(catName) }) {
                        Text(if (catName == selectedCatName) "$catName · 当前" else catName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
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
    catName: String,
    selectedAction: CompanionActionUi,
    actions: List<CompanionActionUi>,
    hunger: Float,
    happiness: Float,
    health: Float,
    onAction: (CompanionActionUi) -> Unit,
    onDoubleTap: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp)
            .shadow(22.dp, RoundedCornerShape(36.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(36.dp))
            .background(SurfaceContainerLowest.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(36.dp))
    ) {
        CatModel3DViewer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(410.dp),
            modelAssetPath = "models/mao-xiaohei-rigged.glb",
            label = "3D $catName 陪伴模型",
            isFullScreen = false,
            mode = CatViewerMode.COMPANION,
            animationName = selectedAction.animationName,
            onDoubleTap = onDoubleTap
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceContainerLowest.copy(alpha = 0.92f))
                .border(1.dp, selectedAction.color.copy(alpha = 0.28f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(selectedAction.color, CircleShape))
            Text("-5 🐟", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = selectedAction.color)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 118.dp)
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
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp, top = 126.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(50))
                .clickable { onOpenProfile() }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("档案", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
        }
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
                Text("推荐：${selectedAction.subtitle}", fontSize = 10.sp, color = selectedAction.color, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (selectedAction.label == "观察") {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("默认推荐", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Text("-5 🐟", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
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
            Text("长期陪伴记录", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
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
