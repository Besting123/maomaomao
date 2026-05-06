package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.launch

enum class CatViewerMode {
    COMPANION,
    PROFILE
}

@Composable
fun CatModel3DViewer(
    modifier: Modifier = Modifier,
    modelAssetPath: String = "models/mao-lihua-animated.glb",
    label: String = "3D 陪伴狸花猫",
    isFullScreen: Boolean = false,
    mode: CatViewerMode = if (isFullScreen) CatViewerMode.COMPANION else CatViewerMode.PROFILE,
    animationName: String = "Idle",
    animationLoop: Boolean = true,
    animationSpeed: Float = 1f,
    onDoubleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hasModelAsset = remember(modelAssetPath) {
        val folder = modelAssetPath.substringBeforeLast('/', missingDelimiterValue = "")
        val fileName = modelAssetPath.substringAfterLast('/')
        context.assets.list(folder)?.contains(fileName) == true
    }
    var hasInteracted by rememberSaveable(modelAssetPath, mode) { mutableStateOf(false) }
    val rotationY = remember { Animatable(if (mode == CatViewerMode.COMPANION) -15f else 12f) }
    val decaySpec = remember { exponentialDecay<Float>() }
    val draggableState = rememberDraggableState { delta ->
        hasInteracted = true
        coroutineScope.launch { rotationY.snapTo((rotationY.value + delta * 0.35f).coerceIn(-50f, 50f)) }
    }

    Box(
        modifier = modifier
            .clip(if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp))
            .background(if (mode == CatViewerMode.COMPANION) Color.Transparent else Color(0xFFFFF3E4))
    ) {
        AnimatedModelBackdrop(mode = mode)
        ModelGroundingStage(mode = mode, animationName = animationName)

        if (hasModelAsset) {
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)
            val environmentLoader = rememberEnvironmentLoader(engine)
            val environment = remember(environmentLoader) {
                environmentLoader.createHDREnvironment("environments/studio.hdr")
            }
            val modelInstance = rememberModelInstance(modelLoader, modelAssetPath)

            SceneView(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            hasInteracted = true
                            onDoubleTap()
                        })
                    }
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        onDragStopped = { velocity ->
                            coroutineScope.launch {
                                rotationY.animateDecay(
                                    initialVelocity = velocity * 0.035f,
                                    animationSpec = decaySpec
                                )
                            }
                        }
                    ),
                engine = engine,
                modelLoader = modelLoader,
                environmentLoader = environmentLoader,
                environment = environment!!,
                cameraManipulator = null
            ) {
                modelInstance?.let { instance ->
                    ModelNode(
                        modelInstance = instance,
                        scaleToUnits = if (mode == CatViewerMode.COMPANION) 0.72f else 0.98f,
                        position = if (mode == CatViewerMode.COMPANION) Float3(0f, -0.08f, 0f) else Float3(0f, -0.06f, 0f),
                        rotation = Float3(0f, rotationY.value, 0f),
                        autoAnimate = false,
                        animationName = animationName,
                        animationLoop = animationLoop,
                        animationSpeed = animationSpeed
                    )
                }
            }

            if (modelInstance == null) {
                ModelLoadingOverlay()
            }
        } else {
            ModelMissingOverlay(modelAssetPath = modelAssetPath)
        }

        AnimatedModelForeground(mode = mode)
        GestureHint(visible = hasModelAsset && !hasInteracted, mode = mode)
        if (mode == CatViewerMode.PROFILE) {
            ModelLabel(label = label, animationName = animationName)
        }
    }
}

@Composable
private fun BoxScope.AnimatedModelBackdrop(mode: CatViewerMode) {
    val transition = rememberInfiniteTransition(label = "model-stage")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "model-stage-pulse"
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
            .offset(y = if (mode == CatViewerMode.COMPANION) 28.dp else 0.dp)
            .size(if (mode == CatViewerMode.COMPANION) 350.dp else 285.dp)
            .graphicsLayer(scaleX = pulse, scaleY = pulse)
            .background(
                Brush.radialGradient(listOf(accent.copy(alpha = 0.38f), accent.copy(alpha = 0.1f), Color.Transparent)),
                CircleShape
            )
    )
}

@Composable
private fun BoxScope.ModelGroundingStage(mode: CatViewerMode, animationName: String) {
    val transition = rememberInfiniteTransition(label = "model-ground")
    val pulse by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "model-ground-pulse"
    )
    val accent = when (animationName) {
        "Drink" -> MaterialTheme.colorScheme.tertiary
        "Eat" -> MaterialTheme.colorScheme.primary
        "Pet", "Happy" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    val bottomOffset = if (mode == CatViewerMode.COMPANION) (-34).dp else (-18).dp

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = bottomOffset)
            .width(if (mode == CatViewerMode.COMPANION) 310.dp else 240.dp)
            .height(if (mode == CatViewerMode.COMPANION) 86.dp else 58.dp)
            .graphicsLayer(scaleX = pulse)
            .background(
                Brush.radialGradient(
                    listOf(
                        accent.copy(alpha = if (mode == CatViewerMode.COMPANION) 0.22f else 0.14f),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = bottomOffset + 10.dp)
            .width(if (mode == CatViewerMode.COMPANION) 230.dp else 180.dp)
            .height(if (mode == CatViewerMode.COMPANION) 42.dp else 30.dp)
            .shadow(18.dp, CircleShape, spotColor = Color(0xFF3E2723).copy(alpha = 0.28f))
            .background(Color(0xFF4E342E).copy(alpha = if (mode == CatViewerMode.COMPANION) 0.28f else 0.16f), CircleShape)
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = bottomOffset - 2.dp)
            .width(if (mode == CatViewerMode.COMPANION) 268.dp else 204.dp)
            .height(if (mode == CatViewerMode.COMPANION) 50.dp else 34.dp)
            .border(1.dp, Color.White.copy(alpha = 0.46f), CircleShape)
            .background(Color.White.copy(alpha = if (mode == CatViewerMode.COMPANION) 0.18f else 0.1f), CircleShape)
    )

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = bottomOffset - 12.dp)
            .width(if (mode == CatViewerMode.COMPANION) 218.dp else 160.dp)
            .height(if (mode == CatViewerMode.COMPANION) 20.dp else 14.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.34f),
                        Color(0xFF8B5928).copy(alpha = 0.16f),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )

    if (mode == CatViewerMode.COMPANION) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = bottomOffset - 28.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.82f))
                .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val props = when (animationName) {
                "Drink" -> listOf("💧", "小水碗")
                "Eat" -> listOf("🐟", "少量添粮")
                "Pet", "Happy" -> listOf("💕", "轻柔安抚")
                else -> listOf("🌿", "安静观察")
            }
            Text(props[0], fontSize = 14.sp)
            Text(props[1], fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@Composable
private fun BoxScope.AnimatedModelForeground(mode: CatViewerMode) {
    val shadowColors = if (mode == CatViewerMode.COMPANION) {
        listOf(Color.Transparent, Color(0xFF8B5928).copy(alpha = 0.10f), Color(0xFF4D6C51).copy(alpha = 0.08f))
    } else {
        listOf(Color.Transparent, Color(0xFF8B5928).copy(alpha = 0.12f))
    }
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(if (mode == CatViewerMode.COMPANION) 92.dp else 110.dp)
            .background(Brush.verticalGradient(shadowColors))
    )
}

@Composable
private fun BoxScope.GestureHint(visible: Boolean, mode: CatViewerMode) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450)),
        exit = fadeOut(tween(320)),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)
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
                text = if (mode == CatViewerMode.COMPANION) "拖动转身 · 双击安抚" else "拖动观察动态模型",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E)
            )
        }
    }
}

@Composable
private fun ModelLoadingOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("正在唤醒小猫...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ModelMissingOverlay(modelAssetPath: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🐈", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text("小猫模型暂时不可用", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(modelAssetPath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BoxScope.ModelLabel(label: String, animationName: String) {
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(14.dp)
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("情境动效：$animationName", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
