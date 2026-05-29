package com.example.myapplication.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Renders the companion cat model inside a neutral SceneView card. Drag to rotate,
 * double-tap to trigger the host's onDoubleTap.
 */
@Composable
fun CatModel3DViewer(
    modifier: Modifier = Modifier,
    modelAssetPath: String = "models/kitty.glb",
    environmentAssetPath: String = "environments/lythwood_lounge_1k.hdr",
    animationName: String = "Idle",
    animationLoop: Boolean = true,
    animationSpeed: Float = 1f,
    deferSceneMillis: Long = 0L,
    onDoubleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hasModelAsset = remember(modelAssetPath) {
        val folder = modelAssetPath.substringBeforeLast('/', missingDelimiterValue = "")
        val fileName = modelAssetPath.substringAfterLast('/')
        context.assets.list(folder)?.contains(fileName) == true
    }
    var hasInteracted by rememberSaveable(modelAssetPath) { mutableStateOf(false) }
    val rotationY = remember { Animatable(-15f) }
    val decaySpec = remember { exponentialDecay<Float>() }
    val draggableState = rememberDraggableState { delta ->
        hasInteracted = true
        coroutineScope.launch { rotationY.snapTo((rotationY.value + delta * 0.35f).coerceIn(-50f, 50f)) }
    }
    var sceneReady by remember(modelAssetPath, deferSceneMillis) { mutableStateOf(deferSceneMillis == 0L) }

    LaunchedEffect(modelAssetPath, deferSceneMillis) {
        if (deferSceneMillis > 0L) {
            sceneReady = false
            delay(deferSceneMillis)
            sceneReady = true
        } else {
            sceneReady = true
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Transparent)
    ) {
        ViewerBackdrop()

        if (hasModelAsset && sceneReady) {
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)
            val environmentLoader = rememberEnvironmentLoader(engine)
            val view = rememberView(engine).apply {
                blendMode = com.google.android.filament.View.BlendMode.OPAQUE
                isPostProcessingEnabled = false
            }
            val renderer = rememberRenderer(engine).apply {
                clearOptions = clearOptions.apply {
                    clear = true
                    discard = true
                    clearColor = floatArrayOf(0.96f, 0.94f, 0.90f, 1f)
                }
            }
            val environment = remember(environmentLoader, environmentAssetPath) {
                environmentLoader.createHDREnvironment(environmentAssetPath, createSkybox = false)
            }
            val modelInstance = rememberModelInstance(modelLoader, modelAssetPath)

            if (environment != null) {
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
                    view = view,
                    renderer = renderer,
                    surfaceType = SurfaceType.TextureSurface,
                    isOpaque = true,
                    environment = environment,
                    cameraManipulator = null
                ) {
                    modelInstance?.let { instance ->
                        ModelNode(
                            modelInstance = instance,
                            scaleToUnits = 0.5f,
                            position = Float3(0f, -0.2f, 0f),
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
                ModelMissingOverlay(modelAssetPath = environmentAssetPath)
            }
        } else if (hasModelAsset) {
            ModelLoadingOverlay()
        } else {
            ModelMissingOverlay(modelAssetPath = modelAssetPath)
        }
    }
}

@Composable
private fun BoxScope.ViewerBackdrop() {
    val gradient = listOf(Color(0xFFF8F6F1), Color(0xFFEDE8DE), Color(0xFFF4F1EA))
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradient)))
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(y = 16.dp)
            .size(310.dp)
            .background(Brush.radialGradient(listOf(Color(0xFFFFFFFF), Color(0x223F4A5A), Color.Transparent)), CircleShape)
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 22.dp, y = 24.dp)
            .width(100.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEAF7FF).copy(alpha = 0.86f))
            .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = (-18).dp)
            .width(230.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFD8D2C6))
    )
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
