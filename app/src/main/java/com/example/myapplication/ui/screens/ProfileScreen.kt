package com.example.myapplication.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*

@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()
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
            GoodwillStatsSection()
            FollowedCatsSection()
            CompanionTimelineSection()
            ProfileSettingsSection()
            Spacer(modifier = Modifier.height(120.dp))
        }
        ProfileTopBar()
    }
}

@Composable
fun ProfileTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
        }
        Text("我的", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
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
                        Text("华东师范大学", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("校园观察者", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoodwillStatsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text("善意账本", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Stat 1
            StatBentoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Home,
                iconColor = MaterialTheme.colorScheme.tertiary,
                value = "42",
                label = "累计补水次数"
            )
            // Stat 2
            StatBentoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ShoppingCart,
                iconColor = MaterialTheme.colorScheme.primary,
                value = "15",
                label = "投喂记录"
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
                Text("128", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("目击报告数", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
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

data class FollowedCat(val name: String, val imageRes: Int)

@Composable
fun FollowedCatsSection() {
    val cats = listOf(
        FollowedCat("大橘", R.drawable.img_net_2af44102d5),
        FollowedCat("小黑", R.drawable.img_net_5bd5bb21ca),
        FollowedCat("奶油", R.drawable.img_net_6cd1d93759)
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Text("关注的猫咪", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cats) { cat ->
                Card(
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
fun CompanionTimelineSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.List, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Text("陪伴轨迹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                            Text("\u300c大橘今天看起来心情不错，喝了不少水。\u300d", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
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
            }
        }
    }
}

@Composable
fun ProfileSettingsSection() {
    val items = listOf(
        Triple(Icons.Outlined.Place, MaterialTheme.colorScheme.primary, "收藏的地点"),
        Triple(Icons.Outlined.DateRange, MaterialTheme.colorScheme.secondary, "线下活动报名"),
        Triple(Icons.Outlined.Settings, MaterialTheme.colorScheme.tertiary, "通用设置")
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            items.forEachIndexed { index, (icon, color, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {}
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
                        }
                        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
