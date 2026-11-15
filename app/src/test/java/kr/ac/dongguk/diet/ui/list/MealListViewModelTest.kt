@file:OptIn(ExperimentalCoroutinesApi::class)

package kr.ac.dongguk.diet.ui.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.ui.MainDispatcherRule
import kr.ac.dongguk.diet.ui.TestRepositories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MealListViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()
    private val repos = TestRepositories(main.dispatcher)

    @Test
    fun groupsByDate_newestDayFirst_mealsChronologicalWithinDay() = runTest {
        repos.saveMeal("2026-11-15T19:00", MealType.DINNER, 3L to 300.0)   // 300 kcal
        repos.saveMeal("2026-11-15T08:00", MealType.BREAKFAST, 1L to 200.0) // 200 kcal
        repos.saveMeal("2026-11-14T12:00", MealType.LUNCH, 2L to 150.0)     // 150 kcal
        val vm = MealListViewModel(repos.meals)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val days = vm.uiState.value.days
        assertEquals(listOf(LocalDate.parse("2026-11-15"), LocalDate.parse("2026-11-14")), days.map { it.summary.date })
        assertEquals(2, days[0].summary.mealCount)
        assertEquals(500.0, days[0].summary.kcal, 1e-9)
        assertEquals(listOf(MealType.BREAKFAST, MealType.DINNER), days[0].meals.map { it.meal.type })
        assertEquals(300.0, days[0].meals[1].nutrition.kcal, 1e-9)
    }

    @Test
    fun noMeals_isEmptyState() = runTest {
        val vm = MealListViewModel(repos.meals)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertTrue(vm.uiState.value.isEmpty)
    }

    @Test
    fun delete_removesMealFromList() = runTest {
        val id = repos.saveMeal("2026-11-15T12:00", MealType.LUNCH, 1L to 100.0)
        val vm = MealListViewModel(repos.meals)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.delete(id)

        assertTrue(vm.uiState.value.isEmpty)
    }
}
