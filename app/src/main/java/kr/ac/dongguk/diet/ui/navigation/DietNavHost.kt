package kr.ac.dongguk.diet.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.ac.dongguk.diet.ui.analysis.AnalysisScreen
import kr.ac.dongguk.diet.ui.input.MealInputScreen
import kr.ac.dongguk.diet.ui.list.MealListScreen

/** 하단 탭 3개: 입력 / 목록 / 분석. 상세 화면 등은 B가 route를 추가한다. */
enum class TopLevelRoute(val route: String, val label: String, val icon: ImageVector) {
    INPUT("input", "기록", Icons.Filled.AddCircle),
    LIST("list", "식사 목록", Icons.AutoMirrored.Filled.List),
    ANALYSIS("analysis", "분석", Icons.Filled.BarChart),
}

@Composable
fun DietNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelRoute.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelRoute.LIST.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelRoute.INPUT.route) { MealInputScreen() }
            composable(TopLevelRoute.LIST.route) { MealListScreen() }
            composable(TopLevelRoute.ANALYSIS.route) { AnalysisScreen() }
        }
    }
}
