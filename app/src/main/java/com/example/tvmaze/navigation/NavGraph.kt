package com.example.tvmaze.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tvmaze.ui.detail.DetailScreen
import com.example.tvmaze.ui.detail.DetailViewModel
import com.example.tvmaze.ui.favourites.FavouritesScreen
import com.example.tvmaze.ui.favourites.FavouritesViewModel
import com.example.tvmaze.ui.list.ListScreen
import com.example.tvmaze.ui.list.ListViewModel

sealed class Screen(val route: String, val title: String) {
    object List : Screen("list", "Сериалы")
    object Favourites : Screen("favourites", "Избранное")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(Screen.List, Screen.Favourites)

                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(screen.route) {
                                // Упрощённая навигация без saveState/restoreState
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                if (screen == Screen.List) Icons.Default.Home else Icons.Default.Favorite,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.List.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.List.route) {
                val viewModel: ListViewModel = hiltViewModel()
                ListScreen(
                    onShowClick = { showId ->
                        navController.navigate("detail/$showId")
                    },
                    viewModel = viewModel
                )
            }

            composable(Screen.Favourites.route) {
                val viewModel: FavouritesViewModel = hiltViewModel()
                FavouritesScreen(
                    onShowClick = { showId ->
                        navController.navigate("detail/$showId")
                    },
                    viewModel = viewModel
                )
            }

            composable(
                route = "detail/{showId}",
                arguments = listOf(
                    navArgument("showId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getInt("showId") ?: return@composable
                val viewModel: DetailViewModel = hiltViewModel()
                DetailScreen(
                    showId = showId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}