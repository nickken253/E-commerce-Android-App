package com.mustfaibra.roffu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mustfaibra.roffu.screens.home.HomeScreen
import com.mustfaibra.roffu.screens.search.SearchScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                cartOffset = IntOffset(0, 0),
                cartProductsIds = emptyList(),
                bookmarkProductsIds = emptyList(),
                onProductClicked = { /* TODO */ },
                onCartStateChanged = { /* TODO */ },
                onBookmarkStateChanged = { /* TODO */ },
//                onNavigateToSearch = {
//                    navController.navigate(Screen.Search.route)
//                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
//                onNavigateBack = {
//                    navController.popBackStack()
//                }
            )
        }
    }
} 