package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.environment.Environment

@Composable
fun CatModel3DViewer(
    modifier: Modifier = Modifier,
    modelAssetPath: String = "models/cat.glb",
    label: String = "3D 立体猫咪模型",
    animationIndex: Int = 0,
    isFullScreen: Boolean = false,
    onDoubleTap: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hasModelAsset = remember(modelAssetPath) {
        val folder = modelAssetPath.substringBeforeLast('/', missingDelimiterValue = "")
        val fileName = modelAssetPath.substringAfterLast('/')
        context.assets.list(folder)?.contains(fileName) == true
    }

    Box(
        modifier = modifier
            .then(
                if (isFullScreen) Modifier 
                else Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            )
    ) {
        if (hasModelAsset) {
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)
            val environmentLoader = rememberEnvironmentLoader(engine)
            
            // 加载环境光照 HDR，以提供真实的 PBR 材质反射和光影
            val environment = remember(environmentLoader) {
                environmentLoader.createHDREnvironment("environments/studio.hdr")
            }
            
            val rotationY = remember { Animatable(0f) }
            val decaySpec = remember { exponentialDecay<Float>() }

            val draggableState = rememberDraggableState { delta ->
                coroutineScope.launch {
                    rotationY.snapTo(rotationY.value + delta * 0.5f)
                }
            }

            SceneView(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { onDoubleTap() })
                    }
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        onDragStopped = { velocity ->
                            coroutineScope.launch {
                                rotationY.animateDecay(
                                    initialVelocity = velocity * 0.05f,
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
                        scaleToUnits = 1.6f,
                        // 将模型下沉 -0.5f 保证头部正好处于黄金视觉中心
                        position = Float3(0f, -0.5f, 0f), 
                        rotation = Float3(0f, rotationY.value, 0f),
                        autoAnimate = false
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("等待 3D 模型资产", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    "请在 Android Studio 中放入 app/src/main/assets/$modelAssetPath",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (!isFullScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.White.copy(alpha = 0.88f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(modelAssetPath, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
