package com.example.hobbyhabit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hobbyhabit.ui.screens.AddHobbyScreen
import com.example.hobbyhabit.ui.screens.CalendarScreen
import com.example.hobbyhabit.ui.screens.EventsScreen
import com.example.hobbyhabit.ui.screens.HobbyDetailScreen
import com.example.hobbyhabit.ui.screens.HomeScreen
import com.example.hobbyhabit.ui.screens.ProfileScreen
import com.example.hobbyhabit.ui.screens.StatsScreen
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.viewmodel.UserViewModel

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object EventBrowser : Screen("event_browser")
    object Calendar     : Screen("calendar")
    object AddHobby     : Screen("add_hobby")
    object Profile      : Screen("profile")

    object HobbyDetail : Screen("hobby_detail/{hobbyId}") {
        fun createRoute(hobbyId: Int) = "hobby_detail/$hobbyId"
    }
    object Stats : Screen("stats/{hobbyId}") {
        fun createRoute(hobbyId: Int) = "stats/$hobbyId"
    }
    object Events : Screen("events/{hobbyName}/{category}") {
        fun createRoute(hobbyName: String, category: String) =
            "events/${hobbyName.replace("/", "-")}/${category.replace("/", "-")}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    hobbyViewModel: HobbyViewModel,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route,
        modifier         = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel      = hobbyViewModel,
                onAddHobby     = { navController.navigate(Screen.AddHobby.route) },
                onHobbyClick   = { navController.navigate(Screen.HobbyDetail.createRoute(it.id)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.AddHobby.route) {
            AddHobbyScreen(viewModel = hobbyViewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = userViewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Screen.HobbyDetail.route,
            arguments = listOf(navArgument("hobbyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val hobbyId = backStackEntry.arguments!!.getInt("hobbyId")
            HobbyDetailScreen(
                hobbyId      = hobbyId,
                viewModel    = hobbyViewModel,
                onBack       = { navController.popBackStack() },
                onFindEvents = { hobbyName, category ->
                    navController.navigate(Screen.Events.createRoute(hobbyName, category))
                },
                onViewStats  = { navController.navigate(Screen.Stats.createRoute(hobbyId)) }
            )
        }

        composable(
            route     = Screen.Stats.route,
            arguments = listOf(navArgument("hobbyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val hobbyId = backStackEntry.arguments!!.getInt("hobbyId")
            StatsScreen(
                hobbyId  = hobbyId,
                viewModel = hobbyViewModel,
                onBack   = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.Events.route,
            arguments = listOf(
                navArgument("hobbyName") { type = NavType.StringType },
                navArgument("category")  { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val hobbyName = backStackEntry.arguments!!.getString("hobbyName") ?: ""
            val category  = backStackEntry.arguments!!.getString("category")  ?: ""
            EventsScreen(
                hobbyName = hobbyName,
                category  = category,
                viewModel = eventViewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Screen.EventBrowser.route) {
            EventsScreen(
                hobbyName = "All",
                category  = "Music",
                viewModel = eventViewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(viewModel = hobbyViewModel)
        }
    }
}