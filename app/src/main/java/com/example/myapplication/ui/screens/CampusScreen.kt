package com.example.myapplication.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.SurfaceContainerHigh
import com.example.myapplication.ui.theme.SurfaceContainerLow
import com.example.myapplication.ui.theme.SurfaceContainerLowest
import com.example.myapplication.ui.viewmodel.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class CampusCatTimeHabit(
    val catName: String,
    val areaHint: String,
    val habit: String,
    val safety: String,
    val accent: Color
)

data class CampusTimeSegmentInfo(
    val name: String,
    val icon: String,
    val title: String,
    val summary: String,
    val notice: String,
    val cats: List<CampusCatTimeHabit>
)

data class CampusCatMarker(
    val name: String,
    val areaHint: String,
    val habit: String,
    val safety: String,
    val advice: String,
    val emoji: String,
    val markerColor: Color,
    val location: GeoPoint
)

@Composable
fun CampusScreen(
    navController: NavController? = null,
    viewModel: MainViewModel? = null
) {
    var selectedTime by remember { mutableStateOf("清晨") }
    val segments = rememberCampusTimeSegments()
    val selectedSegment = segments.first { it.name == selectedTime }
    val catMarkers = rememberCampusCatMarkers()
    var selectedCat by remember { mutableStateOf<CampusCatMarker?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLow)
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        DisposableEffect(Unit) {
            Configuration.getInstance().userAgentValue = context.packageName
            onDispose { }
        }
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
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
                    setMinZoomLevel(16.0)
                    setMaxZoomLevel(19.5)

                    val bjtuCenter = GeoPoint(39.9510, 116.3427)
                    controller.setZoom(18.0)
                    controller.setCenter(bjtuCenter)

                    catMarkers.forEach { catMarker ->
                        val markerBitmap = createCatPinBitmap(
                            ctx,
                            catMarker.name.take(1),
                            catMarker.markerColor.toArgb()
                        )
                        val osmMarker = Marker(this).apply {
                            position = catMarker.location
                            // anchor bottom-center → pin tip points at the exact GeoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = BitmapDrawable(ctx.resources, markerBitmap)
                            title = catMarker.name
                            // Suppress osmdroid default infobox; we render our own bottom card
                            setInfoWindow(null)
                            setOnMarkerClickListener { _, _ ->
                                selectedCat = catMarker
                                true
                            }
                        }
                        overlays.add(osmMarker)
                    }

                    post {
                        controller.setZoom(18.0)
                        controller.setCenter(bjtuCenter)
                        invalidate()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        CampusTimeEffectOverlay(selectedTime)
        CampusTopAppBar()

        // TopCenter Box — only wraps TimeSelectorOverlay actual height,
        // so map gestures (drag/zoom) and marker clicks below are NOT swallowed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            TimeSelectorOverlay(selectedTime = selectedTime, onTimeSelected = { selectedTime = it })
        }

        // Bottom info card — only visible after marker tap
        AnimatedVisibility(
            visible = selectedCat != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedCat?.let { cat ->
                CampusCatMarkerCard(
                    cat = cat,
                    onClose = { selectedCat = null },
                    onViewProfile = {
                        viewModel?.selectProfileCat(cat.name)
                        selectedCat = null
                        navController?.navigate("catProfile")
                    }
                )
            }
        }
    }
}

@Composable
fun rememberCampusCatMarkers(): List<CampusCatMarker> {
    return remember {
        listOf(
            CampusCatMarker(
                name = "大橘",
                areaHint = "教学区草坪",
                habit = "上午常在草坪边晒太阳，对靠近敏感",
                safety = "保持 2 米以上",
                advice = "远观即可，不要追逐或直接抚摸",
                emoji = "🐱",
                markerColor = Color(0xFFE08A3C),
                location = GeoPoint(39.95198, 116.34403)
            ),
            CampusCatMarker(
                name = "云朵",
                areaHint = "后勤绿化带",
                habit = "傍晚沿绿化带缓慢移动，警惕性较高",
                safety = "不要堵路",
                advice = "侧身蹲低观察，给猫咪留下退路",
                emoji = "🤍",
                markerColor = Color(0xFF7A8DA8),
                location = GeoPoint(39.94962, 116.34168)
            ),
            CampusCatMarker(
                name = "奶油",
                areaHint = "图书馆林荫带",
                habit = "午后多在树荫下休息，对声音敏感",
                safety = "降低音量",
                advice = "经过即可，不要呼唤或打扰睡眠",
                emoji = "🤍",
                markerColor = Color(0xFFB48A66),
                location = GeoPoint(39.95061, 116.34297)
            )
        )
    }
}

/**
 * Classic teardrop "pin" marker: rounded head with a sharp tip pointing down.
 * The tip sits at bitmap (centerX, height-padding) so when paired with
 * Marker.ANCHOR_CENTER/ANCHOR_BOTTOM, it points exactly at the GeoPoint.
 *
 * Geometry: head is a circle at (cx, cyHead) with radius headR. The two tangent
 * lines from the tip (cx, tipY) touch the circle at angles ±θ from vertical
 * (sin θ = headR / tipDist). The path traces the upper arc between the tangent
 * points, then straight lines to the tip — a true tear-drop, not a stack of shapes.
 */
private fun createCatPinBitmap(
    context: Context,
    label: String,
    bgColor: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val pinW = (36 * density).toInt()
    val pinH = (48 * density).toInt()
    val bitmap = Bitmap.createBitmap(pinW, pinH, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)

    val cx = pinW / 2f
    val headR = pinW * 0.42f
    val cyHead = headR + 2f * density
    val tipY = pinH - 2f * density
    val tipDist = tipY - cyHead
    val sinTheta = (headR / tipDist).coerceIn(-1f, 1f)
    val thetaDeg = Math.toDegrees(Math.asin(sinTheta.toDouble())).toFloat()

    val ovalRect = RectF(cx - headR, cyHead - headR, cx + headR, cyHead + headR)

    paint.style = AndroidPaint.Style.FILL
    paint.color = AndroidColor.argb(60, 0, 0, 0)
    paint.maskFilter = BlurMaskFilter(3f * density, BlurMaskFilter.Blur.NORMAL)
    val shadowOval = RectF(
        cx - headR,
        cyHead - headR + 2f * density,
        cx + headR,
        cyHead + headR + 2f * density
    )
    val shadowPath = AndroidPath().apply {
        arcTo(shadowOval, 90f + thetaDeg, 360f - 2f * thetaDeg, true)
        lineTo(cx, tipY + 2f * density)
        close()
    }
    canvas.drawPath(shadowPath, paint)
    paint.maskFilter = null

    paint.color = bgColor
    val pinPath = AndroidPath().apply {
        arcTo(ovalRect, 90f + thetaDeg, 360f - 2f * thetaDeg, true)
        lineTo(cx, tipY)
        close()
    }
    canvas.drawPath(pinPath, paint)

    paint.color = AndroidColor.WHITE
    val innerR = headR * 0.62f
    canvas.drawCircle(cx, cyHead, innerR, paint)

    paint.color = bgColor
    paint.textSize = innerR * 1.15f
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textAlign = AndroidPaint.Align.CENTER
    val baseline = cyHead - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(label, cx, baseline, paint)

    return bitmap
}

@Composable
fun CampusCatMarkerCard(
    cat: CampusCatMarker,
    onClose: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .shadow(20.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(cat.markerColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cat.emoji, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        cat.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = cat.markerColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            cat.areaHint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cat.markerColor
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Habit row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerLow)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Pets,
                    contentDescription = null,
                    tint = cat.markerColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    cat.habit,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Approach advice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "接近指南",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        cat.advice,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Safety + action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DoNotDisturbOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            cat.safety,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onViewProfile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("查看档案", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun rememberCampusTimeSegments(): List<CampusTimeSegmentInfo> {
    val colorScheme = MaterialTheme.colorScheme
    return remember(colorScheme) {
        listOf(
            CampusTimeSegmentInfo(
                name = "清晨",
                icon = "🌤",
                title = "清晨活跃观察",
                summary = "清晨人流较少，胆子大的猫会先出来巡看水点和草坪边缘。",
                notice = "适合远距离记录精神状态，先看尾巴、耳朵和步态。",
                cats = listOf(
                    CampusCatTimeHabit("大白", "教学区草坪边", "会沿草坪边慢走，愿意保持距离被观察。", "保持 2 米以上", colorScheme.primary),
                    CampusCatTimeHabit("橘子", "北侧补水点", "常先检查水点，停留时间短。", "只记录不追随", colorScheme.tertiary)
                )
            ),
            CampusTimeSegmentInfo(
                name = "午后",
                icon = "☀️",
                title = "午后休息避扰",
                summary = "午后多数猫咪进入休息状态，树荫和安静墙角更常见。",
                notice = "午后不建议主动寻找或靠近，避免打扰睡眠。",
                cats = listOf(
                    CampusCatTimeHabit("奶油", "林荫休息带", "午后常蜷在树荫下休息，对声音敏感。", "降低音量", colorScheme.secondary),
                    CampusCatTimeHabit("云朵", "休息区内侧", "通常减少移动，只适合远观确认状态。", "不触碰不投喂", colorScheme.primary)
                )
            ),
            CampusTimeSegmentInfo(
                name = "傍晚",
                icon = "🌇",
                title = "傍晚巡看补水",
                summary = "傍晚活动增加，适合巡看补水和记录不同猫咪的出现节奏。",
                notice = "先补水后互动，避免多人聚集在同一片区。",
                cats = listOf(
                    CampusCatTimeHabit("小墨", "后勤绿化带", "傍晚会沿绿化带缓慢移动，偶尔停留。", "不要堵路", colorScheme.tertiary),
                    CampusCatTimeHabit("橘子", "开放草坪外缘", "傍晚更可能出现，但不喜欢被围观。", "分散观察", colorScheme.primary)
                )
            ),
            CampusTimeSegmentInfo(
                name = "夜间",
                icon = "🌙",
                title = "夜间减少打扰",
                summary = "夜间不鼓励主动寻找猫咪，只查看历史片区习惯和次日提醒。",
                notice = "不要开闪光灯，不发布实时路线，不追踪精确位置。",
                cats = listOf(
                    CampusCatTimeHabit("云朵", "安静通道附近", "夜间可能短暂经过，不建议跟随。", "不追踪", colorScheme.secondary),
                    CampusCatTimeHabit("奶油", "隐蔽休息点", "夜间更依赖安静环境，发现后应离开。", "立即降噪离开", colorScheme.tertiary)
                )
            )
        )
    }
}

@Composable
fun CampusTimeEffectOverlay(selectedTime: String) {
    val colors = when (selectedTime) {
        "清晨" -> listOf(Color(0x22FFDFA8), Color.Transparent, Color(0x18FFF6D5))
        "午后" -> listOf(Color(0x10FFFFFF), Color.Transparent, Color(0x14FFE7A3))
        "傍晚" -> listOf(Color(0x26FF9A62), Color.Transparent, Color(0x22C56A3A))
        else -> listOf(Color(0x66304966), Color(0x33222B45), Color(0x66111A2E))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    )
}

@Composable
fun CampusTopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f))
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
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("校园地图", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("按时间了解猫咪习性", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TimeSelectorOverlay(selectedTime: String, onTimeSelected: (String) -> Unit) {
    val times = listOf("清晨" to "🌤", "午后" to "☀️", "傍晚" to "🌇", "夜间" to "🌙")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.84f), RoundedCornerShape(50))
            .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        times.forEach { (time, icon) ->
            val selected = time == selectedTime
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onTimeSelected(time) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    time,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            }
        }
    }
}

@Composable
fun CampusTimeSummaryCard(segment: CampusTimeSegmentInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(segment.icon, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(segment.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(segment.summary, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(segment.notice, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CampusCatHabitCard(habit: CampusCatTimeHabit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, habit.accent.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(habit.accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Pets, contentDescription = null, tint = habit.accent, modifier = Modifier.size(21.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.catName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(habit.areaHint, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = habit.accent)
                }
                Text(habit.habit, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.background(habit.accent.copy(alpha = 0.1f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(habit.safety, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = habit.accent)
                }
            }
        }
    }
}

@Composable
fun CampusTimeSafetyCard(segment: CampusTimeSegmentInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.58f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.DoNotDisturbOn, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${segment.name}安全边界", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
            Text("只展示时间段习性和片区级倾向，不展示实时点位、路线或精确坐标。", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}
