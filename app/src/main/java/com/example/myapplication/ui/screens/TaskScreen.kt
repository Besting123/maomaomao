package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import com.example.myapplication.ui.viewmodel.MainViewModel
import com.example.myapplication.ui.viewmodel.TaskState
import com.example.myapplication.ui.viewmodel.TaskType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(onBackClick: () -> Unit, viewModel: MainViewModel? = null) {
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    val allTasks = uiState?.tasks ?: emptyList()
    val completedCount = allTasks.count { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务中心", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SignInSection(
                signInDays = uiState?.signInDays ?: 3,
                hasSignedInToday = uiState?.hasSignedInToday ?: false,
                onSignIn = {
                    viewModel?.signIn()
                    feedbackMessage = "签到成功：获得 10 小鱼干，连续参与会提升陪伴等级。"
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            TaskOverviewSection(
                tokenBalance = uiState?.tokenBalance ?: 350,
                completedCount = completedCount,
                totalCount = allTasks.size
            )
            Spacer(modifier = Modifier.height(32.dp))
            TaskCategorySection(
                title = "日常陪伴任务",
                tasks = allTasks.filter { it.type == TaskType.DAILY },
                onTaskClick = { task ->
                    viewModel?.completeTask(task.id)
                    feedbackMessage = "已完成「${task.title}」：+${task.reward} 小鱼干。"
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            TaskCategorySection(
                title = "学习任务",
                tasks = allTasks.filter { it.type == TaskType.LEARNING },
                onTaskClick = { task ->
                    viewModel?.completeTask(task.id)
                    feedbackMessage = "已完成「${task.title}」：+${task.reward} 小鱼干。"
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            TaskCategorySection(
                title = "特殊成就",
                tasks = allTasks.filter { it.type == TaskType.SPECIAL },
                onTaskClick = { task ->
                    viewModel?.completeTask(task.id)
                    feedbackMessage = "已完成「${task.title}」：+${task.reward} 小鱼干。"
                }
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    feedbackMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null },
            confirmButton = {
                TextButton(onClick = { feedbackMessage = null }) {
                    Text("知道了")
                }
            },
            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
            title = { Text("任务反馈", fontWeight = FontWeight.Bold) },
            text = { Text(message, lineHeight = 22.sp) }
        )
    }
}

@Composable
fun SignInSection(signInDays: Int, hasSignedInToday: Boolean, onSignIn: () -> Unit) {
    val remainingDays = if (signInDays >= 7) 0 else 7 - signInDays
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("本周已连续签到 $signInDays 天", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(if (remainingDays == 0) "本周连续签到奖励已达成" else "再签到 $remainingDays 天可额外获得 50 小鱼干", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (hasSignedInToday) MaterialTheme.colorScheme.primary else Color.White)
                        .then(if (!hasSignedInToday) Modifier.clickable { onSignIn() } else Modifier)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if (hasSignedInToday) "已签到" else "去签到", color = if (hasSignedInToday) Color.White else MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (1..7).forEach { day ->
                    val isPastOrToday = day <= signInDays
                    val isToday = day == signInDays && hasSignedInToday || (day == signInDays + 1 && !hasSignedInToday)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isPastOrToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPastOrToday) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("+10", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (isToday) "今天" else "$day 天", fontSize = 12.sp, color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f), fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskOverviewSection(tokenBalance: Int, completedCount: Int, totalCount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TaskStatCard(
            title = "小鱼干余额",
            value = tokenBalance.toString(),
            subtitle = "可用于云陪伴互动",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        TaskStatCard(
            title = "任务进度",
            value = "$completedCount/$totalCount",
            subtitle = "今日前端模拟状态",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TaskStatCard(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun TaskCategorySection(title: String, tasks: List<TaskState>, onTaskClick: (TaskState) -> Unit) {
    if (tasks.isEmpty()) return
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tasks.forEach { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (task.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Pets, contentDescription = null, tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(task.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(task.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        if (task.isCompleted) {
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("已领取", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onTaskClick(task) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+${task.reward} 小鱼干", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
