package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.draw.alpha
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
    modelAssetPath: String = "models/cat.glb",
    label: String = "3D 立体猫咪模型",
    isFullScreen: Boolean = false,
    mode: CatViewerMode = if (isFullScreen) CatViewerMode.COMPANION else CatViewerMode.PROFILE,
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
    val containerShape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp)
    val rotationY = remember { Animatable(-18f) }
    val decaySpec = remember { exponentialDecay<Float>() }

    LaunchedEffect(hasInteracted) {
        if (!hasInteracted) {
            while (true) {
                rotationY.animateTo(
                    targetValue = rotationY.value + 360f,
                    animationSpec = tween(durationMillis = if (mode == CatViewerMode.COMPANION) 22000 else 28000, easing = LinearEasing)
                )
            }
        }
    }

    val draggableState = rememberDraggableState { delta ->
        hasInteracted = true
        coroutineScope.launch {
            rotationY.snapTo(rotationY.value + delta * 0.42f)
        }
    }

    Box(
        modifier = modifier
            .clip(containerShape)
            .background(if (mode == CatViewerMode.COMPANION) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
    ) {
        CatViewerBackdrop(mode = mode)

        if (hasModelAsset) {
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)
            val environmentLoader = rememberEnvironmentLoader(engine)
            val environment = remember(environmentLoader) {
                environmentLoader.createHDREnvironment("environments/studio.hdr")
            }

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
                                    initialVelocity = velocity * 0.045f,
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
                rememberModelInstance(modelLoader, modelAssetPath)?.let { modelInstance ->
                    ModelNode(
                        modelInstance = modelInstance,
                        scaleToUnits = if (mode == CatViewerMode.COMPANION) 1.95f else 1.52f,
                        position = if (mode == CatViewerMode.COMPANION) Float3(0f, -0.72f, 0f) else Float3(0f, -0.48f, 0f),
                        rotation = Float3(0f, rotationY.value, 0f),
                        autoAnimate = false
                    )
                }
            }
        } else {
            CatModelFallback(modelAssetPath = modelAssetPath, mode = mode)
        }

        CatPedestal(mode = mode)
        CatViewerForeground(mode = mode)
        CatViewerHint(
            visible = hasModelAsset && !hasInteracted,
            mode = mode
        )

        if (mode == CatViewerMode.PROFILE) {
            CatViewerLabel(label = label, modelAssetPath = modelAssetPath)
        }
    }
}

@Composable
private fun BoxScope.CatViewerBackdrop(mode: CatViewerMode) {
    val transition = rememberInfiniteTransition(label = "cat-stage")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "stage-pulse"
    )
    val backgroundColors = if (mode == CatViewerMode.COMPANION) {
        listOf(Color(0x001A1428), Color(0x552B1B3D), Color(0x664A2E1F))
    } else {
        listOf(Color(0xFFECE2D3), Color(0xFFF8F1E8), Color(0xFFFFFBF4))
    }
    val glowColor = if (mode == CatViewerMode.COMPANION) Color(0xFFFFB74D) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundColors))
    )
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(y = if (mode == CatViewerMode.COMPANION) 34.dp else 0.dp)
            .size(if (mode == CatViewerMode.COMPANION) 360.dp else 280.dp)
            .graphicsLayer(scaleX = pulse, scaleY = pulse)
            .background(
                Brush.radialGradient(
                    listOf(glowColor.copy(alpha = 0.34f), glowColor.copy(alpha = 0.08f), Color.Transparent)
                ),
                CircleShape
            )
    )
}

@Composable
private fun BoxScope.CatPedestal(mode: CatViewerMode) {
    val bottomOffset = if (mode == CatViewerMode.COMPANION) 116.dp else 56.dp
    val width = if (mode == CatViewerMode.COMPANION) 260.dp else 210.dp
    val height = if (mode == CatViewerMode.COMPANION) 58.dp else 42.dp
    Canvas(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = bottomOffset)
            .size(width = width, height = height)
            .alpha(if (mode == CatViewerMode.COMPANION) 0.9f else 0.68f)
    ) {
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color.Black.copy(alpha = 0.34f), Color.Black.copy(alpha = 0.1f), Color.Transparent)
            )
        )
    }
}

@Composable
private fun BoxScope.CatViewerForeground(mode: CatViewerMode) {
    val height = if (mode == CatViewerMode.COMPANION) 220.dp else 150.dp
    val alpha = if (mode == CatViewerMode.COMPANION) 0.34f else 0.22f
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = alpha))))
    )
}

@Composable
private fun BoxScope.CatViewerHint(visible: Boolean, mode: CatViewerMode) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(400)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = if (mode == CatViewerMode.COMPANION) 146.dp else 18.dp)
    ) {
        Row(
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
                .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("↔", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF6D4C41))
            Text(
                text = if (mode == CatViewerMode.COMPANION) "拖动观察 · 双击安抚" else "拖动旋转观察花色",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E)
            )
        }
    }
}

@Composable
private fun BoxScope.CatViewerLabel(label: String, modelAssetPath: String) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(14.dp)
            .background(Color.White.copy(alpha = 0.88f), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(14.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(modelAssetPath, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CatModelFallback(modelAssetPath: String, mode: CatViewerMode) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🐈", fontSize = if (mode == CatViewerMode.COMPANION) 88.sp else 72.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("小猫暂时躲起来了", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "检查模型资源：$modelAssetPath",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
