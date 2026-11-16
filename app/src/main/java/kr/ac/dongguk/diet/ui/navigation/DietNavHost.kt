package kr.ac.dongguk.diet.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kr.ac.dongguk.diet.ui.analysis.AnalysisScreen
import kr.ac.dongguk.diet.ui.detail.MealDetailScreen
import kr.ac.dongguk.diet.ui.input.MealInputScreen
import kr.ac.dongguk.diet.ui.list.MealListScreen
import kotlin.reflect.KClass

/** 하단 탭 3개: 기록 / 식사 목록 / 분석. */
enum class TopLevelRoute(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
) {
    INPUT(InputRoute(), InputRoute::class, "기록", Icons.Filled.AddCircle),
    LIST(ListRoute, ListRoute::class, "식사 목록", Icons.AutoMirrored.Filled.List),
    ANALYSIS(AnalysisRoute, AnalysisRoute::class, "분석", Icons.Filled.BarChart),
}

/**
 * 화면 이동 구조.
 * - 하단 탭: 목록(시작) · 기록 · 분석
 * - 목록 → 상세(DetailRoute) → 수정(InputRoute(mealId))
 * - 새 기록 저장 → 그 끼니의 상세로 이동(입력 화면은 스택에서 빼서 다음 기록은 빈 칸으로 시작)
 * - 수정 저장 → 상세로 돌아감
 * 상세·수정 화면에서는 하단 탭을 숨긴다.
 */
@Composable
fun DietNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val isEditing = destination?.hasRoute(InputRoute::class) == true &&
        backStackEntry?.toRoute<InputRoute>()?.mealId != 0L
    val showBottomBar = destination != null && !isEditing &&
        TopLevelRoute.entries.any { destination.hasRoute(it.routeClass) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelRoute.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = destination.isOn(tab),
                            onClick = { navController.navigateToTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ListRoute,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable<ListRoute> {
                MealListScreen(onMealClick = { id -> navController.navigate(DetailRoute(id)) })
            }
            composable<InputRoute> { entry ->
                val route = entry.toRoute<InputRoute>()
                MealInputScreen(
                    onSaved = { id, wasEditing ->
                        if (wasEditing) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(DetailRoute(id)) {
                                popUpTo<InputRoute> { inclusive = true }
                            }
                        }
                    },
                    onBack = if (route.mealId != 0L) ({ navController.popBackStack() }) else null,
                )
            }
            composable<DetailRoute> {
                MealDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(InputRoute(mealId = id)) },
                )
            }
            composable<AnalysisRoute> { AnalysisScreen() }
        }
    }
}

private fun NavDestination?.isOn(tab: TopLevelRoute): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

private fun NavHostController.navigateToTab(tab: TopLevelRoute) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
