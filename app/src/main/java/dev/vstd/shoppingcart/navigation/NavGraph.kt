package dev.vstd.shoppingcart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.vstd.shoppingcart.repository.SearchRepositoryImpl
import dev.vstd.shoppingcart.ui.home.HomeScreen
import dev.vstd.shoppingcart.ui.search.SearchScreen
import dev.vstd.shoppingcart.ui.search.SearchViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = SearchViewModel(SearchRepositoryImpl()),
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProductClick = { product ->
                    // TODO: Navigate to product detail
                }
            )
        }
    }
} 