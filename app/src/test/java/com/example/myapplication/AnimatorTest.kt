package com.example.myapplication

import org.junit.Test
import java.lang.reflect.Method

class AnimatorTest {
    @Test
    fun testAnimatorMethods() {
        val animatableModelClass = Class.forName("io.github.sceneview.animation.AnimatableModel")
        val modelInstanceClass = Class.forName("io.github.sceneview.model.ModelInstance")
        val modelNodeClass = Class.forName("io.github.sceneview.node.ModelNode")
        
        println("ModelInstance is AnimatableModel: ${animatableModelClass.isAssignableFrom(modelInstanceClass)}")
        println("ModelNode is AnimatableModel: ${animatableModelClass.isAssignableFrom(modelNodeClass)}")
    }
}
