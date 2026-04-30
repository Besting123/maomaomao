package com.example.myapplication.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
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
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class CampusResidentCat(
    val name: String,
    val imageRes: Int
)

data class CampusHotspotInfo(
    val name: String,
    val safetyTag: String,
    val areaTitle: String,
    val summary: String,
    val status: String,
    val imageRes: Int,
    val geoPoint: GeoPoint,
    val residents: List<CampusResidentCat>
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
                geoPoint = GeoPoint(39.9518, 116.3433),
                residents = listOf(
                    CampusResidentCat("大白", R.drawable.img_net_e7d3e76bea),
                    CampusResidentCat("橘子", R.drawable.img_net_a53f9ce8f2),
                    CampusResidentCat("奶油", R.drawable.img_net_27ce5092c2)
                )
            ),
            CampusHotspotInfo(
                name = "橘子刚喝过水",
                safetyTag = "补水正常",
                areaTitle = "思源楼北侧补水点",
                summary = "补水点刚维护，适合记录状态",
                status = "补水点充足",
                imageRes = R.drawable.img_net_7f99b46ce0,
                geoPoint = GeoPoint(39.9511, 116.3421),
                residents = listOf(
                    CampusResidentCat("橘子", R.drawable.img_net_a53f9ce8f2),
                    CampusResidentCat("小黑", R.drawable.img_net_8c081179f2)
                )
            ),
            CampusHotspotInfo(
                name = "奶油在树荫休息",
                safetyTag = "请勿打扰",
                areaTitle = "林荫道休息区",
                summary = "猫咪正在休息，建议只做远距离观察",
                status = "不打扰",
                imageRes = R.drawable.img_net_27ce5092c2,
                geoPoint = GeoPoint(39.9505, 116.3430),
                residents = listOf(
                    CampusResidentCat("奶油", R.drawable.img_net_27ce5092c2)
                )
            ),
            CampusHotspotInfo(
                name = "小墨在觅食",
                safetyTag = "可远观",
                areaTitle = "食堂后方灌木丛",
                summary = "傍晚偶尔出现，建议不要靠近食物残渣区",
                status = "远观记录",
                imageRes = R.drawable.img_net_8c081179f2,
                geoPoint = GeoPoint(39.9497, 116.3422),
                residents = listOf(
                    CampusResidentCat("小墨", R.drawable.img_net_8c081179f2)
                )
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
                    minZoomLevel = 16.5
                    maxZoomLevel = 19.0

                    // GCJ-02 坐标（高德地图使用的坐标系）
                    // BJTU 中心点
                    val bjtuCenter = GeoPoint(39.9510, 116.3427)
                    val bjtuBounds = BoundingBox(39.9530, 116.3450, 39.9490, 116.3407)
                    setScrollableAreaLimitDouble(bjtuBounds)
                    controller.setZoom(18.0)
                    controller.setCenter(bjtuCenter)

                    hotspots.forEach { hotspot ->
                        val m = Marker(this)
                        m.position = hotspot.geoPoint
                        m.title = "🐱 ${hotspot.name}"
                        m.snippet = "${hotspot.areaTitle}·${hotspot.safetyTag}"
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        m.setOnMarkerClickListener { _, _ ->
                            selectedHotspot = hotspot
                            sheetExpanded = true
                            true
                        }
                        overlays.add(m)
                    }

                    // 建筑标注（GCJ-02 坐标）
                    listOf(
                        Pair("📚 图书馆", GeoPoint(39.9519, 116.3430)),
                        Pair("🏛 思源楼", GeoPoint(39.9512, 116.3418)),
                        Pair("🏫 逸夫楼", GeoPoint(39.9508, 116.3440)),
                        Pair("🍽 学生食堂", GeoPoint(39.9496, 116.3424)),
                        Pair("🏟 体育馆", GeoPoint(39.9493, 116.3436))
                    ).forEach { (title, pos) ->
                        val m = Marker(this)
                        m.position = pos
                        m.title = title
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        overlays.add(m)
                    }

                    // 延迟到布局完成后再设置中心和缩放
                    post {
                        controller.setZoom(18.0)
                        controller.setCenter(bjtuCenter)
                        invalidate()
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            hotspot.residents.forEach { resident ->
                CatChip(name = resident.name, imageRes = resident.imageRes, onClick = { navController?.navigate("catProfile") })
            }
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
