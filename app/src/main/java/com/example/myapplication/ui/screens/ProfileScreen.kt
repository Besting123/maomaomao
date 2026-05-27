package com.example.myapplication.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.CompanionRecord
import com.example.myapplication.ui.viewmodel.MainViewModel
import com.example.myapplication.ui.viewmodel.RewardExchangeRecord

@Composable
fun ProfileScreen(viewModel: MainViewModel? = null) {
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var activePanel by remember { mutableStateOf<ProfilePanel?>(null) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var exchangeMessage by remember { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            ProfileHeroCard()
            TokenBalanceSection(tokenBalance = uiState?.tokenBalance ?: 350, onExchangeClick = { showExchangeDialog = true })
            GoodwillStatsSection(
                signInDays = uiState?.signInDays ?: 0,
                completedCoursesCount = uiState?.completedCoursesCount ?: 0,
                reportCount = uiState?.publishedForumPosts?.count { it.category == "目击记录" } ?: 0
            )
            KnowledgeBadgesSection(completedCoursesCount = uiState?.completedCoursesCount ?: 0)
            FollowedCatsSection()
            CompanionTimelineSection(records = uiState?.companionRecords.orEmpty())
            ProfileSettingsSection(onOpenPanel = { activePanel = it })
            Spacer(modifier = Modifier.height(120.dp))
        }
        ProfileTopBar(
            onOpenNotifications = { activePanel = ProfilePanel.Notifications }
        )
        activePanel?.let { panel ->
            ProfilePanelDialog(
                panel = panel,
                joinedWeekendShelterEvent = uiState?.joinedWeekendShelterEvent == true,
                exchangeRecords = uiState?.rewardExchangeRecords.orEmpty(),
                onDismiss = { activePanel = null }
            )
        }
        if (showExchangeDialog) {
            RewardExchangeDialog(
                onDismiss = { showExchangeDialog = false },
                onExchange = { title, cost ->
                    val success = viewModel?.exchangeReward(title, cost) ?: false
                    exchangeMessage = if (success) "已兑换「$title」，消耗 $cost 小鱼干。" else "小鱼干不足，先完成学习、签到或陪伴任务。"
                    showExchangeDialog = false
                }
            )
        }
        exchangeMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { exchangeMessage = null },
                title = { Text("兑换结果", fontWeight = FontWeight.Bold) },
                text = { Text(message, lineHeight = 22.sp) },
                confirmButton = { Button(onClick = { exchangeMessage = null }, shape = CircleShape) { Text("知道了") } }
            )
        }
    }
}

enum class ProfilePanel {
    SavedPlaces,
    OfflineEvents,
    GeneralSettings,
    Notifications
}

@Composable
fun ProfileTopBar(
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(40.dp))
        Text("善意账本", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        IconButton(onClick = onOpenNotifications, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ProfileHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 16.dp, bottomEnd = 32.dp, bottomStart = 24.dp)
    ) {
        Row(
            modifier = Modifier.padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(96.dp).clip(CircleShape).border(4.dp, SurfaceContainerLowest, CircleShape)) {
                    Image(
                        painter = painterResource(R.drawable.img_net_a15565a0f2),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("LV.5", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("路过图书馆的小王", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.5).sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("北京交通大学", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("校园观察者", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text("长期记录安全观察、学习与补水提醒，不公开精确点位。", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TokenBalanceSection(tokenBalance: Int, onExchangeClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("我的小鱼干", fontSize = 14.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                Text("通过学习、观察和温和陪伴获得", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.68f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🐟", fontSize = 24.sp)
                    Text("$tokenBalance", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Button(
                onClick = onExchangeClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("兑换奖励", color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RewardExchangeDialog(onDismiss: () -> Unit, onExchange: (String, Int) -> Unit) {
    val rewards = listOf(
        "补水点维护提醒卡" to 80,
        "猫窝材料心愿" to 120,
        "校园照护徽章" to 50
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("兑换奖励", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rewards.forEach { (title, cost) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow)
                            .clickable { onExchange(title, cost) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("用于本次会话内的安全照护与长期陪伴展示", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$cost 🐟", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
fun GoodwillStatsSection(signInDays: Int, completedCoursesCount: Int, reportCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text("长期陪伴账本", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Stat 1
            StatBentoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CheckCircle,
                iconColor = MaterialTheme.colorScheme.tertiary,
                value = signInDays.toString(),
                label = "连续守护天数"
            )
            // Stat 2
            StatBentoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.MenuBook,
                iconColor = MaterialTheme.colorScheme.primary,
                value = completedCoursesCount.toString(),
                label = "安全课程完成"
            )
        }
        // Highlighted stat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Text(reportCount.toString(), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("安全观察报告数", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                Text("只记录片区与状态，保护猫咪活动边界", fontSize = 11.sp, color = Color.White.copy(alpha = 0.78f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun KnowledgeBadgesSection(completedCoursesCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Text("知识勋章", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(icon = "🌱", name = "安全观察员", color = MaterialTheme.colorScheme.primaryContainer, onColor = MaterialTheme.colorScheme.onPrimaryContainer)
            BadgeCard(icon = if (completedCoursesCount >= 3) "💧" else "🔒", name = "补水守护者", color = if (completedCoursesCount >= 3) MaterialTheme.colorScheme.secondaryContainer else SurfaceContainerHigh, onColor = if (completedCoursesCount >= 3) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.outline)
            BadgeCard(icon = if (completedCoursesCount >= 6) "🧭" else "🔒", name = "边界识别", color = SurfaceContainerHigh, onColor = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun BadgeCard(icon: String, name: String, color: Color, onColor: Color) {
    Column(
        modifier = Modifier.background(color, RoundedCornerShape(12.dp)).padding(16.dp).width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 28.sp)
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onColor, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatBentoCard(modifier: Modifier = Modifier, icon: ImageVector, iconColor: Color, value: String, label: String) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(28.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

data class FollowedCat(val name: String, val imageRes: Int, val status: String, val note: String)

@Composable
fun FollowedCatsSection() {
    var selectedCat by remember { mutableStateOf("大橘") }
    val cats = listOf(
        FollowedCat("大橘", R.drawable.img_net_2af44102d5, "稳定远观", "固定片区记录中"),
        FollowedCat("小黑", R.drawable.img_net_5bd5bb21ca, "补水优先", "云陪伴主对象"),
        FollowedCat("奶油", R.drawable.img_net_6cd1d93759, "请勿打扰", "午后多在休息")
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Text("关注的猫咪", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cats) { cat ->
                Card(
                    modifier = Modifier.width(132.dp).clickable { selectedCat = cat.name },
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(64.dp).clip(CircleShape)) {
                            Image(painter = painterResource(cat.imageRes), contentDescription = cat.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(cat.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(cat.note, fontSize = 10.sp, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        if (selectedCat == cat.name) {
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("持续关注中", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }
            item {
                // Add more card
                Card(
                    modifier = Modifier.width(96.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
                        Text("发现更多", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun CompanionTimelineSection(records: List<CompanionRecord>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.List, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Text("长期陪伴轨迹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (records.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                records.take(4).forEach { record ->
                    Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${record.catName} · ${record.action}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(record.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(record.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                            Text("已同步到善意账本 · 仅保留陪伴行为与片区级记录", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            return
        }
        // Timeline items
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Left line
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(16.dp).background(MaterialTheme.colorScheme.secondary, CircleShape).border(4.dp, MaterialTheme.colorScheme.background, CircleShape))
                Box(modifier = Modifier.width(2.dp).height(80.dp).background(MaterialTheme.colorScheme.secondaryContainer))
                Box(modifier = Modifier.size(16.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape).border(4.dp, MaterialTheme.colorScheme.background, CircleShape))
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Item 1
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("10月24日", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary, letterSpacing = 2.sp)
                    Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row {
                                Text("在图书馆北门为 ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text("大橘", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(" 进行了补水。", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            }
                            Text("\u300c大橘今天看起来心情不错，喝了不少水。记录片区状态，不公开精确位置。\u300d", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
                // Item 2
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("10月22日", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary, letterSpacing = 2.sp)
                    Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row {
                                Text("提交了 ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text("奶油", fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                Text(" 的目击记录。", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(8.dp))) {
                                    Image(painter = painterResource(R.drawable.img_net_536f870f7d), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                                Box(modifier = Modifier.weight(1f).height(80.dp).background(SurfaceContainerHigh, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Text("草坪东侧\n活跃", fontSize = 10.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                // Item 3
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("10月21日", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row {
                                Text("完成了学习：", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text("《猫咪行为学入门》", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text("掌握了识别猫咪安定信号的基本知识。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsSection(onOpenPanel: (ProfilePanel) -> Unit) {
    val items = listOf(
        ProfileSettingsItem(Icons.Outlined.Place, MaterialTheme.colorScheme.primary, "收藏的地点", ProfilePanel.SavedPlaces),
        ProfileSettingsItem(Icons.Outlined.DateRange, MaterialTheme.colorScheme.secondary, "线下活动报名", ProfilePanel.OfflineEvents),
        ProfileSettingsItem(Icons.Outlined.Settings, MaterialTheme.colorScheme.tertiary, "通用设置", ProfilePanel.GeneralSettings)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPanel(item.panel) }
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(item.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = item.label, tint = item.color, modifier = Modifier.size(20.dp))
                        }
                        Text(item.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(20.dp))
                }
                if (index < items.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                }
            }
        }
    }
}

data class ProfileSettingsItem(
    val icon: ImageVector,
    val color: Color,
    val label: String,
    val panel: ProfilePanel
)

@Composable
fun ProfilePanelDialog(
    panel: ProfilePanel,
    joinedWeekendShelterEvent: Boolean,
    exchangeRecords: List<RewardExchangeRecord>,
    onDismiss: () -> Unit
) {
    val title = when (panel) {
        ProfilePanel.SavedPlaces -> "收藏的地点"
        ProfilePanel.OfflineEvents -> "线下活动报名"
        ProfilePanel.GeneralSettings -> "通用设置"
        ProfilePanel.Notifications -> "喵伴提醒"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (panel) {
                    ProfilePanel.SavedPlaces -> {
                        ProfileInfoRow(Icons.Outlined.Place, "图书馆北门补水点", "已收藏 · 适合远观，不显示精确坐标")
                        ProfileInfoRow(Icons.Outlined.Place, "综合体育场南侧片区", "已收藏 · 傍晚活跃，避免聚集")
                        ProfileInfoRow(Icons.Outlined.Place, "教三后侧草坪", "医疗求助关注中，仅展示片区")
                    }
                    ProfilePanel.OfflineEvents -> {
                        ProfileInfoRow(Icons.Outlined.DateRange, "周末自制猫窝换新活动", if (joinedWeekendShelterEvent) "已报名 · 10月28日 13:00 · 学生活动中心集合" else "未报名 · 可在论坛组队活动中报名")
                        ProfileInfoRow(Icons.Outlined.Group, "秋季补水点巡查", "报名待确认 · 建议两人同行")
                    }
                    ProfilePanel.GeneralSettings -> {
                        ProfileInfoRow(Icons.Outlined.Notifications, "提醒偏好", "已开启：学习、任务、片区照护和长期陪伴提醒")
                        ProfileInfoRow(Icons.Outlined.Shield, "动物福利保护", "隐藏精确位置 · 禁止追逐围堵提示")
                        ProfileInfoRow(Icons.Outlined.Info, "数据状态", "本次会话内将同步任务、学习、陪伴和社区状态")
                        exchangeRecords.take(2).forEach { record ->
                            ProfileInfoRow(Icons.Outlined.Star, record.title, "${record.time} · 消耗 ${record.cost} 小鱼干")
                        }
                    }
                    ProfilePanel.Notifications -> {
                        ProfileInfoRow(Icons.Outlined.Favorite, "小黑云陪伴", "今天还可以进行一次安静观察互动")
                        ProfileInfoRow(Icons.Outlined.MenuBook, "新手学堂", "完成情绪识别小测可获得 20 小鱼干")
                        ProfileInfoRow(Icons.Outlined.Warning, "片区提醒", "傍晚远观时请优先补水，不要公开精确点位")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = CircleShape) { Text("知道了") }
        }
    )
}

@Composable
fun ProfileInfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
