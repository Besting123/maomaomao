package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

data class ForumCommentState(
    val author: String,
    val content: String,
    val time: String
)

data class ForumPostState(
    val id: String,
    val category: String,
    val title: String,
    val content: String,
    val author: String,
    val time: String,
    val liked: Boolean = false,
    val collected: Boolean = false,
    val comments: List<ForumCommentState> = emptyList()
)

data class RewardExchangeRecord(
    val title: String,
    val cost: Int,
    val time: String
)

data class MainAppState(
    val tokenBalance: Int = 350,
    val signInDays: Int = 3,
    val hasSignedInToday: Boolean = false,
    val tasks: List<TaskState> = listOf(
        TaskState("1", "完成1次安全观察记录", "保持距离记录猫咪状态", 10, false, TaskType.DAILY),
        TaskState("2", "进行1次温和安抚互动", "先判断边界再传递善意", 15, false, TaskType.DAILY),
        TaskState("3", "阅读《如何正确判断猫咪情绪》", "掌握正确的互动边界", 20, false, TaskType.LEARNING),
        TaskState("4", "连续陪伴同一只猫咪3天", "用稳定远观建立长期守护", 50, false, TaskType.SPECIAL)
    ),
    val companionRecords: List<CompanionRecord> = listOf(
        CompanionRecord("10月24日", "补水", "大橘", "「大橘今天看起来心情不错，喝了不少水。」", 2),
        CompanionRecord("10月22日", "观察", "奶油", "记录了奶油在草坪东侧活跃状态。", 3)
    ),
    val publishedForumPosts: List<ForumPostState> = listOf(
        ForumPostState(
            id = "sample-water-point",
            category = "片区记录",
            title = "图书馆北门补水点已清理",
            content = "今天中午路过时看到水碗有落叶，已经清理并补充了清水。这个点位只记录为图书馆北侧片区，不建议公开精确坐标。",
            author = "图书馆路过同学",
            time = "12:20",
            comments = listOf(
                ForumCommentState("喵伴志愿者", "收到，晚间巡查时会复核水碗状态。", "12:32"),
                ForumCommentState("北门观察员", "刚刚远观过，没有猫咪聚集，片区安静。", "12:48")
            )
        ),
        ForumPostState(
            id = "sample-yunduo-observe",
            category = "目击记录",
            title = "云朵傍晚在教学区边缘短暂停留",
            content = "18 点左右远距离看到云朵经过教学区边缘，步态正常，没有明显应激。建议继续保持远观，不要多人围过去。",
            author = "校园观察者",
            time = "18:10",
            comments = listOf(
                ForumCommentState("我", "已加入今日观察记录，优先提醒大家保持距离。", "18:16")
            )
        )
    ),
    val sightingLiked: Boolean = false,
    val hasJoinedEmergencyQueue: Boolean = false,
    val sightingComments: List<ForumCommentState> = listOf(
        ForumCommentState("北门观察员", "看起来状态不错，建议保持远距离观察就好。", "10:12"),
        ForumCommentState("喵伴志愿者", "已记录为片区级动态，不会公开精确位置。", "10:18"),
        ForumCommentState("图书馆路过同学", "刚刚经过，没有围观，奶牛还在安静晒太阳。", "10:25")
    ),
    val followedCatNames: Set<String> = setOf("大橘", "云朵", "奶油"),
    val selectedProfileCatName: String = "大橘",
    val joinedWeekendShelterEvent: Boolean = false,
    val rewardExchangeRecords: List<RewardExchangeRecord> = emptyList(),
    val completedCourseTitles: Set<String> = setOf("边界与安全", "情绪识别", "科学补水"),
    val dailyQuizCompleted: Boolean = false,
    val learningProgress: Float = 0.3f,
    val completedCoursesCount: Int = 3,
    val totalCoursesCount: Int = 10,
    // 游戏化状态
    val petLevel: Int = 5,
    val petExp: Int = 320,
    val petExpToNext: Int = 500,
    val hungerValue: Float = 0.7f,
    val happinessValue: Float = 0.85f,
    val healthValue: Float = 0.92f
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
    
    fun completeCourse(courseTitle: String) {
        _uiState.update { currentState ->
            if (courseTitle in currentState.completedCourseTitles) return@update currentState
            val updatedTitles = currentState.completedCourseTitles + courseTitle
            val completedCount = updatedTitles.size.coerceAtMost(currentState.totalCoursesCount)
            currentState.copy(
                completedCourseTitles = updatedTitles,
                completedCoursesCount = completedCount,
                learningProgress = (completedCount.toFloat() / currentState.totalCoursesCount).coerceIn(0f, 1f)
            )
        }
    }

    fun completeQuiz() {
        _uiState.update { currentState ->
            if (currentState.dailyQuizCompleted) {
                currentState
            } else {
                currentState.copy(
                    dailyQuizCompleted = true,
                    tokenBalance = currentState.tokenBalance + 20
                )
            }
        }
    }

    fun exchangeReward(title: String, cost: Int): Boolean {
        val currentState = _uiState.value
        if (currentState.tokenBalance < cost) return false
        val record = RewardExchangeRecord(
            title = title,
            cost = cost,
            time = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA).format(Date())
        )
        _uiState.value = currentState.copy(
            tokenBalance = currentState.tokenBalance - cost,
            rewardExchangeRecords = listOf(record) + currentState.rewardExchangeRecords
        )
        return true
    }

    fun toggleCatFollow(catName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                followedCatNames = if (catName in currentState.followedCatNames) {
                    currentState.followedCatNames - catName
                } else {
                    currentState.followedCatNames + catName
                }
            )
        }
    }

    fun selectProfileCat(catName: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedProfileCatName = catName)
        }
    }

    fun toggleWeekendShelterEvent() {
        _uiState.update { currentState ->
            currentState.copy(joinedWeekendShelterEvent = !currentState.joinedWeekendShelterEvent)
        }
    }

    fun publishForumPost(category: String, title: String, content: String) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isEmpty() || trimmedContent.isEmpty()) return

        val post = ForumPostState(
            id = Date().time.toString(),
            category = category,
            title = trimmedTitle,
            content = trimmedContent,
            author = "路过图书馆的小王",
            time = "刚刚"
        )

        _uiState.update { currentState ->
            currentState.copy(publishedForumPosts = listOf(post) + currentState.publishedForumPosts)
        }
    }

    fun toggleForumPostLike(postId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                publishedForumPosts = currentState.publishedForumPosts.map { post ->
                    if (post.id == postId) post.copy(liked = !post.liked) else post
                }
            )
        }
    }

    fun toggleForumPostCollection(postId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                publishedForumPosts = currentState.publishedForumPosts.map { post ->
                    if (post.id == postId) post.copy(collected = !post.collected) else post
                }
            )
        }
    }

    fun addForumPostComment(postId: String, content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) return

        val comment = ForumCommentState(
            author = "我",
            content = trimmedContent,
            time = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
        )

        _uiState.update { currentState ->
            currentState.copy(
                publishedForumPosts = currentState.publishedForumPosts.map { post ->
                    if (post.id == postId) {
                        post.copy(comments = post.comments + comment)
                    } else {
                        post
                    }
                }
            )
        }
    }

    fun toggleSightingLike() {
        _uiState.update { currentState ->
            currentState.copy(sightingLiked = !currentState.sightingLiked)
        }
    }

    fun addSightingComment(content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) return

        val comment = ForumCommentState(
            author = "我",
            content = trimmedContent,
            time = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
        )

        _uiState.update { currentState ->
            currentState.copy(sightingComments = currentState.sightingComments + comment)
        }
    }

    fun joinEmergencyQueue() {
        _uiState.update { currentState ->
            currentState.copy(hasJoinedEmergencyQueue = true)
        }
    }

    fun interactWithCat(actionName: String, catName: String = "橘子", cost: Int = 0): Boolean {
        val currentState = _uiState.value
        if (currentState.tokenBalance < cost) return false

        val formattedTime = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA).format(Date())
        val newRecord = CompanionRecord(
            time = formattedTime,
            action = actionName,
            catName = catName,
            description = "进行了一次${actionName}互动。",
            colorType = (1..3).random()
        )

        val hungerDelta = when (actionName) {
            "添粮" -> 0.15f; "补水" -> 0.05f; else -> -0.02f
        }
        val happinessDelta = when (actionName) {
            "安抚" -> 0.12f; "观察" -> 0.05f; else -> 0.03f
        }
        val healthDelta = when (actionName) {
            "补水" -> 0.08f; "观察" -> 0.03f; else -> 0.01f
        }
        val expGain = when (actionName) {
            "安抚" -> 15; "观察" -> 10; "补水" -> 12; "添粮" -> 12; else -> 5
        }

        val newExp = currentState.petExp + expGain
        val levelUp = newExp >= currentState.petExpToNext

        _uiState.value = currentState.copy(
            tokenBalance = currentState.tokenBalance - cost,
            companionRecords = listOf(newRecord) + currentState.companionRecords,
            hungerValue = (currentState.hungerValue + hungerDelta).coerceIn(0f, 1f),
            happinessValue = (currentState.happinessValue + happinessDelta).coerceIn(0f, 1f),
            healthValue = (currentState.healthValue + healthDelta).coerceIn(0f, 1f),
            petExp = if (levelUp) newExp - currentState.petExpToNext else newExp,
            petLevel = if (levelUp) currentState.petLevel + 1 else currentState.petLevel,
            petExpToNext = if (levelUp) currentState.petExpToNext + 100 else currentState.petExpToNext
        )
        return true
    }
}
