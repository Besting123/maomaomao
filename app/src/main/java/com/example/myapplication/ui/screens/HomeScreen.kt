package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.myapplication.ui.navigation.BottomNavItem
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.MainViewModel

private enum class HomePanel { Notifications, ActivityList, Story, FollowedCats }

private fun NavController.navigateFromHome(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun HomeScreen(navController: NavController? = null, viewModel: MainViewModel? = null) {
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var activePanel by remember { mutableStateOf<HomePanel?>(null) }
    var showCatProfilePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // Top app bar spacing

            // Immersive Welcome Hero
            HeroSection()

            // Scientific Feeding Alert
            FeedingAlertSection()

            // NEW: Daily Tasks and Sign-in Section
            DailyMissionSection(
                navController = navController,
                signInDays = uiState?.signInDays ?: 3,
                hasSignedInToday = uiState?.hasSignedInToday ?: false,
                completedCount = uiState?.tasks?.count { it.isCompleted } ?: 0,
                totalCount = uiState?.tasks?.size ?: 0
            )

            // Core Functions Bento
            CoreFunctionsBento(navController, onOpenCatProfilePicker = { showCatProfilePicker = true })

            // Today's Activity
            TodaysActivitySection(onOpenAll = { activePanel = HomePanel.ActivityList })

            // NEW: Followed Cats Section
            Spacer(modifier = Modifier.height(16.dp))
            HomeFollowedCatsSection(onOpenAll = { activePanel = HomePanel.FollowedCats })

            Spacer(modifier = Modifier.height(96.dp)) // space for bottom bar
        }

        // Fixed Top App Bar with Blur Effect (simulated with semi-transparent bg)
        TopAppBarSection(
            tokenBalance = uiState?.tokenBalance ?: 350,
            onOpenProfile = { navController?.navigateFromHome(BottomNavItem.Profile.route) },
            onOpenNotifications = { activePanel = HomePanel.Notifications }
        )
        activePanel?.let { panel ->
            HomePanelDialog(panel = panel, onDismiss = { activePanel = null }, navController = navController)
        }
        if (showCatProfilePicker) {
            HomeCatProfilePickerDialog(
                selectedCatName = uiState?.selectedProfileCatName ?: "大橘",
                onSelect = { catName ->
                    viewModel?.selectProfileCat(catName)
                    showCatProfilePicker = false
                    navController?.navigate("catProfile")
                },
                onDismiss = { showCatProfilePicker = false }
            )
        }
    }
}

@Composable
fun TopAppBarSection(tokenBalance: Int, onOpenProfile: () -> Unit, onOpenNotifications: () -> Unit) {
    val animatedToken by animateIntAsState(
        targetValue = tokenBalance,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "token_anim"
    )
    // Scale bounce when token changes
    var triggerBounce by remember { mutableStateOf(0) }
    LaunchedEffect(tokenBalance) { triggerBounce++ }
    val bounceScale by animateFloatAsState(
        targetValue = if (triggerBounce > 0) 1f else 1f,
        animationSpec = keyframes {
            durationMillis = 400
            1.25f at 100 using FastOutSlowInEasing
            0.95f at 200 using FastOutSlowInEasing
            1f at 400 using FastOutSlowInEasing
        },
        label = "bounce"
    )

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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .clickable { onOpenProfile() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.myapplication.R.drawable.img_net_9a89893c4e),
                    contentDescription = "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = "早安，喵伴守护者",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = bounceScale
                        scaleY = bounceScale
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🐟", fontSize = 14.sp)
                    Text(animatedToken.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            IconButton(
                onClick = onOpenNotifications,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.myapplication.R.drawable.img_net_d67284ea8d),
            contentDescription = "Hero Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                        startY = 100f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "安全优先 · 先远观后互动",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "早安，教学区片区的猫咪开始活跃了",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "先看片区状态与补水提醒，再决定是否记录或互动。",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun FeedingAlertSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Outlined.Info, contentDescription = "Water", tint = MaterialTheme.colorScheme.tertiary)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "今日安全提醒",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "补水优先于加餐，保持距离观察，不追逐、不围堵。",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun TodaysActivitySection(onOpenAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日猫咪动态",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "查看全部",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenAll() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        val rowScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rowScrollState)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActivityCard(
                name = "奶油",
                location = "区域观察",
                desc = "“今天在教学区片区被远距离观察到，状态稳定。”",
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                tagIcon = Icons.Outlined.LocationOn,
                tagBg = SurfaceContainerHighest,
                tagColor = MaterialTheme.colorScheme.onSurfaceVariant,
                imageResId = com.example.myapplication.R.drawable.img_net_27ce5092c2
            )
            ActivityCard(
                name = "小黑",
                location = "补水正常",
                desc = "已连续两天保持正常的补水习惯，身体状况优良。",
                borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                tagIcon = Icons.Outlined.CheckCircle,
                tagBg = MaterialTheme.colorScheme.secondaryContainer,
                tagColor = MaterialTheme.colorScheme.onSecondaryContainer,
                imageResId = com.example.myapplication.R.drawable.img_net_e10e6b9fb1
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun ActivityCard(
    name: String, location: String, desc: String, borderColor: Color, 
    tagIcon: androidx.compose.ui.graphics.vector.ImageVector, tagBg: Color, tagColor: Color, imageResId: Int
) {
    Card(
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(borderColor))
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray)) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = imageResId),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.clip(CircleShape).background(tagBg).padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(tagIcon, contentDescription = null, tint = tagColor, modifier = Modifier.size(12.dp))
                        Text(text = location, fontSize = 11.sp, color = tagColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = desc,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CoreFunctionsBento(navController: NavController? = null, onOpenCatProfilePicker: () -> Unit = {}) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { navController?.navigateFromHome(BottomNavItem.Campus.route) },
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("先看校园片区", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("非精确点位 · 先看安全建议", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = "Map", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FunctionSquareCard(
                modifier = Modifier.weight(1f),
                title = "云陪伴",
                subtitle = "先观察再互动",
                icon = Icons.Outlined.FavoriteBorder,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                cardBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                onClick = { navController?.navigateFromHome(BottomNavItem.Companion.route) }
            )
            FunctionSquareCard(
                modifier = Modifier.weight(1f),
                title = "新手学堂",
                subtitle = "科学互动指南",
                icon = Icons.Outlined.School,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                cardBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                onClick = { navController?.navigate("education") }
            )
            FunctionSquareCard(
                modifier = Modifier.weight(1f),
                title = "猫咪档案",
                subtitle = "长期记录与边界",
                icon = Icons.Outlined.Pets,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                cardBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                onClick = onOpenCatProfilePicker
            )
        }
    }
}

@Composable
private fun HomeCatProfilePickerDialog(selectedCatName: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val cats = listOf(
        "大橘" to "慢热型橘猫 · 适合稳定远观",
        "小黑" to "云陪伴主角 · 适合观察和补水",
        "奶油" to "教学区常见 · 状态稳定"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择猫咪档案", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cats.forEach { (name, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (name == selectedCatName) MaterialTheme.colorScheme.primaryContainer else SurfaceContainerLow)
                            .clickable { onSelect(name) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(if (name == selectedCatName) "$name · 当前" else name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(desc, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
fun FunctionSquareCard(
    modifier: Modifier, title: String, subtitle: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconTint: Color, cardBg: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.aspectRatio(1f).then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = iconTint)
            }
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun StoryCardSection(onOpenStory: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
        Text(
            text = "故事集",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().rotate(1f).clickable { onOpenStory() },
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(4.dp))
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.myapplication.R.drawable.img_net_ec43d2eca7),
                        contentDescription = "Story Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("MEMBER STORY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.height(1.dp).weight(1f).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("“橘子”的一天：从操场到实验楼", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
        text = "作为学校里最出名的“外交官”，橘子的行程总是排得很满。早晨常在开阔区域远观人群，中午会出现在安静片区附近...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpenStory() }) {
                    Text("阅读全文", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomePanelDialog(panel: HomePanel, onDismiss: () -> Unit, navController: NavController?) {
    val title = when (panel) {
        HomePanel.Notifications -> "今日提醒"
        HomePanel.ActivityList -> "全部猫咪动态"
        HomePanel.Story -> "故事详情"
        HomePanel.FollowedCats -> "关注对象示例"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (panel) {
                    HomePanel.Notifications -> {
                        HomeInfoRow(Icons.Outlined.Notifications, "傍晚远观提醒", "18:00 后适合观察猫咪活动状态，保持距离并记录片区即可。")
                        HomeInfoRow(Icons.Outlined.Info, "补水优先", "今日天气偏干，优先检查清水，不建议频繁零食投喂。")
                        HomeInfoRow(Icons.Outlined.School, "学习任务", "完成情绪识别课程，可获得小鱼干并减少不当互动。")
                    }
                    HomePanel.ActivityList -> {
                        HomeInfoRow(Icons.Outlined.LocationOn, "奶油 · 教学区附近", "远距离观察到，状态稳定，无需靠近。")
                        HomeInfoRow(Icons.Outlined.CheckCircle, "小黑 · 补水正常", "连续两天补水点状态良好，建议继续巡查。")
                        HomeInfoRow(Icons.Outlined.Pets, "三花 · 图书馆片区", "午后常在安静角落休息，请降低音量。")
                    }
                    HomePanel.Story -> {
                        Text("橘子常在开阔区域远观人群，中午会绕到安静片区休息。志愿者记录显示，它更接受固定人员的远距离陪伴，不适合突然靠近或多人围观。", fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HomeInfoRow(Icons.Outlined.FavoriteBorder, "照护建议", "先观察耳朵和尾巴，保持两米以上距离，不主动伸手。")
                    }
                    HomePanel.FollowedCats -> {
                        HomeInfoRow(Icons.Outlined.Pets, "橘子", "慢热型橘猫，适合稳定远观，不适合突然靠近。")
                        HomeInfoRow(Icons.Outlined.Pets, "小黑", "云陪伴主角，适合用观察和补水作为长期陪伴对象。")
                        HomeInfoRow(Icons.Outlined.Pets, "奶油", "教学区附近远观记录较多，状态稳定，继续保持距离。")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val route = when (panel) {
                        HomePanel.Notifications -> "tasks"
                        HomePanel.ActivityList -> "campus"
                        HomePanel.Story -> "catProfile"
                        HomePanel.FollowedCats -> "catProfile"
                    }
                    onDismiss()
                    navController?.navigate(route)
                },
                shape = CircleShape
            ) {
                Text(
                    when (panel) {
                        HomePanel.Notifications -> "去任务中心"
                        HomePanel.ActivityList -> "打开地图"
                        HomePanel.Story -> "查看档案"
                        HomePanel.FollowedCats -> "查看档案"
                    }
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun HomeInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DailyMissionSection(
    navController: NavController? = null,
    signInDays: Int = 3,
    hasSignedInToday: Boolean = false,
    completedCount: Int = 0,
    totalCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f).height(80.dp).clickable { navController?.navigate("tasks") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("每日签到", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("连续 $signInDays 天", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                Icon(
                    if (hasSignedInToday) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f).height(80.dp).clickable { navController?.navigate("tasks") },
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("今日任务", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("完成 $completedCount/$totalCount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun HomeFollowedCatsSection(onOpenAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我关注的猫咪",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text("管理", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onOpenAll() })
        }
        Spacer(modifier = Modifier.height(16.dp))

        val rowScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rowScrollState)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FollowedCatAvatar("橘子", com.example.myapplication.R.drawable.img_net_ec43d2eca7, onClick = onOpenAll)
            FollowedCatAvatar("小黑", com.example.myapplication.R.drawable.img_net_c9e15cf0b7, onClick = onOpenAll)
            FollowedCatAvatar("奶油", com.example.myapplication.R.drawable.img_net_27ce5092c2, onClick = onOpenAll)
            
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(SurfaceContainerHigh).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape).clickable { onOpenAll() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FollowedCatAvatar(name: String, imageResId: Int, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = imageResId),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(2.dp).clip(CircleShape)
            )
        }
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
