package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TaskState(
    val id: String,
    val title: String,
    val subtitle: String,
    val reward: Int,
    val isCompleted: Boolean,
    val type: TaskType
)

enum class TaskType {
    DAILY, LEARNING, SPECIAL
}

data class CompanionRecord(
    val time: String,
    val action: String,
    val catName: String,
    val description: String,
    val colorType: Int // 1 for primary, 2 for secondary, 3 for tertiary
)

data class MainAppState(
    val tokenBalance: Int = 350,
    val signInDays: Int = 3,
    val hasSignedInToday: Boolean = false,
    val tasks: List<TaskState> = listOf(
        TaskState("1", "完成1次观察记录", "记录猫咪的健康状态", 10, false, TaskType.DAILY),
        TaskState("2", "进行1次安抚互动", "传递你的善意", 15, false, TaskType.DAILY),
        TaskState("3", "阅读《如何正确判断猫咪情绪》", "掌握正确的互动边界", 20, false, TaskType.LEARNING),
        TaskState("4", "连续陪伴同一只猫咪3天", "建立深厚情感羁绊", 50, false, TaskType.SPECIAL)
    ),
    val companionRecords: List<CompanionRecord> = listOf(
        CompanionRecord("10月24日", "补水", "大橘", "「大橘今天看起来心情不错，喝了不少水。」", 2),
        CompanionRecord("10月22日", "观察", "奶油", "记录了奶油在草坪东侧活跃状态。", 3)
    ),
    val learningProgress: Float = 0.3f,
    val completedCoursesCount: Int = 3,
    val totalCoursesCount: Int = 10
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainAppState())
    val uiState: StateFlow<MainAppState> = _uiState.asStateFlow()

    fun signIn() {
        _uiState.update { currentState ->
            if (!currentState.hasSignedInToday) {
                currentState.copy(
                    hasSignedInToday = true,
                    signInDays = currentState.signInDays + 1,
                    tokenBalance = currentState.tokenBalance + 10 // Reward for signing in
                )
            } else {
                currentState
            }
        }
    }

    fun completeTask(taskId: String) {
        _uiState.update { currentState ->
            val updatedTasks = currentState.tasks.map {
                if (it.id == taskId && !it.isCompleted) {
                    it.copy(isCompleted = true)
                } else {
                    it
                }
            }
            
            // Find reward if task was just completed
            val reward = currentState.tasks.find { it.id == taskId && !it.isCompleted }?.reward ?: 0
            
            currentState.copy(
                tasks = updatedTasks,
                tokenBalance = currentState.tokenBalance + reward
            )
        }
    }
    
    fun completeQuiz() {
        _uiState.update {
            it.copy(tokenBalance = it.tokenBalance + 20)
        }
    }

    fun interactWithCat(actionName: String, catName: String = "橘子", cost: Int = 0) {
        _uiState.update { currentState ->
            if (currentState.tokenBalance >= cost) {
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("MM月dd日 HH:mm")
                val formattedTime = currentDateTime.format(formatter)
                
                val newRecord = CompanionRecord(
                    time = formattedTime,
                    action = actionName,
                    catName = catName,
                    description = "进行了一次${actionName}互动。",
                    colorType = (1..3).random()
                )
                
                currentState.copy(
                    tokenBalance = currentState.tokenBalance - cost,
                    companionRecords = listOf(newRecord) + currentState.companionRecords
                )
            } else {
                currentState // Not enough tokens
            }
        }
    }
}
