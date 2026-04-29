package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.myapplication.R
import com.example.myapplication.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(onBackClick: () -> Unit, viewModel: MainViewModel? = null) {
    val scrollState = rememberScrollState()
    var selectedCourse by remember { mutableStateOf<KnowledgeCard?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新手学堂", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ProgressTrackerSection()
            Spacer(modifier = Modifier.height(32.dp))
            KnowledgeGallerySection(onCourseClick = { selectedCourse = it })
            Spacer(modifier = Modifier.height(32.dp))
            QuizSection(onCorrectAnswer = { viewModel?.completeQuiz() })
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    selectedCourse?.let { course ->
        AlertDialog(
            onDismissRequest = { selectedCourse = null },
            confirmButton = {
                TextButton(onClick = { selectedCourse = null }) {
                    Text("完成学习")
                }
            },
            icon = { Icon(Icons.Outlined.School, contentDescription = null) },
            title = { Text(course.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(course.subtitle, fontWeight = FontWeight.Medium)
                    Text(course.detail, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("预计阅读 ${course.readTime} · 前端演示课程", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
fun ProgressTrackerSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.School, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text("初级观察员", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text("已完成 3/10 必修课程", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { 0.3f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("再完成1课，即可升级为中级观察员！", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

data class KnowledgeCard(val title: String, val subtitle: String, val imageRes: Int, val readTime: String, val detail: String)

@Composable
fun KnowledgeGallerySection(onCourseClick: (KnowledgeCard) -> Unit = {}) {
    val cards = listOf(
        KnowledgeCard("边界与安全", "不要强行抚摸流浪猫", R.drawable.img_net_c9e15cf0b7, "3分钟", "先观察尾巴、耳朵、瞳孔和身体姿态。猫咪主动靠近前，不要伸手追摸；如果它后退、低吼或飞机耳，应立刻停止接近。"),
        KnowledgeCard("情绪识别", "读懂猫咪的尾巴语言", R.drawable.img_net_27ce5092c2, "5分钟", "快速甩尾、耳朵后贴、身体压低通常代表紧张或警告。慢眨眼、尾巴自然竖起、身体放松才更适合温和互动。"),
        KnowledgeCard("科学补水", "为何补水比喂零食更重要", R.drawable.img_net_e10e6b9fb1, "4分钟", "校园猫常见问题之一是饮水不足。补水点维护、湿粮比例和避免高盐零食，比单次投喂更有长期健康价值。")
    )

    Column {
        Text(
            text = "必修课程推荐",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(cards) { card ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clickable { onCourseClick(card) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f/3f)) {
                            Image(
                                painter = painterResource(card.imageRes),
                                contentDescription = card.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(card.readTime, color = Color.White, fontSize = 10.sp)
                            }
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(card.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(card.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizSection(onCorrectAnswer: () -> Unit) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var rewarded by remember { mutableStateOf(false) }
    val correctAnswer = "烦躁、警告，不要靠近"

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("每日实战测验", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("情境测试", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Text("+20 小鱼干", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("当流浪猫快速摇晃尾巴，耳朵向后平贴时，它表达的情绪是？", fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("开心，想互动", correctAnswer, "饿了，要吃的").forEach { option ->
                        QuizOptionButton(
                            text = option,
                            isCorrect = option == correctAnswer,
                            selectedAnswer = selectedAnswer,
                            onSelect = {
                                if (selectedAnswer == null) {
                                    selectedAnswer = option
                                    if (option == correctAnswer && !rewarded) {
                                        rewarded = true
                                        onCorrectAnswer()
                                    }
                                }
                            }
                        )
                    }
                    selectedAnswer?.let { answer ->
                        val correct = answer == correctAnswer
                        Text(
                            text = if (correct) "回答正确：保持距离、不刺激，是最安全的选择。已获得 20 小鱼干。" else "回答错误：快速甩尾和飞机耳通常代表警告，不建议靠近。",
                            fontSize = 13.sp,
                            color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOptionButton(text: String, isCorrect: Boolean, selectedAnswer: String?, onSelect: () -> Unit) {
    val selected = selectedAnswer == text
    val answered = selectedAnswer != null
    
    val bgColor = if (selected && isCorrect) MaterialTheme.colorScheme.primaryContainer 
                  else if (selected && !isCorrect) MaterialTheme.colorScheme.errorContainer
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                  
    val textColor = if (selected && isCorrect) MaterialTheme.colorScheme.onPrimaryContainer 
                  else if (selected && !isCorrect) MaterialTheme.colorScheme.onErrorContainer
                  else MaterialTheme.colorScheme.onSurface

    Button(
        onClick = onSelect,
        enabled = !answered || selected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 14.sp)
            if (selected) {
                Icon(
                    if (isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
