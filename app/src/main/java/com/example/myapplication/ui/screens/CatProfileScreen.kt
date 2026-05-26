package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.SurfaceContainerHigh
import com.example.myapplication.ui.theme.SurfaceContainerHighest
import com.example.myapplication.ui.theme.SurfaceContainerLow
import com.example.myapplication.ui.theme.SurfaceContainerLowest
import com.example.myapplication.ui.utils.sharePlainText
import com.example.myapplication.ui.viewmodel.CompanionRecord
import com.example.myapplication.ui.viewmodel.MainViewModel

private data class CatProfileUi(
    val name: String,
    val codeLine: String,
    val tags: List<String>,
    val imageRes: Int,
    val personality: String,
    val memoryQuote: String,
    val memoryAuthor: String,
    val healthText: String,
    val theaterFirst: String,
    val theaterSecond: String
)

private fun catProfileFor(catName: String): CatProfileUi = when (catName) {
    "小黑" -> CatProfileUi(
        name = "小黑",
        codeLine = "编号: SC-2023-011 · 教学区边缘常驻",
        tags = listOf("小黑", "安静型", "适合远观"),
        imageRes = R.drawable.img_net_8c081179f2,
        personality = "小黑习惯在教学区边缘慢慢移动，遇到人多时会主动拉开距离。它更适合固定时段的远观记录和补水提醒，不适合突然靠近或多人围观。",
        memoryQuote = "它从树影里绕出来，又很快回到安静的角落。",
        memoryAuthor = "— 2023.11.08 · 教学区观察员",
        healthText = "如发现小黑步态异常、长时间躲藏或精神下降，请联系校园流浪猫志愿者团队。日常记录只保留片区，不公开精确停留点。",
        theaterFirst = "每天傍晚，小黑常沿教学区边缘短暂停留。保持距离时，它会放慢脚步；靠得太近时，它会直接退回树影里。",
        theaterSecond = "小黑对补水点很敏感。水碗干净时会停留更久，因此比起零食，稳定清水更适合它。"
    )
    "奶油" -> CatProfileUi(
        name = "奶油",
        codeLine = "编号: SC-2023-015 · 教学区休息片区",
        tags = listOf("奶油", "稳定型", "爱晒太阳"),
        imageRes = R.drawable.img_net_27ce5092c2,
        personality = "奶油常在教学区和草坪边缘活动，状态稳定，但休息时不喜欢被打扰。适合远距离拍照记录，不建议在午后休息时靠近。",
        memoryQuote = "它趴在草坪边缘晒太阳，听到脚步声也没有马上离开。",
        memoryAuthor = "— 2023.11.10 · 图书馆路过同学",
        healthText = "如发现奶油眼鼻分泌物增多、食欲下降或长时间不移动，请记录片区并联系志愿者，不自行用药或抓捕。",
        theaterFirst = "午后的奶油很会挑位置，常在有阳光但不吵的地方休息。远远看一眼就足够，不需要靠近确认。",
        theaterSecond = "奶油对人群比较淡定，但多人围观仍会让它紧张。一个人安静记录，比一群人靠近更合适。"
    )
    else -> CatProfileUi(
        name = "大橘",
        codeLine = "编号: SC-2023-009 · 艺术学院常驻嘉宾",
        tags = listOf("橘子", "警惕型", "爱晒太阳"),
        imageRes = R.drawable.img_net_a53f9ce8f2,
        personality = "大橘是一只典型的“慢热型”橘猫。初次见面时会保持警惕距离，但一旦建立信任，就会展现出极其黏人的一面。它对固定的几位志愿者表现出明显的偏好，会主动蹭腿、翻肚皮。对陌生人则保持礼貌但疏离的态度。",
        memoryQuote = "它在蹭我的画架，仿佛在指导我构图。",
        memoryAuthor = "— 2023.11.12 · 某大一新生",
        healthText = "如发现大橘出现精神萎靡、食欲下降、毛发异常脱落等情况，请联系校园流浪猫志愿者团队。日常照护以固定片区规则为准，不公开精确补给位置。",
        theaterFirst = "每天早上，大橘常在教学区附近短暂停留。但如果你试图直接摸它，它会优雅地后退两步，用眼神告诉你：“请先保持距离。”",
        theaterSecond = "大橘对画室有着莫名的执着。它会趁门没关严时溜进去，在画架之间巡视一圈，最后选一个最碍事的位置趴下，仿佛在说：“今天的构图，我来把关。”"
    )
}

@Composable
fun CatProfileScreen(onBackClick: () -> Unit, viewModel: MainViewModel? = null, catName: String? = null) {
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val selectedCatName = catName ?: uiState?.selectedProfileCatName ?: "大橘"
    val profile = remember(selectedCatName) { catProfileFor(selectedCatName) }
    var showMoreDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            CatProfileHeroSection(
                profile = profile,
                isFollowed = uiState?.followedCatNames?.contains(profile.name) ?: false,
                onToggleFollow = { viewModel?.toggleCatFollow(profile.name) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            CatMoodIndicatorSection()
            Spacer(modifier = Modifier.height(24.dp))

            CatInteractionBoundarySection()
            Spacer(modifier = Modifier.height(24.dp))

            CatPersonalitySection(profile)
            Spacer(modifier = Modifier.height(24.dp))

            CatMemoryPolaroid(profile)
            Spacer(modifier = Modifier.height(24.dp))

            CatLocationSection()
            Spacer(modifier = Modifier.height(24.dp))

            CatHealthAdviceSection(profile)
            Spacer(modifier = Modifier.height(24.dp))

            CatTimelineSection(profile)
            Spacer(modifier = Modifier.height(24.dp))
            
            CatMyCompanionRecordsSection(records = uiState?.companionRecords.orEmpty().filter { it.catName == profile.name }, profile = profile)
            Spacer(modifier = Modifier.height(24.dp))

            CatPersonalityTheater(profile)
            Spacer(modifier = Modifier.height(48.dp))
        }

        CatProfileTopBar(profile = profile, onBackClick = onBackClick, onOpenMore = { showMoreDialog = true })
        if (showMoreDialog) {
            CatProfileMoreDialog(onDismiss = { showMoreDialog = false })
        }
    }
}

@Composable
private fun CatProfileTopBar(profile: CatProfileUi, onBackClick: () -> Unit, onOpenMore: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "档案详情",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Row {
            IconButton(onClick = {
                sharePlainText(
                    context = context,
                    chooserTitle = "分享猫咪档案",
                    subject = "喵伴云养猫咪档案：${profile.name}",
                    body = "${profile.name} · ${profile.codeLine.removePrefix("编号: ")}\n\n${profile.personality}\n\n互动提示：不追逐、不围堵、不公开精确位置。"
                )
            }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onOpenMore) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CatProfileMoreDialog(onDismiss: () -> Unit) {
    var detailTitle by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("档案操作", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (detailTitle == null) {
                    CatProfileActionRow(Icons.Outlined.History, "查看照护历史", "近 30 日远观、补水、健康提醒已汇总。") { detailTitle = "照护历史" }
                    CatProfileActionRow(Icons.Outlined.Block, "上报不当互动", "发现追逐、围堵、闪光拍摄时可记录线索。") { detailTitle = "不当互动记录" }
                    CatProfileActionRow(Icons.Outlined.AutoStories, "阅读互动指南", "先观察尾巴、耳朵和身体姿态，再决定是否靠近。") { detailTitle = "互动指南" }
                } else {
                    Text(detailTitle ?: "档案操作", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = when (detailTitle) {
                            "照护历史" -> "示例：10月24日完成补水；10月22日完成远观；10月20日志愿者记录步态正常。"
                            "不当互动记录" -> "如发现追逐、围堵、闪光拍摄，请记录时间段、片区和现象，不公开精确点位。"
                            else -> "先观察耳朵、尾巴和身体姿态；猫咪主动靠近前不伸手，后退或飞机耳时立即停止。"
                        },
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (detailTitle == null) onDismiss() else detailTitle = null }, shape = CircleShape) { Text(if (detailTitle == null) "完成" else "返回") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun CatProfileActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable { onClick() }, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun CatProfileHeroSection(profile: CatProfileUi, isFollowed: Boolean, onToggleFollow: () -> Unit) {
    val moodLabel = if (isFollowed) "亲近中" else "保持观察"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(480.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = profile.imageRes),
            contentDescription = "${profile.name}档案图",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text("🐾 $moodLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                profile.tags.take(3).forEachIndexed { index, tag ->
                    val colors = when (index) {
                        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    }
                    CatTag(tag, colors.first, colors.second)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.name,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profile.codeLine,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Text("健康", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    Text("A+", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onToggleFollow()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowed) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary,
                        contentColor = if (isFollowed) Color.White else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        if (isFollowed) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFollowed) "已关注" else "关注此猫", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CatMoodIndicatorSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.Mood, title = "今日心情与防备心")
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Mood
            Column(
                modifier = Modifier.weight(1f).background(SurfaceContainerLow, RoundedCornerShape(16.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("☀️", fontSize = 32.sp)
                Text("心情愉悦", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("适合互动", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            // Defense Level
            Column(
                modifier = Modifier.weight(1f).background(SurfaceContainerLow, RoundedCornerShape(16.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🛡️", fontSize = 32.sp)
                Text("防备心：低", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("可尝试轻抚", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CatInteractionBoundarySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.Block, title = "互动边界指南")
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                    Text("🟢 绿灯区：下巴、额头、耳根（非常享受）", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF9800), CircleShape))
                    Text("🟡 黄灯区：背部（视心情而定，不可长摸）", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                    Text("🔴 红灯区：肚子、尾巴、爪子（绝对禁区！）", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CatTag(label: String, bgColor: Color, textColor: Color) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CatPersonalitySection(profile: CatProfileUi) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.Psychology, title = "个性与习惯")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.personality,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        HabitCard(
            borderColor = MaterialTheme.colorScheme.primary,
            title = "社交属性",
            description = "熟人限定热情"
        )
        Spacer(modifier = Modifier.height(10.dp))
        HabitCard(
            borderColor = MaterialTheme.colorScheme.secondary,
            title = "觅食习惯",
            description = "拒绝隔夜粮"
        )
        Spacer(modifier = Modifier.height(10.dp))
        HabitCard(
            borderColor = MaterialTheme.colorScheme.tertiary,
            title = "活动规律",
            description = "日落准时退场"
        )
    }
}

@Composable
private fun HabitCard(borderColor: Color, title: String, description: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                drawRect(
                    color = borderColor,
                    size = size.copy(width = 4.dp.toPx())
                )
            }
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CatMemoryPolaroid(profile: CatProfileUi) {
    Column(
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .rotate(2f)
            .fillMaxWidth()
            .background(SurfaceContainerLowest, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = profile.imageRes),
            contentDescription = "记忆照片",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "“${profile.memoryQuote}”",
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = profile.memoryAuthor,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun CatLocationSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.LocationOn, title = "常活动片区与时段规律")

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "基于近30日的延迟远观记录统计",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 54.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        TimeBarChart()

        Spacer(modifier = Modifier.height(20.dp))

        LocationItem("教学区安静片区", 0.65f, "65%", MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        LocationItem("草坪边缘片区", 0.22f, "22%", MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        LocationItem("后勤绿化片区", 0.13f, "13%", MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun TimeBarChart() {
    val times = listOf("清晨", "上午", "中午", "下午", "傍晚", "夜间")
    val heights = listOf(0.3f, 0.5f, 0.8f, 0.6f, 1.0f, 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        times.forEachIndexed { index, time ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height((heights[index] * 80).dp)
                        .background(
                            if (heights[index] >= 0.8f) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun LocationItem(name: String, fraction: Float, percent: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(SurfaceContainerHigh, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
        Text(
            text = percent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CatHealthAdviceSection(profile: CatProfileUi) {
    val errorColor = MaterialTheme.colorScheme.error
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .drawBehind {
                drawRect(
                    color = errorColor,
                    size = size.copy(width = 8.dp.toPx())
                )
            }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "健康与饮食建议",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "严禁喂食",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProhibitedItem("任何含调味料的人类零食")
        Spacer(modifier = Modifier.height(8.dp))
        ProhibitedItem("含巧克力葡萄成分的食物")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "观察指南",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        val dashColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val stroke = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                    drawRoundRect(
                        color = dashColor,
                        style = stroke,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                    )
                }
                .padding(16.dp)
        ) {
            Text(
                text = profile.healthText,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ProhibitedItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Outlined.Block,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun CatTimelineSection(profile: CatProfileUi) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.AutoStories, title = "成长与生命轨迹")

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            Box(
                modifier = Modifier
                    .offset(x = 15.dp)
                    .width(2.dp)
                    .height(420.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TimelineItem(
                    dotColor = MaterialTheme.colorScheme.primary,
                    time = "2小时前 · 被目击",
                    title = "${profile.name}正在安静片区停留",
                    description = "\u201C趴在老位置，尾巴偶尔甩一下，看起来心情不错。\u201D"
                )

                TimelineItem(
                    dotColor = MaterialTheme.colorScheme.secondary,
                    time = "昨天 16:45 · 补水记录",
                    title = "志愿者已更换纯净水",
                    description = "水碗已清洗并更换新鲜纯净水，记录为片区级补水维护。"
                )

                TimelineItemWithImages(
                    dotColor = MaterialTheme.colorScheme.tertiary,
                    time = "3天前 · 档案更新",
                    title = "更新了冬季换毛写真",
                    images = listOf(profile.imageRes, R.drawable.img_net_e7d3e76bea, R.drawable.img_net_8c081179f2)
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    dotColor: Color,
    time: String,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(dotColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(dotColor, CircleShape)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun TimelineItemWithImages(
    dotColor: Color,
    time: String,
    title: String,
    images: List<Int>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(dotColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(dotColor, CircleShape)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                images.forEach { imgRes ->
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun CatMyCompanionRecordsSection(records: List<CompanionRecord>, profile: CatProfileUi) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.History, title = "我的长期陪伴记录")
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (records.isNotEmpty()) {
                    records.take(3).forEach { record ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                Text(if (record.action == "补水") "💧" else "🐾", fontSize = 16.sp)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${record.action}安全记录", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(record.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(record.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    return@Column
                }
                // Record 1
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text("💧", fontSize = 16.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("为${profile.name}补水", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("10月24日", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${profile.name}今天状态稳定，补水后继续保持远观。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                // Record 2
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text("📷", fontSize = 16.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("记录了观察日志", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("10月20日", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("发现${profile.name}在校园安静片区休息，只记录片区，不靠近打扰。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CatPersonalityTheater(profile: CatProfileUi) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(SurfaceContainerHighest, RoundedCornerShape(32.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.TheaterComedy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "个性剧场",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        TheaterStoryCard(
            title = "第一幕：傲娇的早安",
            description = profile.theaterFirst
        )

        Spacer(modifier = Modifier.height(12.dp))

        TheaterStoryCard(
            title = "第二幕：画室的不速之客",
            description = profile.theaterSecond
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(272.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(264.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        CircleShape
                    )
            )
            Image(
                painter = painterResource(id = profile.imageRes),
                contentDescription = "${profile.name}个性照",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(256.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TheaterStoryCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}
