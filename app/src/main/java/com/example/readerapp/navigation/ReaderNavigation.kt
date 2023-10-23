package com.example.readerapp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readerapp.screens.ReaderSplashScreen
import com.example.readerapp.screens.details.BookDetailsScreen
import com.example.readerapp.screens.home.Home
import com.example.readerapp.screens.home.ReaderHomeScreenViewModel
import com.example.readerapp.screens.login.ReaderLoginScreen
import com.example.readerapp.screens.search.ReaderSearchScreen
import com.example.readerapp.screens.search.ReaderSearchScreenViewModel
import com.example.readerapp.screens.stats.ReaderStatsScreen
import com.example.readerapp.screens.update.ReaderUpdateScreen

@Composable
fun ReaderNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ReaderScreens.SplashScreen.name) {
        composable(ReaderScreens.SplashScreen.name) {
            ReaderSplashScreen(navController)
        }

        composable(ReaderScreens.ReaderHomeScreen.name) {
            val homeViewModel = hiltViewModel<ReaderHomeScreenViewModel>()
            Home(navController, homeViewModel)
        }

        composable(ReaderScreens.LoginScreen.name) {
            ReaderLoginScreen(navController)
        }

        composable(ReaderScreens.ReaderStatsScreen.name) {
            ReaderStatsScreen(navController)
        }

        composable(ReaderScreens.SearchScreen.name) {
            val searchViewModel = hiltViewModel<ReaderSearchScreenViewModel>()
            ReaderSearchScreen(navController, searchViewModel)
        }

        val detailsName = ReaderScreens.DetailsScreen.name
        composable("$detailsName/{bookId}", arguments = listOf(navArgument("bookId") {
            type = NavType.StringType
        })) { backStackEntry ->
            backStackEntry.arguments?.getString("bookId")?.let {
                BookDetailsScreen(navController, it)
            }
        }

        val updateName = ReaderScreens.UpdateScreen.name
        composable("$updateName/{bookItemId}", arguments = listOf(navArgument("bookItemId") {
            type = NavType.StringType
        })) { navBackStackEntry ->
            navBackStackEntry.arguments?.getString("bookItemId")?.let { bookItemId ->
                ReaderUpdateScreen(navController, bookItemId)
            }
        }

    }
}