package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.navigation.BottomNavItem
import com.example.myapplication.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel(), launcherOpenVersion: Int = 0) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(launcherOpenVersion) {
        if (launcherOpenVersion > 0) {
            navController.navigate(BottomNavItem.Home.route) {
                popUpTo(BottomNavItem.Home.route) {
                    inclusive = false
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }
    val showBottomBar = currentRoute in listOf(
        BottomNavItem.Home.route, BottomNavItem.Campus.route,
        BottomNavItem.Companion.route, BottomNavItem.Forum.route, BottomNavItem.Profile.route
    )

    Scaffold(
        bottomBar = { if (showBottomBar) CustomBottomNavigationBar(navController) },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) // Handle insets inside screens
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(BottomNavItem.Home.route) { HomeScreen(navController = navController, viewModel = viewModel) }
                composable(BottomNavItem.Campus.route) { CampusScreen(navController = navController, viewModel = viewModel) }
                composable(BottomNavItem.Companion.route) {
                    CompanionScreen(navController = navController, viewModel = viewModel)
                }
                composable(BottomNavItem.Forum.route) {
                    ForumScreen(
                        viewModel = viewModel,
                        onOpenSightingComments = { navController.navigate("forumSightingComments") },
                        onOpenPostDetail = { postId -> navController.navigate("forumPostDetail/$postId") }
                    )
                }
                composable(BottomNavItem.Profile.route) { ProfileScreen(viewModel = viewModel) }
                composable("forumSightingComments") {
                    val uiState by viewModel.uiState.collectAsState()
                    ForumCommentsScreen(
                        title = "目击记录评论",
                        subtitle = "综合体育场南侧片区 · 安全观察讨论",
                        body = "奶牛今天看起来心情不错，在南侧片区晒太阳。请继续保持远观，只补充片区级状态和安全提醒。",
                        comments = uiState.sightingComments,
                        onBackClick = { navController.popBackStack() },
                        onSubmitComment = { viewModel.addSightingComment(it) }
                    )
                }
                composable("forumPostDetail/{postId}") { backStackEntry ->
                    val uiState by viewModel.uiState.collectAsState()
                    val postId = backStackEntry.arguments?.getString("postId")
                    val post = uiState.publishedForumPosts.firstOrNull { it.id == postId }
                    ForumPostDetailScreen(
                        post = post,
                        onBackClick = { navController.popBackStack() },
                        onSubmitComment = { content ->
                            if (post != null) {
                                viewModel.addForumPostComment(post.id, content)
                            }
                        }
                    )
                }
                composable("catProfile") {
                    CatProfileScreen(onBackClick = { navController.popBackStack() }, viewModel = viewModel)
                }
                composable("tasks") {
                    TaskScreen(onBackClick = { navController.popBackStack() }, viewModel = viewModel)
                }
                composable("education") {
                    EducationScreen(onBackClick = { navController.popBackStack() }, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Campus,
        BottomNavItem.Companion,
        BottomNavItem.Forum,
        BottomNavItem.Profile
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp, 
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                spotColor = Color(0x0A383833)
            )
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                BottomNavItemView(item, selected) {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItemView(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else Color(0x80383833)
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = item.title,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal
        )
    }
}
