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
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class CampusHotspotInfo(
    val name: String,
    val safetyTag: String,
    val areaTitle: String,
    val summary: String,
    val status: String,
    val imageRes: Int,
    val offsetX: Float,
    val offsetY: Float
)

@Composable
fun CampusScreen(navController: NavController? = null) {
    var selectedTime by remember { mutableStateOf("清晨") }
    val hotspots = remember {
        listOf(
            CampusHotspotInfo(
                name = "大白在这儿",
                safetyTag = "适合远观",
                areaTitle = "图书馆东侧草坪",
                summary = "此区域通常有 3 只猫咪出没",
                status = "远观优先",
                imageRes = R.drawable.img_net_cf9a4fdf2a,
                offsetX = 0.28f,
                offsetY = 0.35f
            ),
            CampusHotspotInfo(
                name = "橘子刚喝过水",
                safetyTag = "补水正常",
                areaTitle = "思源楼北侧补水点",
                summary = "补水点刚维护，适合记录状态",
                status = "补水点充足",
                imageRes = R.drawable.img_net_7f99b46ce0,
                offsetX = 0.65f,
                offsetY = 0.42f
            ),
            CampusHotspotInfo(
                name = "奶油在树荫休息",
                safetyTag = "请勿打扰",
                areaTitle = "林荫道休息区",
                summary = "猫咪正在休息，建议只做远距离观察",
                status = "不打扰",
                imageRes = R.drawable.img_net_27ce5092c2,
                offsetX = 0.48f,
                offsetY = 0.62f
            )
        )
    }
    var selectedHotspot by remember { mutableStateOf(hotspots.first()) }
    var sheetExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLow)
    ) {
        // ══ 真实校园地图 (osmdroid + 高德瓦片) ══
        val context = androidx.compose.ui.platform.LocalContext.current
        DisposableEffect(Unit) {
            Configuration.getInstance().userAgentValue = context.packageName
            onDispose { }
        }
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // 使用高德地图瓦片（国内极速）
                    val amapSource = object : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
                        "Amap", 3, 19, 256, ".png",
                        arrayOf("https://webrd01.is.autonavi.com", "https://webrd02.is.autonavi.com", "https://webrd03.is.autonavi.com", "https://webrd04.is.autonavi.com")
                    ) {
                        override fun getTileURLString(pMapTileIndex: Long): String {
                            val z = org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex)
                            val x = org.osmdroid.util.MapTileIndex.getX(pMapTileIndex)
                            val y = org.osmdroid.util.MapTileIndex.getY(pMapTileIndex)
                            return "https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=$x&y=$y&z=$z"
                        }
                    }
                    setTileSource(amapSource)
                    setMultiTouchControls(true)
                    isHorizontalMapRepetitionEnabled = false
                    isVerticalMapRepetitionEnabled = false
                    minZoomLevel = 15.0
                    maxZoomLevel = 19.0

                    // GCJ-02 坐标（高德地图使用的坐标系）
                    // BJTU 中心点
                    val bjtuCenter = GeoPoint(39.9562, 116.3555)

                    // 猫咪热点标记（GCJ-02 坐标）
                    val catData = listOf(
                        Triple("🐱 大白在这儿", "图书馆东侧草坪·适合远观", GeoPoint(39.9577, 116.3557)),
                        Triple("🐈 橘子刚喝过水", "思源楼北侧补水点·补水正常", GeoPoint(39.9560, 116.3575)),
                        Triple("😺 奶油在树荫休息", "林荫道休息区·请勿打扰", GeoPoint(39.9550, 116.3543)),
                        Triple("🐈‍⬛ 小墨在觅食", "食堂后方灌木丛·可远观", GeoPoint(39.9543, 116.3545))
                    )
                    catData.forEach { (title, snippet, pos) ->
                        val m = Marker(this)
                        m.position = pos
                        m.title = title
                        m.snippet = snippet
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        overlays.add(m)
                    }

                    // 建筑标注（GCJ-02 坐标）
                    listOf(
                        Pair("📚 图书馆", GeoPoint(39.9580, 116.3545)),
                        Pair("🏛 思源楼", GeoPoint(39.9555, 116.3575)),
                        Pair("🏫 逸夫楼", GeoPoint(39.9565, 116.3525)),
                        Pair("🍽 学生食堂", GeoPoint(39.9545, 116.3535)),
                        Pair("🏟 体育馆", GeoPoint(39.9535, 116.3565))
                    ).forEach { (title, pos) ->
                        val m = Marker(this)
                        m.position = pos
                        m.title = title
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        overlays.add(m)
                    }

                    // 关怀路径
                    val route = Polyline()
                    route.addPoint(GeoPoint(39.9580, 116.3545))
                    route.addPoint(GeoPoint(39.9577, 116.3557))
                    route.addPoint(GeoPoint(39.9560, 116.3575))
                    route.addPoint(GeoPoint(39.9550, 116.3543))
                    route.addPoint(GeoPoint(39.9543, 116.3545))
                    route.outlinePaint.color = android.graphics.Color.parseColor("#8B5928")
                    route.outlinePaint.strokeWidth = 5f
                    route.outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(20f, 15f), 0f)
                    overlays.add(route)

                    // 延迟到布局完成后再设置中心和缩放
                    post {
                        controller.setZoom(17.0)
                        controller.setCenter(bjtuCenter)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 顶部浮层 — 半透明
        CampusTopAppBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 90.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeSelectorOverlay(selectedTime = selectedTime, onTimeSelected = { selectedTime = it })
            RouteBadgeOverlay()
        }

        // 底部卡片 — 可折叠，默认只显示摘要行
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp, start = 12.dp, end = 12.dp)
        ) {
            if (sheetExpanded) {
                CampusBottomSheet(
                    navController = navController,
                    selectedTime = selectedTime,
                    hotspot = selectedHotspot,
                    onCollapse = { sheetExpanded = false }
                )
            } else {
                // 折叠态：只显示一行摘要
                CampusBottomSheetCollapsed(
                    hotspot = selectedHotspot,
                    onClick = { sheetExpanded = true }
                )
            }
        }
    }
}

// ── 折叠态底部卡片 ──
@Composable
fun CampusBottomSheetCollapsed(hotspot: CampusHotspotInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(
                painter = painterResource(id = hotspot.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape)
            )
            Column {
                Text(hotspot.areaTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(hotspot.summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Outlined.KeyboardArrowUp, "展开", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MapCanvasLayer(selectedTime: String) {
    val greenColor = Color(0xFFC9EBCA).copy(alpha = 0.4f)
    val blueColor = Color(0xFFC4EFFD).copy(alpha = 0.4f)
    val dottedLineColor = Color(0xFF81817A).copy(alpha = 0.4f)
    val routeLineColor = when (selectedTime) {
        "午后" -> Color(0xFF416A76)
        "傍晚" -> Color(0xFF8B5928)
        "夜间" -> Color(0xFF4D6C51)
        else -> Color(0xFF8B5928)
    }.copy(alpha = 0.68f)

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

        drawCircle(color = Color(0xFFFFE8CC).copy(alpha = 0.38f), radius = w * 0.18f, center = Offset(w * 0.34f, h * 0.38f))
        drawCircle(color = Color(0xFFC9EBCA).copy(alpha = 0.34f), radius = w * 0.16f, center = Offset(w * 0.65f, h * 0.52f))
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
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(6.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.7f) }),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun CatHotspot(hotspot: CampusHotspotInfo, selected: Boolean, onClick: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val x = maxWidth * hotspot.offsetX
        val y = maxHeight * hotspot.offsetY

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
                .size(if (selected) 56.dp else 48.dp)
                .clickable {
                    showTooltip = !showTooltip
                    onClick()
                },
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
                    .border(
                        width = if (selected) 4.dp else 3.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = hotspot.imageRes),
                    contentDescription = hotspot.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (showTooltip || selected) {
                Column(
                    modifier = Modifier
                        .offset(y = 40.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(hotspot.name, color = Color.White, fontSize = 10.sp)
                    Text(hotspot.safetyTag, color = Color.White.copy(alpha = 0.82f), fontSize = 9.sp)
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
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
                    .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_net_aaf424f0b6),
                    contentDescription = "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text("校园地图", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun TimeSelectorOverlay(selectedTime: String, onTimeSelected: (String) -> Unit) {
    val times = listOf("清晨", "午后", "傍晚", "夜间")
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        times.forEach { time ->
            val selected = time == selectedTime
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onTimeSelected(time) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    time,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
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
fun CampusBottomSheet(navController: NavController? = null, selectedTime: String = "清晨", hotspot: CampusHotspotInfo, onCollapse: () -> Unit = {}) {
    val guideText = when (selectedTime) {
        "清晨" -> "可远距离观察，先看尾巴和耳朵状态，保持 2 米以上距离。"
        "午后" -> "远观记录即可，保持 3 米以上安全社交距离。"
        "傍晚" -> "猫咪较活跃，可慢速靠近，不要突然伸手或围堵。"
        else -> "夜间不建议寻找或打扰猫咪，优先查看历史记录。"
    }
    val activeAdvice = when (selectedTime) {
        "清晨" -> "清晨 7:00-9:00 适合安静观察。"
        "午后" -> "午后多在休息，建议只做远观。"
        "傍晚" -> "傍晚 17:00 后活跃，适合温和互动。"
        else -> "夜间降低打扰，避免使用闪光灯。"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(48.dp, RoundedCornerShape(32.dp), spotColor = Color(0x14383833))
            .background(SurfaceContainerLowest, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        // Handle bar — 点击可折叠
        Box(
            modifier = Modifier.fillMaxWidth().clickable { onCollapse() }.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.KeyboardArrowDown, "收起", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Title
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Text(hotspot.areaTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(hotspot.summary, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(hotspot.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
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
                    Text("$selectedTime 时段请优先观察状态，不追逐、不围堵、不强行喂食。", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primary))
                    Text("接近指南 ($selectedTime)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    Text(guideText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.secondary))
                    Text("推荐互动时间", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    Text(activeAdvice, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
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
