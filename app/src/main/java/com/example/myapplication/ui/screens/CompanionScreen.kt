package com.example.myapplication.ui.screens

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.MainViewModel

@Composable
fun CompanionScreen(viewModel: MainViewModel? = null) {
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var catMood by remember { mutableStateOf("活泼") }
    var catFeedback by remember { mutableStateOf("小黑正在轻轻呼吸，适合远距离陪伴。") }
    var actionPulse by remember { mutableStateOf(0) }
    var offlineClaimed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(96.dp)) 

            PolaroidStatusSection(
                mood = catMood,
                feedback = catFeedback,
                actionPulse = actionPulse
            )
            Spacer(modifier = Modifier.height(32.dp))

            StatsGridSection()
            Spacer(modifier = Modifier.height(32.dp))

            TipCalloutSection()
            Spacer(modifier = Modifier.height(32.dp))

            CompanionConsoleSection(onAction = { actionName ->
                viewModel?.interactWithCat(actionName, "小黑", 5)
                catMood = when (actionName) {
                    "安抚" -> "放松"
                    "观察" -> "安心"
                    "记录" -> "被看见"
                    "补水" -> "满足"
                    else -> "期待"
                }
                catFeedback = when (actionName) {
                    "安抚" -> "小黑眯起眼睛，尾巴放松地摆了一下。"
                    "观察" -> "你保持了安全距离，小黑没有出现应激反应。"
                    "记录" -> "这次陪伴已写入今日观察记录。"
                    "补水" -> "补水提醒已点亮，优先关注泌尿健康。"
                    else -> "已记录一次轻量添粮，注意不要过度投喂。"
                }
                actionPulse++
            })
            Spacer(modifier = Modifier.height(32.dp))

            CompanionRecordSection(records = uiState?.companionRecords ?: emptyList())
            Spacer(modifier = Modifier.height(32.dp))

            OfflineLinkageSection(
                claimed = offlineClaimed,
                onClaimClick = { offlineClaimed = !offlineClaimed }
            )
            
            Spacer(modifier = Modifier.height(120.dp))
        }

        CompanionTopAppBar(onNotificationClick = {
            catFeedback = "今日提醒：傍晚 17:00 后更适合远观记录。"
            actionPulse++
        })
    }
}

@Composable
fun CompanionTopAppBar(onNotificationClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_net_b422dd3a07),
                    contentDescription = "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = "陪伴中心",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
            )
        }
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(40.dp).clip(CircleShape)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PolaroidStatusSection(mood: String, feedback: String, actionPulse: Int) {
    // Breathing animation for cat image
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_alpha"
    )
    val interactionScale by animateFloatAsState(
        targetValue = if (actionPulse % 2 == 0) 1f else 1.04f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "interaction_scale"
    )
    val moodColor = when (mood) {
        "放松", "安心" -> MaterialTheme.colorScheme.secondaryContainer
        "满足" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val moodEmoji = when (mood) {
        "放松" -> "😌"
        "安心" -> "👀"
        "满足" -> "💧"
        "被看见" -> "📝"
        else -> "🐾"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(-1f)
                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0A383833)),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f/3f)
                        .clip(RoundedCornerShape(8.dp))
                        .graphicsLayer {
                            scaleX = breatheScale * interactionScale
                            scaleY = breatheScale * interactionScale
                            alpha = breatheAlpha
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_net_c9e15cf0b7),
                        contentDescription = "小黑",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.82f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("2D 动态形象演示", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(moodColor, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("$moodEmoji $mood", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("小黑", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("今日状态：$mood", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("健康状况 优", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = feedback,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
        
        // Decorative Blur Element
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), CircleShape)
                .blur(16.dp)
        )
    }
}

@Composable
fun StatsGridSection() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Card 1
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Favorite, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("善意值", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("1,280", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("已累计救助 12 次", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
        }
        
        // Card 2
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = "Water", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Text("补水包", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("08", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                    Text(" 份", fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 2.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("预计消耗 2 份/日", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)))
        }
    }
}

@Composable
fun TipCalloutSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            .border(width = 0.dp, color = Color.Transparent) // Need left border
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left border trick: Add a Box instead if wanted, but simpler to use internal row
        Icon(Icons.Outlined.Info, contentDescription = "Idea", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("科学饮食建议", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Text(
                text = "当前环境湿度较低，建议补水优先。近期小黑已有3次零食记录，为了预防肥胖，不建议高频喂食零食。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanionConsoleSection(onAction: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3
        ) {
            // Button 1: 安抚
            Button(
                onClick = { onAction("安抚") },
                modifier = Modifier.weight(1f).aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Comfort", modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                    Text("安抚", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
            
            // Button 2: 观察
            Button(
                onClick = { onAction("观察") },
                modifier = Modifier.weight(1f).aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Visibility, contentDescription = "Observe", modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                    Text("观察", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            // Button 3: 记录
            Button(
                onClick = { onAction("记录") },
                modifier = Modifier.weight(1f).aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Record", modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                    Text("记录", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            // Button 4: 补水
            Button(
                onClick = { onAction("补水") },
                modifier = Modifier.weight(1f).aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Opacity, contentDescription = "Water", modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                    Text("补水", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            // Button 5: 添粮
            Button(
                onClick = { onAction("添粮") },
                modifier = Modifier.weight(1f).aspectRatio(1f).shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Restaurant, contentDescription = "Feed", modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                    Text("添粮", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            // Empty box to balance the grid
            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
        }
        
        // Feedback
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
                .padding(vertical = 12.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = "Done", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("已完成今日陪伴关怀", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun CompanionRecordSection(records: List<com.example.myapplication.ui.viewmodel.CompanionRecord>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Text("今日陪伴记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (records.isEmpty()) {
                    Text("今日暂无互动记录", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    records.take(5).forEach { record ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val dotColor = when (record.colorType) {
                                1 -> MaterialTheme.colorScheme.primary
                                2 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                            Text("${record.time.substringAfter(" ")} ${record.description}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineLinkageSection(claimed: Boolean = false, onClaimClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHigh)
            .padding(24.dp)
    ) {
        // Circle deco
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 64.dp, y = (-64).dp)
                .size(128.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape)
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("线下联动入口", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("认领一次线下补水提醒，让校园志愿者精准关怀。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(200.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClaimClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (claimed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        contentColor = if (claimed) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.surface
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(if (claimed) "已认领" else "认领提醒", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .shadow(1.dp, RoundedCornerShape(8.dp), clip = false),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = "Location", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            }
        }
    }
}
