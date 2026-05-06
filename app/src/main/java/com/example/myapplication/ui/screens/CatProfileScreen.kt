package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.myapplication.ui.components.CatModel3DViewer
import com.example.myapplication.ui.components.CatViewerMode
import com.example.myapplication.ui.theme.SurfaceContainerHigh
import com.example.myapplication.ui.theme.SurfaceContainerHighest
import com.example.myapplication.ui.theme.SurfaceContainerLow
import com.example.myapplication.ui.theme.SurfaceContainerLowest

@Composable
fun CatProfileScreen(onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()

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

            CatProfileHeroSection()
            Spacer(modifier = Modifier.height(24.dp))
            
            CatMoodIndicatorSection()
            Spacer(modifier = Modifier.height(24.dp))

            CatInteractionBoundarySection()
            Spacer(modifier = Modifier.height(24.dp))

            CatPersonalitySection()
            Spacer(modifier = Modifier.height(24.dp))

            CatMemoryPolaroid()
            Spacer(modifier = Modifier.height(24.dp))

            CatLocationSection()
            Spacer(modifier = Modifier.height(24.dp))

            CatHealthAdviceSection()
            Spacer(modifier = Modifier.height(24.dp))

            CatTimelineSection()
            Spacer(modifier = Modifier.height(24.dp))
            
            CatMyCompanionRecordsSection()
            Spacer(modifier = Modifier.height(24.dp))

            CatPersonalityTheater()
            Spacer(modifier = Modifier.height(48.dp))
        }

        CatProfileTopBar(onBackClick = onBackClick)
    }
}

@Composable
private fun CatProfileTopBar(onBackClick: () -> Unit) {
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
            IconButton(onClick = { Toast.makeText(context, "档案分享卡片生成中", Toast.LENGTH_SHORT).show() }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = { Toast.makeText(context, "更多档案功能将在前端演示中补充", Toast.LENGTH_SHORT).show() }) {
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
private fun CatProfileHeroSection() {
    var isFollowed by remember { mutableStateOf(false) }
    val moodLabel = if (isFollowed) "亲近中" else "保持观察"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(480.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        CatModel3DViewer(
            modelAssetPath = "models/mao-lihua-animated.glb",
            label = "3D 陪伴狸花猫模型",
            mode = CatViewerMode.PROFILE,
            animationName = "Idle",
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
                CatTag("橘子", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                CatTag("警惕型", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                CatTag("爱晒太阳", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "大橘",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "编号: SC-2023-009 · 艺术学院常驻嘉宾",
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
                        isFollowed = !isFollowed
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
private fun CatPersonalitySection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.Psychology, title = "个性与习惯")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "大橘是一只典型的\u201C慢热型\u201D橘猫。初次见面时会保持警惕距离，但一旦建立信任，就会展现出极其黏人的一面。它对固定的几位志愿者表现出明显的偏好，会主动蹭腿、翻肚皮。对陌生人则保持礼貌但疏离的态度。",
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
private fun CatMemoryPolaroid() {
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
            painter = painterResource(id = R.drawable.img_net_ec43d2eca7),
            contentDescription = "记忆照片",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "\u201C它在蹭我的画架，仿佛在指导我构图。\u201D",
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "— 2023.11.12 · 某大一新生",
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
private fun CatHealthAdviceSection() {
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
                text = "如发现大橘出现精神萎靡、食欲下降、毛发异常脱落等情况，请联系校园流浪猫志愿者团队。日常照护以固定片区规则为准，不公开精确补给位置。",
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
private fun CatTimelineSection() {
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
                    title = "正在教学区安静片区晒太阳",
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
                    images = listOf(R.drawable.img_net_e7d3e76bea, R.drawable.img_net_a53f9ce8f2, R.drawable.img_net_8c081179f2)
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
private fun CatMyCompanionRecordsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(icon = Icons.Outlined.History, title = "我的陪伴记录")
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Record 1
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text("💧", fontSize = 16.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("为大橘补水", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("10月24日", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("大橘今天看起来心情不错，喝了不少水。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("发现大橘在校园安静片区睡午觉。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CatPersonalityTheater() {
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
            description = "每天早上，大橘常在教学区附近短暂停留。但如果你试图直接摸它，它会优雅地后退两步，用眼神告诉你：\u201C请先保持距离。\u201D"
        )

        Spacer(modifier = Modifier.height(12.dp))

        TheaterStoryCard(
            title = "第二幕：画室的不速之客",
            description = "大橘对画室有着莫名的执着。它会趁门没关严时溜进去，在画架之间巡视一圈，最后选一个最碍事的位置趴下，仿佛在说：\u201C今天的构图，我来把关。\u201D"
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
                painter = painterResource(id = R.drawable.img_net_a53f9ce8f2),
                contentDescription = "大橘个性照",
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
