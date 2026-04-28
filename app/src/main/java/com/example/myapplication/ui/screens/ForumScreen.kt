package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*

@Composable
fun ForumScreen() {
    val scrollState = rememberScrollState()
    var showPostDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(96.dp))
            SchoolSwitcherRow()
            Spacer(modifier = Modifier.height(24.dp))
            CategoryChipsRow()
            Spacer(modifier = Modifier.height(24.dp))

            // Team Event Card
            TeamEventCard()
            Spacer(modifier = Modifier.height(16.dp))

            // Knowledge Share Card
            KnowledgeShareCard()
            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Card
            EmergencyForumCard()
            Spacer(modifier = Modifier.height(16.dp))

            // Sighting Card
            SightingForumCard()
            Spacer(modifier = Modifier.height(16.dp))

            // Diary Polaroid Card (slightly tilted aside from grid)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiaryPolaroidCard(modifier = Modifier.weight(1f))
                MapPostCard(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
        ForumTopAppBar()

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 120.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { showPostDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Post", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
        }
        if (showPostDialog) {
            AlertDialog(
                onDismissRequest = { showPostDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPostDialog = false }) {
                        Text("知道了")
                    }
                },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                title = { Text("模拟发布") },
                text = { Text("前端演示阶段：这里将用于发布目击记录、知识分享或组队活动。") }
            )
        }
    }
}

@Composable
fun ForumTopAppBar() {
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
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                Image(
                    painter = painterResource(R.drawable.img_net_b6f8927693),
                    contentDescription = "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text("论坛", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SchoolSwitcherRow() {
    var selected by remember { mutableStateOf(0) }
    val tabs = listOf("本校", "周边学校", "社区内容流")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tabs.indices.toList()) { index ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected == index) MaterialTheme.colorScheme.primary else SurfaceContainerHigh)
                    .clickable { selected = index }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = tabs[index],
                    fontWeight = if (selected == index) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CategoryChipsRow() {
    var selectedLabel by remember { mutableStateOf("目击记录") }
    data class Chip(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)
    val chips = listOf(
        Chip(Icons.Outlined.Search, "目击记录"),
        Chip(Icons.Outlined.Group, "组队活动"),
        Chip(Icons.Outlined.MenuBook, "知识分享"),
        Chip(Icons.Outlined.Info, "求助信息"),
        Chip(Icons.Outlined.Star, "经验分享"),
        Chip(Icons.Outlined.Home, "猫咪日记"),
        Chip(Icons.Outlined.Place, "地图发帖")
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chips) { chip ->
            val selected = selectedLabel == chip.label
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondaryContainer else SurfaceContainerHighest
                    )
                    .clickable { selectedLabel = chip.label }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(chip.icon, contentDescription = chip.label, tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(chip.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EmergencyForumCard() {
    val context = LocalContext.current
    var responded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp, topEnd = 16.dp, bottomStart = 16.dp),
            border = BorderStroke(2.dp, Color(0xFFDE7D70))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("教三教学楼后的「大橘」腿部受伤，急需送医", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 26.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text("15分钟前发布", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))) {
                        Image(painter = painterResource(R.drawable.img_net_ed8f952dc5), contentDescription = "Injured Cat", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text("第三教学楼 · 停车场草坪", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Text("关联：大橘 (Orange)", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("高优先级", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Box(modifier = Modifier.background(SurfaceContainerHigh, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(if (responded) "已响应" else "待响应", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFCBD5E0)).border(2.dp, Color.White, CircleShape))
                        Box(modifier = Modifier.size(28.dp).offset(x = (-8).dp).clip(CircleShape).background(Color(0xFFA0AEC0)).border(2.dp, Color.White, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+12 人正在关注", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            responded = true
                            Toast.makeText(context, "已模拟加入协助队列，请等待志愿者确认", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (responded) "已加入" else "我也能帮忙", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = (-12).dp)
                .background(Color(0xFFDE7D70), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("紧急 · 医疗求助", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun SightingForumCard() {
    val context = LocalContext.current
    var liked by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { Toast.makeText(context, "目击详情页将在前端闭环阶段补充", Toast.LENGTH_SHORT).show() },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceContainerHighest), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("目击：奶牛在操场出没", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("刚刚 · 综合体育场南侧", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp))) {
                Image(painter = painterResource(R.drawable.img_net_9755ae2cc8), contentDescription = "Cat Sighting", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text("奶牛 (Tuxedo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Text("奶牛今天看起来心情不错，在南侧看台晒太阳，有人刚提供了冻干小零食。", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(modifier = Modifier.clickable { liked = !liked }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(if (liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(if (liked) "25" else "24", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.clickable { Toast.makeText(context, "评论面板将在前端闭环阶段补充", Toast.LENGTH_SHORT).show() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Send, contentDescription = "Comment", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text("8", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DiaryPolaroidCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .rotate(-1f)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                Image(painter = painterResource(R.drawable.img_net_3b0696c582), contentDescription = "Diary Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("猫咪日记 · 2023.10.24", fontSize = 9.sp, color = Color.White)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Text("图书馆的「三花」进入冬眠模式", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("今天去借书发现她在三楼社科区睡得很死，大家轻声点哦...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                Text("#宁静校园 #三花", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun MapPostCard(modifier: Modifier = Modifier) {
    var synced by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.clickable { synced = !synced },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Box(modifier = Modifier.background(SurfaceContainerHighest, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("地图动态", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(if (synced) "补给站同步完成" else "更新了 4 处能量补给站位置", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(if (synced) "地图动态已标记为已读，后续可接入真实志愿者数据。" else "根据近期猫咪活动路径，志愿者更新了位于西区礼堂背后的隐蔽补给点，请知悉。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(SurfaceContainerHighest)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(if (synced) 1f else 0.68f).background(MaterialTheme.colorScheme.secondary, CircleShape))
                }
                Text(if (synced) "已同步" else "协作完成 68%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun TeamEventCard() {
    var joined by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("组队：周末自制猫窝换新活动", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("1小时前 · 志愿者协会", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(if (joined) "已报名" else "招募中", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Text("天气渐冷，本周末(10.28)下午1点在学生活动中心集合，利用回收旧衣物制作保暖猫窝，预计需要 5-8 人参与，欢迎报名！", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                    Text("10月28日 13:00", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
                Button(
                    onClick = { joined = !joined },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (joined) "取消报名" else "立即报名", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun KnowledgeShareCard() {
    var collected by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("科普：秋季猫咪易发疾病及预防", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("3小时前 · 知识分享", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("秋季气温变化大，校园流浪猫极易感染猫鼻支等上呼吸道疾病。在此向大家科普如何通过观察猫咪眼鼻分泌物进行初步判断...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("#医疗科普", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                    Text("#秋季护理", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                }
                Row(
                    modifier = Modifier.clickable { collected = !collected },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Star, contentDescription = null, tint = if (collected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    Text(if (collected) "已收藏 35" else "收藏 34", fontSize = 12.sp, color = if (collected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
