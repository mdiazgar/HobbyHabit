package com.example.hobbyhabit.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hobbyhabit.ui.screens.AddHobbyScreen
import com.example.hobbyhabit.ui.screens.EventsScreen
import com.example.hobbyhabit.ui.screens.HobbyDetailScreen
import com.example.hobbyhabit.ui.screens.HomeScreen
import com.example.hobbyhabit.ui.screens.ProfileScreen
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.viewmodel.UserViewModel

sealed class Screen(val route: String) {

    // MAIN TABS
    object Home : Screen("home")
    object EventBrowser : Screen("event_browser")
    object MyEvents : Screen("my_events")

    // FLOW SCREENS
    object AddHobby : Screen("add_hobby")
    object Profile : Screen("profile")

    object HobbyDetail : Screen("hobby_detail/{hobbyId}") {
        fun createRoute(hobbyId: Int) = "hobby_detail/$hobbyId"
    }

    object Events : Screen("events/{hobbyName}") {
        fun createRoute(hobbyName: String?) = "events/$hobbyName"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    hobbyViewModel: HobbyViewModel,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // ---------------- HOME TAB ----------------
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = hobbyViewModel,
                onAddHobby = { navController.navigate(Screen.AddHobby.route) },
                onHobbyClick = { hobby ->
                    navController.navigate(Screen.HobbyDetail.createRoute(hobby.id))
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.AddHobby.route) {
            AddHobbyScreen(
                viewModel = hobbyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HobbyDetail.route,
            arguments = listOf(navArgument("hobbyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val hobbyId = backStackEntry.arguments!!.getInt("hobbyId")

            HobbyDetailScreen(
                hobbyId = hobbyId,
                viewModel = hobbyViewModel,
                onBack = { navController.popBackStack() },

                onFindEvents = { hobbyName ->
                    navController.navigate(Screen.Events.createRoute(hobbyName))
                }
            )
        }

        composable(
            route = Screen.Events.route,
            arguments = listOf(navArgument("hobbyName") { type = NavType.StringType })
        ) { backStackEntry ->
            val hobbyName = backStackEntry.arguments!!.getString("hobbyName") ?: ""
            EventsScreen(
                hobbyName = hobbyName,
                viewModel = eventViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- EVENT TAB (GLOBAL) ----------------
        composable(Screen.EventBrowser.route) {
            EventsScreen(
                hobbyName = "", // better design
                viewModel = eventViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- MY EVENTS TAB ----------------
        composable(Screen.MyEvents.route) {
            Text("My Events (Coming Soon)")
        }
    }
}
