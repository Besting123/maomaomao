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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController? = null, viewModel: MainViewModel? = null) {
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }

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

            // NEW: Daily Tasks and Sign-in Section
            DailyMissionSection(navController, uiState?.signInDays ?: 3, uiState?.hasSignedInToday ?: false)

            // Scientific Feeding Alert
            FeedingAlertSection()

            // Today's Activity
            TodaysActivitySection()

            // NEW: Followed Cats Section
            Spacer(modifier = Modifier.height(16.dp))
            HomeFollowedCatsSection()

            // Core Functions Bento
            CoreFunctionsBento(navController)

            // Story Card
            StoryCardSection()
            
            Spacer(modifier = Modifier.height(120.dp)) // space for bottom bar
        }

        // Fixed Top App Bar with Blur Effect (simulated with semi-transparent bg)
        TopAppBarSection(uiState?.tokenBalance ?: 350)
    }
}

@Composable
fun TopAppBarSection(tokenBalance: Int) {
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
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
                onClick = { },
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
            .aspectRatio(4f / 3f)
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
            Text(
                text = "早安，思源楼附近的猫咪开始活跃了",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 30.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "今天空气清新，是个探访猫咪的好天气",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
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
        Text(
            text = "补水优先于加餐，当前环境更建议补充洁净饮水。",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun TodaysActivitySection() {
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
                fontWeight = FontWeight.Bold
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
                location = "图书馆南侧",
                desc = "“今天在图书馆南侧出现，看起来心情不错”",
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
fun CoreFunctionsBento(navController: NavController? = null) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("校园猫咪地图", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("实时掌握它们都在哪里", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                subtitle = "互动与关怀",
                icon = Icons.Outlined.FavoriteBorder,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                cardBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                onClick = { navController?.navigate("companion") }
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
                subtitle = "云养猫咪全集",
                icon = Icons.Outlined.Pets,
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                cardBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                onClick = { navController?.navigate("catProfile") }
            )
        }
    }
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
fun StoryCardSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
        Text(
            text = "故事集",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().rotate(1f),
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
                    text = "作为学校里最出名的“外交官”，橘子的行程总是排得很满。早晨在操场围观晨跑，中午准时出现在食堂二楼露台...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
fun DailyMissionSection(navController: NavController? = null, signInDays: Int = 3, hasSignedInToday: Boolean = false) {
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
                    Text("完成 1/3", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun HomeFollowedCatsSection() {
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
            FollowedCatAvatar("橘子", com.example.myapplication.R.drawable.img_net_ec43d2eca7)
            FollowedCatAvatar("小黑", com.example.myapplication.R.drawable.img_net_c9e15cf0b7)
            FollowedCatAvatar("奶油", com.example.myapplication.R.drawable.img_net_27ce5092c2)
            
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(SurfaceContainerHigh).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FollowedCatAvatar(name: String, imageResId: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
