package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class CatViewerMode {
    COMPANION,
    PROFILE
}

@Composable
fun CatModel3DViewer(
    modifier: Modifier = Modifier,
    modelAssetPath: String = "models/cat.glb",
    label: String = "2.5D 云养小猫",
    isFullScreen: Boolean = false,
    mode: CatViewerMode = if (isFullScreen) CatViewerMode.COMPANION else CatViewerMode.PROFILE,
    onDoubleTap: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var hasInteracted by rememberSaveable(modelAssetPath, mode) { mutableStateOf(false) }
    var tapCount by rememberSaveable(modelAssetPath, mode) { mutableIntStateOf(0) }
    val turn = remember { Animatable(if (mode == CatViewerMode.COMPANION) -10f else 12f) }
    val tapPulse = remember { Animatable(0f) }
    val decaySpec = remember { exponentialDecay<Float>() }
    val transition = rememberInfiniteTransition(label = "cute-pet")
    val breath by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(if (mode == CatViewerMode.COMPANION) 1900 else 2400), RepeatMode.Reverse),
        label = "breath"
    )
    val tail by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "tail"
    )
    val blink by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "blink"
    )

    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            tapPulse.snapTo(1f)
            tapPulse.animateTo(0f, tween(780))
        }
    }

    val draggableState = rememberDraggableState { delta ->
        hasInteracted = true
        coroutineScope.launch { turn.snapTo((turn.value + delta * 0.25f).coerceIn(-42f, 42f)) }
    }
    val containerShape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .clip(containerShape)
            .background(if (mode == CatViewerMode.COMPANION) Color.Transparent else Color(0xFFFFF3E4))
    ) {
        CutePetBackdrop(mode = mode)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        hasInteracted = true
                        tapCount += 1
                        onDoubleTap()
                    })
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            turn.animateDecay(
                                initialVelocity = velocity * 0.018f,
                                animationSpec = decaySpec
                            )
                        }
                    }
                )
        ) {
            drawCutePet(
                mode = mode,
                turn = turn.value,
                breath = breath,
                tail = tail,
                blink = blink,
                tapPulse = tapPulse.value
            )
        }
        CutePetForeground(mode = mode)
        CutePetHint(visible = !hasInteracted, mode = mode)
        if (mode == CatViewerMode.PROFILE) {
            CutePetLabel(label = label)
        }
    }
}

@Composable
private fun BoxScope.CutePetBackdrop(mode: CatViewerMode) {
    val transition = rememberInfiniteTransition(label = "pet-glow")
    val glow by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "glow"
    )
    val colors = if (mode == CatViewerMode.COMPANION) {
        listOf(Color(0x00FFFCF7), Color(0x55FDBC82), Color(0x44C9EBCA))
    } else {
        listOf(Color(0xFFFFE7C7), Color(0xFFFFF6EA), Color(0xFFFFFBF6))
    }
    val accent = if (mode == CatViewerMode.COMPANION) MaterialTheme.colorScheme.primary else Color(0xFFFF9F43)

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(y = if (mode == CatViewerMode.COMPANION) 26.dp else 0.dp)
            .size(if (mode == CatViewerMode.COMPANION) 350.dp else 285.dp)
            .graphicsLayer(scaleX = glow, scaleY = glow)
            .background(
                Brush.radialGradient(listOf(accent.copy(alpha = 0.42f), accent.copy(alpha = 0.12f), Color.Transparent)),
                CircleShape
            )
    )
}

@Composable
private fun BoxScope.CutePetForeground(mode: CatViewerMode) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(if (mode == CatViewerMode.COMPANION) 190.dp else 130.dp)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = if (mode == CatViewerMode.COMPANION) 0.24f else 0.14f))))
    )
}

@Composable
private fun BoxScope.CutePetHint(visible: Boolean, mode: CatViewerMode) {
    AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(tween(450)),
        exit = androidx.compose.animation.fadeOut(tween(320)),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = if (mode == CatViewerMode.COMPANION) 110.dp else 18.dp)
    ) {
        Row(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(50))
                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("↔", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF6D4C41))
            Text(
                text = if (mode == CatViewerMode.COMPANION) "拖动转身 · 双击摸摸" else "拖动看看圆滚滚的小猫",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E)
            )
        }
    }
}

@Composable
private fun BoxScope.CutePetLabel(label: String) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(14.dp)
            .background(Color.White.copy(alpha = 0.9f), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

private fun DrawScope.drawCutePet(
    mode: CatViewerMode,
    turn: Float,
    breath: Float,
    tail: Float,
    blink: Float,
    tapPulse: Float
) {
    val minSize = min(size.width, size.height)
    val scale = if (mode == CatViewerMode.COMPANION) 1.05f else 0.92f
    val base = minSize * scale * (1f + tapPulse * 0.045f)
    val radians = (turn / 180f) * PI.toFloat()
    val faceShift = sin(radians) * base * 0.035f
    val bodyShift = sin(radians) * base * 0.018f
    val bob = (breath - 1f) * base * 0.035f - tapPulse * base * 0.018f
    val centerX = size.width * 0.5f
    val bodyCenter = Offset(centerX + bodyShift, size.height * if (mode == CatViewerMode.COMPANION) 0.65f else 0.62f + bob)
    val headCenter = Offset(centerX + faceShift, size.height * if (mode == CatViewerMode.COMPANION) 0.43f else 0.42f + bob)
    val bodySize = Size(base * 0.48f, base * 0.34f * breath)
    val headSize = Size(base * 0.55f, base * 0.49f * breath)
    val isProfile = mode == CatViewerMode.PROFILE
    val fur = if (isProfile) Color(0xFFE98A3A) else Color(0xFF303247)
    val furLight = if (isProfile) Color(0xFFFFBE72) else Color(0xFF6E7396)
    val furDark = if (isProfile) Color(0xFF93471F) else Color(0xFF141724)
    val belly = if (isProfile) Color(0xFFFFD9A8) else Color(0xFFF2E2D3)
    val eye = if (isProfile) Color(0xFF2A1B14) else Color(0xFFFFCF5D)
    val cheek = Color(0xFFFF8FA3)

    drawOval(
        brush = Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.36f), Color.Transparent)),
        topLeft = Offset(centerX - base * 0.34f, bodyCenter.y + base * 0.16f),
        size = Size(base * 0.68f, base * 0.13f)
    )

    drawTail(center = bodyCenter, base = base, fur = fur, furDark = furDark, tail = tail, turn = turn)
    drawOval(
        brush = Brush.radialGradient(listOf(furLight, fur, furDark), center = Offset(bodyCenter.x - base * 0.09f, bodyCenter.y - base * 0.11f), radius = base * 0.42f),
        topLeft = Offset(bodyCenter.x - bodySize.width / 2f, bodyCenter.y - bodySize.height / 2f),
        size = bodySize
    )
    drawBodyDetails(bodyCenter = bodyCenter, base = base, furDark = furDark, furLight = furLight, isProfile = isProfile)
    drawOval(
        color = belly.copy(alpha = 0.72f),
        topLeft = Offset(bodyCenter.x - base * 0.13f, bodyCenter.y - base * 0.09f),
        size = Size(base * 0.26f, base * 0.22f)
    )
    drawPaws(bodyCenter = bodyCenter, base = base, furLight = furLight, furDark = furDark)
    drawEars(headCenter = headCenter, base = base, fur = fur, furLight = furLight, furDark = furDark)
    drawOval(
        brush = Brush.radialGradient(listOf(furLight, fur, furDark), center = Offset(headCenter.x - base * 0.13f, headCenter.y - base * 0.14f), radius = base * 0.42f),
        topLeft = Offset(headCenter.x - headSize.width / 2f, headCenter.y - headSize.height / 2f),
        size = headSize
    )
    drawHeadDetails(headCenter = headCenter, base = base, furDark = furDark, belly = belly, isProfile = isProfile, turn = turn)
    drawFace(headCenter = headCenter, base = base, eye = eye, cheek = cheek, blink = blink, turn = turn, isProfile = isProfile)
    drawCollar(headCenter = headCenter, base = base, isProfile = isProfile)
    drawSparkles(headCenter = headCenter, base = base, tapPulse = tapPulse)
}

private fun DrawScope.drawTail(center: Offset, base: Float, fur: Color, furDark: Color, tail: Float, turn: Float) {
    val side = if (turn >= 0f) -1f else 1f
    val tailPath = Path().apply {
        moveTo(center.x + side * base * 0.19f, center.y + base * 0.02f)
        cubicTo(
            center.x + side * base * (0.42f + tail * 0.015f), center.y - base * 0.13f,
            center.x + side * base * (0.42f - tail * 0.02f), center.y - base * 0.38f,
            center.x + side * base * 0.2f, center.y - base * 0.31f
        )
    }
    drawPath(tailPath, color = furDark.copy(alpha = 0.34f), style = Stroke(width = base * 0.115f, cap = StrokeCap.Round))
    drawPath(tailPath, color = fur, style = Stroke(width = base * 0.09f, cap = StrokeCap.Round))
    drawPath(tailPath, color = Color.White.copy(alpha = 0.16f), style = Stroke(width = base * 0.027f, cap = StrokeCap.Round))
}

private fun DrawScope.drawBodyDetails(bodyCenter: Offset, base: Float, furDark: Color, furLight: Color, isProfile: Boolean) {
    drawOval(
        color = Color.White.copy(alpha = if (isProfile) 0.12f else 0.18f),
        topLeft = Offset(bodyCenter.x - base * 0.17f, bodyCenter.y - base * 0.13f),
        size = Size(base * 0.18f, base * 0.11f)
    )
    drawOval(
        color = furDark.copy(alpha = 0.18f),
        topLeft = Offset(bodyCenter.x + base * 0.08f, bodyCenter.y - base * 0.11f),
        size = Size(base * 0.14f, base * 0.2f)
    )

    if (isProfile) {
        listOf(-0.16f, -0.05f, 0.08f).forEachIndexed { index, xOffset ->
            val stripe = Path().apply {
                moveTo(bodyCenter.x + base * xOffset, bodyCenter.y - base * 0.13f)
                quadraticTo(
                    bodyCenter.x + base * (xOffset + 0.025f), bodyCenter.y - base * 0.02f,
                    bodyCenter.x + base * (xOffset - 0.025f), bodyCenter.y + base * 0.07f
                )
            }
            drawPath(
                stripe,
                color = furDark.copy(alpha = 0.24f - index * 0.03f),
                style = Stroke(width = base * 0.012f, cap = StrokeCap.Round)
            )
        }
    } else {
        drawCircle(furLight.copy(alpha = 0.2f), radius = base * 0.025f, center = Offset(bodyCenter.x - base * 0.12f, bodyCenter.y - base * 0.02f))
        drawCircle(furLight.copy(alpha = 0.16f), radius = base * 0.018f, center = Offset(bodyCenter.x + base * 0.12f, bodyCenter.y + base * 0.03f))
    }
}

private fun DrawScope.drawPaws(bodyCenter: Offset, base: Float, furLight: Color, furDark: Color) {
    listOf(-1f, 1f).forEach { side ->
        drawOval(
            brush = Brush.radialGradient(listOf(furLight, furDark.copy(alpha = 0.8f))),
            topLeft = Offset(bodyCenter.x + side * base * 0.1f - base * 0.085f, bodyCenter.y + base * 0.09f),
            size = Size(base * 0.17f, base * 0.11f)
        )
        drawCircle(Color.White.copy(alpha = 0.18f), radius = base * 0.012f, center = Offset(bodyCenter.x + side * base * 0.08f, bodyCenter.y + base * 0.13f))
    }
}

private fun DrawScope.drawEars(headCenter: Offset, base: Float, fur: Color, furLight: Color, furDark: Color) {
    listOf(-1f, 1f).forEach { side ->
        val ear = Path().apply {
            moveTo(headCenter.x + side * base * 0.2f, headCenter.y - base * 0.16f)
            lineTo(headCenter.x + side * base * 0.33f, headCenter.y - base * 0.43f)
            lineTo(headCenter.x + side * base * 0.03f, headCenter.y - base * 0.28f)
            close()
        }
        drawPath(ear, brush = Brush.linearGradient(listOf(furLight, fur, furDark)))
        val inner = Path().apply {
            moveTo(headCenter.x + side * base * 0.19f, headCenter.y - base * 0.22f)
            lineTo(headCenter.x + side * base * 0.27f, headCenter.y - base * 0.35f)
            lineTo(headCenter.x + side * base * 0.08f, headCenter.y - base * 0.27f)
            close()
        }
        drawPath(inner, color = Color(0xFFFFB8B8).copy(alpha = 0.72f))
    }
}

private fun DrawScope.drawHeadDetails(headCenter: Offset, base: Float, furDark: Color, belly: Color, isProfile: Boolean, turn: Float) {
    val faceTurn = sin((turn / 180f) * PI.toFloat())
    val muzzleCenter = Offset(headCenter.x + faceTurn * base * 0.012f, headCenter.y + base * 0.087f)
    drawOval(
        brush = Brush.radialGradient(listOf(belly.copy(alpha = 0.82f), belly.copy(alpha = 0.28f), Color.Transparent)),
        topLeft = Offset(muzzleCenter.x - base * 0.145f, muzzleCenter.y - base * 0.082f),
        size = Size(base * 0.29f, base * 0.18f)
    )

    if (isProfile) {
        listOf(-0.13f, -0.055f, 0.055f, 0.13f).forEach { xOffset ->
            val stripe = Path().apply {
                moveTo(headCenter.x + base * xOffset, headCenter.y - base * 0.235f)
                cubicTo(
                    headCenter.x + base * (xOffset * 0.72f), headCenter.y - base * 0.17f,
                    headCenter.x + base * (xOffset * 0.52f), headCenter.y - base * 0.12f,
                    headCenter.x + base * (xOffset * 0.36f), headCenter.y - base * 0.08f
                )
            }
            drawPath(stripe, color = furDark.copy(alpha = 0.28f), style = Stroke(width = base * 0.014f, cap = StrokeCap.Round))
        }
    } else {
        drawOval(
            color = belly.copy(alpha = 0.62f),
            topLeft = Offset(headCenter.x - base * 0.08f, headCenter.y - base * 0.255f),
            size = Size(base * 0.16f, base * 0.1f)
        )
        listOf(-1f, 1f).forEach { side ->
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = base * 0.032f,
                center = Offset(headCenter.x + side * base * 0.165f, headCenter.y - base * 0.11f)
            )
        }
    }
}

private fun DrawScope.drawFace(headCenter: Offset, base: Float, eye: Color, cheek: Color, blink: Float, turn: Float, isProfile: Boolean) {
    val blinkScale = if (blink > 0.9f) 0.18f else 1f
    val faceTurn = sin((turn / 180f) * PI.toFloat())
    val eyeY = headCenter.y - base * 0.025f
    val leftEye = Offset(headCenter.x - base * 0.105f + faceTurn * base * 0.02f, eyeY)
    val rightEye = Offset(headCenter.x + base * 0.105f + faceTurn * base * 0.02f, eyeY)
    listOf(leftEye, rightEye).forEach { center ->
        drawOval(
            color = Color(0xFF130F12),
            topLeft = Offset(center.x - base * 0.049f, center.y - base * 0.066f * blinkScale),
            size = Size(base * 0.098f, base * 0.132f * blinkScale)
        )
        drawOval(
            brush = Brush.radialGradient(
                listOf(eye, if (isProfile) Color(0xFF3B2416) else Color(0xFFE8892F)),
                center = Offset(center.x - base * 0.012f, center.y - base * 0.02f),
                radius = base * 0.07f
            ),
            topLeft = Offset(center.x - base * 0.038f, center.y - base * 0.053f * blinkScale),
            size = Size(base * 0.076f, base * 0.106f * blinkScale)
        )
        if (blinkScale > 0.5f) {
            drawOval(
                color = Color(0xFF0B090A),
                topLeft = Offset(center.x - base * 0.012f, center.y - base * 0.03f),
                size = Size(base * 0.024f, base * 0.07f)
            )
            drawCircle(Color.White.copy(alpha = 0.98f), radius = base * 0.013f, center = Offset(center.x - base * 0.016f, center.y - base * 0.03f))
            drawCircle(Color.White.copy(alpha = 0.56f), radius = base * 0.006f, center = Offset(center.x + base * 0.015f, center.y + base * 0.008f))
        }
    }
    drawCircle(cheek.copy(alpha = 0.45f), radius = base * 0.038f, center = Offset(headCenter.x - base * 0.17f, headCenter.y + base * 0.06f))
    drawCircle(cheek.copy(alpha = 0.45f), radius = base * 0.038f, center = Offset(headCenter.x + base * 0.17f, headCenter.y + base * 0.06f))
    val nose = Path().apply {
        moveTo(headCenter.x, headCenter.y + base * 0.035f)
        lineTo(headCenter.x - base * 0.024f, headCenter.y + base * 0.068f)
        lineTo(headCenter.x + base * 0.024f, headCenter.y + base * 0.068f)
        close()
    }
    drawPath(nose, color = Color(0xFF6B3E36))
    drawPath(
        Path().apply {
            moveTo(headCenter.x, headCenter.y + base * 0.073f)
            quadraticTo(headCenter.x - base * 0.035f, headCenter.y + base * 0.105f, headCenter.x - base * 0.065f, headCenter.y + base * 0.08f)
            moveTo(headCenter.x, headCenter.y + base * 0.073f)
            quadraticTo(headCenter.x + base * 0.035f, headCenter.y + base * 0.105f, headCenter.x + base * 0.065f, headCenter.y + base * 0.08f)
        },
        color = Color(0xFF5B3B35),
        style = Stroke(width = base * 0.009f, cap = StrokeCap.Round)
    )
    listOf(-1f, 1f).forEach { side ->
        val startX = headCenter.x + side * base * 0.1f
        val endX = headCenter.x + side * base * 0.25f
        drawLine(Color.White.copy(alpha = if (isProfile) 0.68f else 0.4f), Offset(startX, headCenter.y + base * 0.03f), Offset(endX, headCenter.y + base * 0.0f), strokeWidth = base * 0.006f, cap = StrokeCap.Round)
        drawLine(Color.White.copy(alpha = if (isProfile) 0.62f else 0.36f), Offset(startX, headCenter.y + base * 0.065f), Offset(endX, headCenter.y + base * 0.07f), strokeWidth = base * 0.006f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawCollar(headCenter: Offset, base: Float, isProfile: Boolean) {
    val collarColor = if (isProfile) Color(0xFF4FA3FF) else Color(0xFFFFC857)
    drawLine(
        color = Color.Black.copy(alpha = 0.18f),
        start = Offset(headCenter.x - base * 0.16f, headCenter.y + base * 0.232f),
        end = Offset(headCenter.x + base * 0.16f, headCenter.y + base * 0.232f),
        strokeWidth = base * 0.022f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = collarColor.copy(alpha = 0.92f),
        start = Offset(headCenter.x - base * 0.145f, headCenter.y + base * 0.22f),
        end = Offset(headCenter.x + base * 0.145f, headCenter.y + base * 0.22f),
        strokeWidth = base * 0.018f,
        cap = StrokeCap.Round
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, collarColor, Color(0xFF8A5A00))),
        radius = base * 0.026f,
        center = Offset(headCenter.x, headCenter.y + base * 0.247f)
    )
}

private fun DrawScope.drawSparkles(headCenter: Offset, base: Float, tapPulse: Float) {
    if (tapPulse <= 0.02f) return
    val alpha = tapPulse.coerceIn(0f, 1f)
    listOf(
        Offset(headCenter.x - base * 0.25f, headCenter.y - base * 0.22f),
        Offset(headCenter.x + base * 0.28f, headCenter.y - base * 0.16f),
        Offset(headCenter.x + base * 0.22f, headCenter.y + base * 0.16f)
    ).forEachIndexed { index, center ->
        val radius = base * (0.025f + index * 0.004f) * (1f + (1f - alpha) * 1.4f)
        drawCircle(Color(0xFFFFF1A8).copy(alpha = alpha), radius = radius, center = center)
        drawCircle(Color.White.copy(alpha = alpha * 0.7f), radius = radius * 0.36f, center = center)
    }
}
