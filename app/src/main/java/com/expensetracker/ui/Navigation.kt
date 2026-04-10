package com.expensetracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensetracker.ui.add.AddEditScreen
import com.expensetracker.ui.home.HomeScreen
import com.expensetracker.ui.settings.SettingsScreen
import com.expensetracker.ui.stats.StatsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEdit : Screen("add_edit?transactionId={transactionId}") {
        fun createRoute(transactionId: Long = -1L) =
            if (transactionId > 0) "add_edit?transactionId=$transactionId" else "add_edit"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {

        composable(Screen.Home.route) {
            HomeScreen(
                onAddTransaction = { navController.navigate(Screen.AddEdit.createRoute()) },
                onEditTransaction = { id -> navController.navigate(Screen.AddEdit.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
