package com.example.myapplication.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.utils.sharePlainText
import com.example.myapplication.ui.viewmodel.ForumCommentState
import com.example.myapplication.ui.viewmodel.ForumPostState
import com.example.myapplication.ui.viewmodel.MainViewModel

@Composable
fun ForumScreen(
    viewModel: MainViewModel? = null,
    onOpenSightingComments: () -> Unit = {},
    onOpenPostDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var showPostDialog by remember { mutableStateOf(false) }
    var selectedRecognition by remember { mutableStateOf("本校") }
    var selectedCategory by remember { mutableStateOf("目击记录") }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showForumNotifications by remember { mutableStateOf(false) }
    val visiblePosts = uiState?.publishedForumPosts.orEmpty()
        .filter { it.source == selectedRecognition }
        .filter { selectedCategory == "全部" || it.category == selectedCategory }
    val showLocalCards = selectedRecognition == "本校"
    val hasLocalCardContent = showLocalCards && selectedCategory in listOf("全部", "组队活动", "知识分享", "求助信息", "目击记录", "猫咪日记", "片区记录")

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(112.dp))
            ForumRecognitionRow(
                selectedLabel = selectedRecognition,
                onSelected = {
                    selectedRecognition = it
                    selectedCategory = "全部"
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            CategoryChipsRow(selectedLabel = selectedCategory, onSelected = { selectedCategory = it })
            Spacer(modifier = Modifier.height(12.dp))

            PublishedPostsSection(
                posts = visiblePosts,
                onOpenPost = { onOpenPostDetail(it.id) },
                onLikePost = { viewModel?.toggleForumPostLike(it.id) },
                onCollectPost = { viewModel?.toggleForumPostCollection(it.id) },
                onOpenComments = { onOpenPostDetail(it.id) }
            )

            if (showLocalCards && (selectedCategory == "全部" || selectedCategory == "组队活动")) {
                TeamEventCard(
                    joined = uiState?.joinedWeekendShelterEvent == true,
                    onToggleJoin = { viewModel?.toggleWeekendShelterEvent() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showLocalCards && (selectedCategory == "全部" || selectedCategory == "知识分享")) {
                KnowledgeShareCard()
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showLocalCards && (selectedCategory == "全部" || selectedCategory == "求助信息")) {
                EmergencyForumCard(
                    responded = uiState?.hasJoinedEmergencyQueue == true,
                    onJoin = { viewModel?.joinEmergencyQueue() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showLocalCards && (selectedCategory == "全部" || selectedCategory == "目击记录")) {
                SightingForumCard(
                    liked = uiState?.sightingLiked == true,
                    commentCount = uiState?.sightingComments?.size ?: 3,
                    onToggleLike = { viewModel?.toggleSightingLike() },
                    onOpenDetail = onOpenSightingComments,
                    onOpenComments = onOpenSightingComments,
                    onShare = {
                        sharePlainText(
                            context = context,
                            chooserTitle = "分享目击记录",
                            subject = "喵伴云养目击记录",
                            body = "目击：奶牛在操场南侧片区安静晒太阳。请保持距离，不围观、不追逐，不公开精确位置。"
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showLocalCards && (selectedCategory == "全部" || selectedCategory == "猫咪日记" || selectedCategory == "片区记录")) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (selectedCategory == "全部" || selectedCategory == "猫咪日记") {
                        DiaryPolaroidCard(modifier = Modifier.weight(1f))
                    }
                    if (selectedCategory == "全部" || selectedCategory == "片区记录") {
                        MapPostCard(modifier = Modifier.weight(1f))
                    }
                }
            }

            if (visiblePosts.isEmpty() && !hasLocalCardContent) {
                ForumEmptyState(source = selectedRecognition, category = selectedCategory)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
        ForumTopAppBar(onSearchClick = { showSearchDialog = true }, onNotificationsClick = { showForumNotifications = true })

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 120.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { showPostDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Post", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
        }
        if (showPostDialog) {
            ForumPostDialog(
                onDismiss = { showPostDialog = false },
                onPublish = { category, title, content ->
                    viewModel?.publishForumPost(category, title, content)
                    showPostDialog = false
                }
            )
        }
        if (showSearchDialog) {
            ForumSearchDialog(
                posts = uiState?.publishedForumPosts.orEmpty(),
                onDismiss = { showSearchDialog = false },
                onOpenPost = {
                    showSearchDialog = false
                    onOpenPostDetail(it.id)
                }
            )
        }
        if (showForumNotifications) {
            ForumNotificationsDialog(
                postCount = uiState?.publishedForumPosts?.size ?: 0,
                commentCount = uiState?.sightingComments?.size ?: 0,
                hasJoinedEmergencyQueue = uiState?.hasJoinedEmergencyQueue == true,
                onDismiss = { showForumNotifications = false }
            )
        }
    }
}

@Composable
fun ForumTopAppBar(onSearchClick: () -> Unit, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                Image(
                    painter = painterResource(R.drawable.img_net_b6f8927693),
                    contentDescription = "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("共护", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ForumRecognitionRow(selectedLabel: String, onSelected: (String) -> Unit) {
    val tabs = listOf("本校", "周边学校", "社区内容流")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tabs) { tab ->
            val selected = selectedLabel == tab
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else SurfaceContainerHighest)
                    .clickable { onSelected(tab) }
                    .padding(horizontal = 20.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun CategoryChipsRow(selectedLabel: String, onSelected: (String) -> Unit) {
    data class Chip(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)
    val chips = listOf(
        Chip(Icons.Outlined.Search, "目击记录"),
        Chip(Icons.Outlined.Group, "组队活动"),
        Chip(Icons.AutoMirrored.Outlined.MenuBook, "知识分享"),
        Chip(Icons.Outlined.Info, "求助信息"),
        Chip(Icons.Outlined.Star, "经验分享"),
        Chip(Icons.Outlined.Home, "猫咪日记"),
        Chip(Icons.Outlined.Place, "片区记录"),
        Chip(Icons.Outlined.Home, "全部")
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chips) { chip ->
            val selected = selectedLabel == chip.label
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.secondaryContainer else SurfaceContainerHighest
                    )
                    .clickable { onSelected(chip.label) }
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(chip.icon, contentDescription = chip.label, tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(chip.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SafePostingNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("安全发帖默认规则", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("只写片区、时段和状态，不写精确点位、路线或实时追踪。", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun PublishedPostsSection(
    posts: List<ForumPostState>,
    onOpenPost: (ForumPostState) -> Unit,
    onLikePost: (ForumPostState) -> Unit,
    onCollectPost: (ForumPostState) -> Unit,
    onOpenComments: (ForumPostState) -> Unit
) {
    if (posts.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text("我的最新共护发布", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        posts.forEach { post ->
            PublishedPostCard(
                post = post,
                onOpenPost = { onOpenPost(post) },
                onLikePost = { onLikePost(post) },
                onCollectPost = { onCollectPost(post) },
                onOpenComments = { onOpenComments(post) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ForumEmptyState(source: String, category: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Text("暂无匹配内容", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "$source · $category 还没有新的共护动态，可以切换分类或发布一条记录。",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun PublishedPostCard(
    post: ForumPostState,
    onOpenPost: () -> Unit,
    onLikePost: () -> Unit,
    onCollectPost: () -> Unit,
    onOpenComments: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPost() },
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(post.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Box(modifier = Modifier.background(SurfaceContainerHigh, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(post.source, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(post.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(post.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(post.content, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.clickable { onLikePost() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(if (post.liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "点赞", tint = if (post.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text(if (post.liked) "1" else "0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.clickable { onOpenComments() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "评论", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text(post.comments.size.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.clickable { onCollectPost() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Star, contentDescription = "收藏", tint = if (post.collected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    Text(if (post.collected) "已收藏" else "收藏", fontSize = 12.sp, color = if (post.collected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun ForumSearchDialog(posts: List<ForumPostState>, onDismiss: () -> Unit, onOpenPost: (ForumPostState) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = posts.filter { post ->
        query.isBlank() || post.title.contains(query, ignoreCase = true) || post.content.contains(query, ignoreCase = true) || post.category.contains(query, ignoreCase = true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("搜索社区内容", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("关键词") },
                    placeholder = { Text("搜索猫咪、补水、目击或任务") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(modifier = Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (results.isEmpty()) {
                        Text("未找到相关内容", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        results.forEach { post ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceContainerLow).clickable { onOpenPost(post) }.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(post.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(post.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss, shape = CircleShape) { Text("完成") } }
    )
}

@Composable
fun ForumNotificationsDialog(postCount: Int, commentCount: Int, hasJoinedEmergencyQueue: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("共护提醒", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CommentNoticeRow(Icons.AutoMirrored.Outlined.Send, "目击评论", "奶牛目击记录已有 $commentCount 条安全观察评论。")
                CommentNoticeRow(Icons.Outlined.Edit, "共护内容流", "本次会话中已有 $postCount 条片区记录和示例对象。")
                CommentNoticeRow(Icons.Outlined.Warning, "求助响应", if (hasJoinedEmergencyQueue) "你已加入医疗求助协助队列。" else "教三片区仍有医疗求助可响应。")
            }
        },
        confirmButton = { Button(onClick = onDismiss, shape = CircleShape) { Text("知道了") } }
    )
}

@Composable
fun CommentNoticeRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmergencyForumCard(
    responded: Boolean,
    onJoin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            shape = RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp, topEnd = 16.dp, bottomStart = 16.dp),
            border = BorderStroke(2.dp, Color(0xFFDE7D70))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("教三教学楼后的「大橘」疑似腿部受伤，需志愿者确认", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 26.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text("15分钟前发布", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))) {
                        Image(painter = painterResource(R.drawable.img_net_ed8f952dc5), contentDescription = "Injured Cat", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text("第三教学楼后侧片区", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Text("关联：大橘 (Orange)", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("高优先级", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Box(modifier = Modifier.background(SurfaceContainerHigh, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(if (responded) "已响应" else "待响应", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFCBD5E0)).border(2.dp, Color.White, CircleShape))
                        Box(modifier = Modifier.size(28.dp).offset(x = (-8).dp).clip(CircleShape).background(Color(0xFFA0AEC0)).border(2.dp, Color.White, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+12 人正在关注", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onJoin,
                        enabled = !responded,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (responded) "已加入" else "我也能帮忙", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = (-12).dp)
                .background(Color(0xFFDE7D70), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("紧急 · 医疗求助", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun SightingForumCard(
    liked: Boolean,
    commentCount: Int,
    onToggleLike: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail() },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceContainerHighest), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text("目击：奶牛在操场出没", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("刚刚 · 综合体育场南侧片区", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp))) {
                    Image(painter = painterResource(R.drawable.img_net_9755ae2cc8), contentDescription = "Cat Sighting", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text("奶牛 (Tuxedo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Text("奶牛今天看起来心情不错，在南侧片区晒太阳。建议只做远观记录，不围观、不补充零食。", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(modifier = Modifier.clickable { onToggleLike() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(if (liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(if (liked) "25" else "24", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.clickable { onOpenComments() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Comment", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(commentCount.toString(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).clickable { onShare() }
                )
            }
        }
    }
}

@Composable
fun ForumPostDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, String) -> Unit
) {
    val categories = listOf("目击记录", "组队活动", "知识分享", "求助信息", "经验分享", "猫咪日记", "片区记录")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val canPublish = title.trim().isNotEmpty() && content.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发布社区内容", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("选择内容类型", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        val selected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primary else SurfaceContainerHigh)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    placeholder = { Text("例如：图书馆北侧片区发现奶油在休息") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("正文") },
                    placeholder = { Text("描述观察到的状态和照护建议，避免写精确点位。") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Text("发布内容会显示在本次会话的社区内容流中；请只写片区、时段和状态，避免精确点位、路线或实时追踪。", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPublish(selectedCategory, title, content) },
                enabled = canPublish,
                shape = CircleShape
            ) {
                Text("发布")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun SightingDetailDialog(
    onDismiss: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("目击详情", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(14.dp))) {
                    Image(painter = painterResource(R.drawable.img_net_9755ae2cc8), contentDescription = "奶牛目击图", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("奶牛在综合体育场南侧安静晒太阳", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("刚刚 · 综合体育场南侧片区 · 远观记录", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("现场状态稳定，没有明显受伤或应激。建议保持 3 米以上距离，只记录片区动态，不聚集围观，不补充零食。", fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text("为保护猫咪安全，详情页仅展示片区与状态，不展示精确坐标和路线。", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(onClick = onShare, label = { Text("分享") }, leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp)) })
                    AssistChip(onClick = onOpenComments, label = { Text("评论") }, leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp)) })
                }
            }
        },
        confirmButton = {
            Button(onClick = onOpenComments, shape = CircleShape) { Text("查看评论") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun UserPostDetailDialog(
    post: ForumPostState,
    onDismiss: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(post.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(post.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text("${post.author} · ${post.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(post.content, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(onClick = onShare, label = { Text("分享") }, leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp)) })
                    AssistChip(onClick = onOpenComments, label = { Text("${post.comments.size} 条评论") }, leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp)) })
                }
            }
        },
        confirmButton = {
            Button(onClick = onOpenComments, shape = CircleShape) { Text("评论") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun ForumPostDetailScreen(
    post: ForumPostState?,
    onBackClick: () -> Unit,
    onSubmitComment: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val canSubmit = draft.trim().isNotEmpty() && post != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "返回", tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("帖子详情", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(post?.let { "${it.category} · ${it.author} · ${it.time}" } ?: "帖子内容暂不可用", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (post == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceContainerHighest), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("帖子不存在或已被移除", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = onBackClick, shape = CircleShape) { Text("返回论坛") }
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text(post.category, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Box(modifier = Modifier.background(SurfaceContainerHigh, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text(post.source, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                                Text("片区级内容", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(post.title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 28.sp)
                            Text("${post.author} · ${post.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(post.content, fontSize = 15.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.56f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Text("为保护校园猫咪，帖子详情仅展示片区与状态，不公开精确坐标、路线或实时追踪。", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(if (post.liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (post.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Text(if (post.liked) "1" else "0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("${post.comments.size} 条评论", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            if (post.collected) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Icon(Icons.Outlined.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                    Text("已收藏", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Text("评论讨论", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Box(modifier = Modifier.background(SurfaceContainerHigh, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("${post.comments.size} 条", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (post.comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLow)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("还没有评论，写下第一条友善提醒吧。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                items(post.comments) { comment ->
                    CommentCard(comment = comment)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("写下友善提醒或状态补充…") },
                    minLines = 1,
                    maxLines = 3,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Button(
                    onClick = {
                        onSubmitComment(draft)
                        draft = ""
                    },
                    enabled = canSubmit,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("发送")
                }
            }
        }
    }
}

@Composable
fun ForumCommentsScreen(
    title: String,
    subtitle: String,
    body: String?,
    comments: List<ForumCommentState>,
    onBackClick: () -> Unit,
    onSubmitComment: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val canSubmit = draft.trim().isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "返回", tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!body.isNullOrBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SurfaceContainerLow)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("原帖内容", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(body, fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("评论内容", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${comments.size} 条", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLow)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("还没有评论，写下第一条友善提醒吧。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                items(comments) { comment ->
                    CommentCard(comment = comment)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLowest)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("写评论") },
                placeholder = { Text("补充观察、提醒保持距离、记录状态变化…") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onSubmitComment(draft)
                    draft = ""
                },
                enabled = canSubmit,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("发送")
            }
        }
    }
}

@Composable
fun CommentsDialog(
    title: String,
    comments: List<ForumCommentState>,
    onDismiss: () -> Unit,
    onSubmitComment: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val canSubmit = draft.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (comments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(18.dp), contentAlignment = Alignment.Center) {
                            Text("还没有评论，写下第一条友善提醒吧。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        comments.forEach { comment ->
                            CommentItem(comment = comment)
                        }
                    }
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("写评论") },
                    placeholder = { Text("补充观察、提醒保持距离、记录状态变化…") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitComment(draft)
                    draft = ""
                },
                enabled = canSubmit,
                shape = CircleShape
            ) {
                Text("发送")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun CommentItem(comment: ForumCommentState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text(comment.author.take(1), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(comment.author, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(comment.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(comment.content, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CommentCard(comment: ForumCommentState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.author.take(1), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(comment.author, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(comment.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(comment.content, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DiaryPolaroidCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .rotate(-1f)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                Image(painter = painterResource(R.drawable.img_net_3b0696c582), contentDescription = "Diary Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("猫咪日记 · 2023.10.24", fontSize = 9.sp, color = Color.White)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Text("图书馆的「三花」进入冬眠模式", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("今天去借书发现她在三楼社科区睡得很死，大家轻声点哦...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                Text("#宁静校园 #三花", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun MapPostCard(modifier: Modifier = Modifier) {
    var synced by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.clickable { synced = !synced },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Box(modifier = Modifier.background(SurfaceContainerHighest, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("片区动态", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(if (synced) "区域照护提醒已读" else "更新了校园区域照护提醒", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(if (synced) "片区动态已标记为已读，可在校园地图查看片区建议。" else "根据近期远观记录，志愿者更新了若干片区的补水与不打扰建议，请以区域提示为准。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(SurfaceContainerHighest)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(if (synced) 1f else 0.68f).background(MaterialTheme.colorScheme.secondary, CircleShape))
                }
                Text(if (synced) "已同步" else "协作完成 68%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun TeamEventCard(joined: Boolean, onToggleJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("组队：周末自制猫窝换新活动", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("1小时前 · 志愿者协会", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(if (joined) "已报名" else "招募中", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Text("天气渐冷，本周末(10.28)下午1点在学生活动中心集合，利用回收旧衣物制作保暖猫窝，预计需要 5-8 人参与，欢迎报名！", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                    Text("10月28日 13:00", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
                Button(
                    onClick = onToggleJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (joined) "取消报名" else "立即报名", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun KnowledgeShareCard() {
    var collected by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                }
                Column {
                Text("科普：秋季猫咪易发疾病及安全观察", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("3小时前 · 知识分享", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("秋季气温变化大，校园流浪猫容易出现上呼吸道问题。请通过远观眼鼻分泌物、精神状态和步态做初步记录，不自行用药或抓捕...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("#医疗科普", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                    Text("#秋季护理", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                }
                Row(
                    modifier = Modifier.clickable { collected = !collected },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Star, contentDescription = null, tint = if (collected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    Text(if (collected) "已收藏 35" else "收藏 34", fontSize = 12.sp, color = if (collected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
