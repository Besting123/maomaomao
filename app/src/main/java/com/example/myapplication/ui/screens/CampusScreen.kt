package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import androidx.navigation.NavController

@Composable
fun CampusScreen(navController: NavController? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLow)
    ) {
        // Map Texture Background
        Image(
            painter = painterResource(id = R.drawable.img_31854930),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f,
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.5f), blendMode = BlendMode.Multiply)
        )

        // SVG Map Canvas Replacement
        MapCanvasLayer()

        // Buildings
        Box(modifier = Modifier.fillMaxSize()) {
            BuildingCard(
                name = "图书馆",
                imageRes = R.drawable.img_net_e706152ff1,
                rotation = -2f,
                offsetX = 0.15f,
                offsetY = 0.22f
            )
            BuildingCard(
                name = "思源楼",
                imageRes = R.drawable.img_net_c8ec049afe,
                rotation = 1f,
                offsetX = 0.7f,
                offsetY = 0.55f
            )

            // Cat Hotspots
            CatHotspot(
                name = "大白在这儿",
                imageRes = R.drawable.img_net_cf9a4fdf2a,
                offsetX = 0.28f,
                offsetY = 0.35f
            )
            CatHotspot(
                name = "橘子刚喝过水",
                imageRes = R.drawable.img_net_7f99b46ce0,
                offsetX = 0.65f,
                offsetY = 0.42f
            )
        }

        // Overlays
        CampusTopAppBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeSelectorOverlay()
            RouteBadgeOverlay()
        }

        // Bottom Sheet (Location Drawer)
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)) {
CampusBottomSheet(navController)
        }
    }
}

@Composable
fun MapCanvasLayer() {
    val greenColor = Color(0xFFC9EBCA).copy(alpha = 0.4f)
    val blueColor = Color(0xFFC4EFFD).copy(alpha = 0.4f)
    val dottedLineColor = Color(0xFF81817A).copy(alpha = 0.4f)
    val routeLineColor = Color(0xFF8B5928).copy(alpha = 0.6f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Lake
        val lakePath = Path().apply {
            moveTo(w * 0.7f, h * 0.3f)
            quadraticTo(w * 0.75f, h * 0.25f, w * 0.85f, h * 0.35f)
            quadraticTo(w * 0.95f, h * 0.45f, w * 0.95f, h * 0.55f)
            quadraticTo(w * 0.85f, h * 0.6f, w * 0.75f, h * 0.5f)
            close()
        }
        drawPath(lakePath, color = blueColor)

        // Green Area 1
        val greenPath = Path().apply {
            moveTo(w * 0.1f, h * 0.1f)
            quadraticTo(w * 0.3f, h * 0.05f, w * 0.4f, h * 0.2f)
            quadraticTo(w * 0.6f, h * 0.3f, w * 0.5f, h * 0.5f)
            quadraticTo(w * 0.2f, h * 0.6f, w * 0.1f, h * 0.4f)
            close()
        }
        drawPath(greenPath, color = greenColor)

        // Main Road Dotted
        val roadPath = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticTo(w * 0.25f, h * 0.48f, w * 0.5f, h * 0.5f)
            quadraticTo(w * 0.75f, h * 0.52f, w, h * 0.52f)
        }
        drawPath(roadPath, color = dottedLineColor, style = Stroke(width = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 24f), 0f)))

        // Route Dotted
        val routePath = Path().apply {
            moveTo(w * 0.15f, h * 0.4f)
            quadraticTo(w * 0.3f, h * 0.35f, w * 0.45f, h * 0.48f)
            quadraticTo(w * 0.6f, h * 0.6f, w * 0.8f, h * 0.38f)
        }
        drawPath(routePath, color = routeLineColor, style = Stroke(width = 12f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f), cap = StrokeCap.Round))
    }
}

@Composable
fun BuildingCard(name: String, imageRes: Int, rotation: Float, offsetX: Float, offsetY: Float) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val x = maxWidth * offsetX
        val y = maxHeight * offsetY
        
        Box(
            modifier = Modifier
                .offset(x = x, y = y)
                .rotate(rotation)
                .shadow(2.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(SurfaceContainerLowest, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 8.dp, bottomStart = 8.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.7f) }),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun CatHotspot(name: String, imageRes: Int, offsetX: Float, offsetY: Float) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val x = maxWidth * offsetX
        val y = maxHeight * offsetY

        // Infinite pulsing animation
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_scale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_alpha"
        )

        var showTooltip by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .offset(x = x, y = y)
                .size(48.dp)
                .clickable { showTooltip = !showTooltip },
            contentAlignment = Alignment.Center
        ) {
            // Pulse circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            )
            // Cat avatar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (showTooltip) {
                Box(
                    modifier = Modifier
                        .offset(y = 40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(name, color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun CampusTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
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
                    .background(SurfaceContainerHighest)
                    .border(2.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_net_aaf424f0b6),
                    contentDescription = "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text("校园地图", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TimeSelectorOverlay() {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text("早晨", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text("午后", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun RouteBadgeOverlay() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.Place, contentDescription = "Route", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
        Text("今日关怀路径", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun CampusBottomSheet(navController: NavController? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(48.dp, RoundedCornerShape(32.dp), spotColor = Color(0x14383833))
            .background(SurfaceContainerLowest, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        // Handle bar
        Box(modifier = Modifier.width(48.dp).height(6.dp).clip(RoundedCornerShape(50)).background(SurfaceContainerHighest).align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Text("图书馆东侧草坪", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text("此区域通常有 3 只猫咪出没", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("补水点充足", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Text("更新于 10分钟前", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Approach Guide & Time Advice & DND Alert
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // "Do Not Disturb" Alert
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.DoNotDisturbOn, contentDescription = "Do Not Disturb", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Text("当前时段猫咪多在午休，请勿近距离打扰或强行喂食。", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primary))
                    Text("接近指南 (午后)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    Text("远观记录即可，保持 3 米以上安全社交距离。", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.secondary))
                    Text("推荐互动时间", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    Text("傍晚 17:00 后活跃，适合温和抚摸与梳毛。", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Residents Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "常驻猫咪",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 1.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { navController?.navigate("catProfile") }
            ) {
                Text(
                    "查看全部档案",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Residents
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CatChip(name = "大白", imageRes = R.drawable.img_net_e7d3e76bea, onClick = { navController?.navigate("catProfile") })
            CatChip(name = "橘子", imageRes = R.drawable.img_net_a53f9ce8f2, onClick = { navController?.navigate("catProfile") })
            CatChip(name = "小墨", imageRes = R.drawable.img_net_8c081179f2, onClick = { navController?.navigate("catProfile") })
        }
    }
}

@Composable
fun CatChip(name: String, imageRes: Int, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
            .background(SurfaceContainer, CircleShape)
            .padding(4.dp)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(32.dp).clip(CircleShape)
        )
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
