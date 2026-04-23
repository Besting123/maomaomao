package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Home : BottomNavItem("首页", Icons.Outlined.Home, "home")
    object Campus : BottomNavItem("校园", Icons.Outlined.LocationOn, "campus")
    object Feed : BottomNavItem("投喂", Icons.Outlined.ShoppingCart, "feed")
    object Forum : BottomNavItem("论坛", Icons.Outlined.Email, "forum")
    object Profile : BottomNavItem("我的", Icons.Outlined.Person, "profile")
}
