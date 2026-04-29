package com.example.myapplication

import org.junit.Test
import java.lang.reflect.Method

class AnimatorTest {
    @Test
    fun testAnimatorMethods() {
        try {
            // SceneView usually uses filament's Manipulator or wraps it
            val clazz = Class.forName("io.github.sceneview.collision.CameraManipulator")
            clazz.methods.forEach { println("Method: ${it.name}") }
        } catch(e: Exception) { println("CameraManipulator not found in collision") }
        
        try {
            val clazz = Class.forName("io.github.sceneview.node.CameraNode")
            clazz.methods.forEach { println("CameraNode Method: ${it.name}") }
        } catch(e: Exception) { println("CameraNode not found") }
    }
}
