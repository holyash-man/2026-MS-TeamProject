@file:OptIn(ExperimentalCoroutinesApi::class)

package kr.ac.dongguk.diet.ui.detail

import androidx.lifecycle.SavedStateHandle
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

class MealDetailViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()
    private val repos = TestRepositories(main.dispatcher)

    private fun kotlinx.coroutines.test.TestScope.viewModelFor(id: Long): MealDetailViewModel {
        val vm = MealDetailViewModel(SavedStateHandle(mapOf(MealDetailViewModel.MEAL_ID_ARG to id)), repos.meals)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun showsItemsWithNutrition_andMealTotal() = runTest {
        val id = repos.saveMeal("2026-11-15T12:00", MealType.LUNCH, 1L to 210.0, 5L to 350.0)

        val state = viewModelFor(id).uiState.value

        assertEquals(listOf("쌀밥", "아메리카노"), state.items.map { it.item.food.name })
        assertEquals(210.0, state.items[0].nutrition.kcal, 1e-9)
        assertEquals(14.0, state.items[1].nutrition.kcal, 1e-9) // 4kcal/100ml × 350ml
        // 쌀밥 210g + 아메리카노 350ml → 탄 (210+350)×0.2 = 112 / 단 28 / 지 5.6
        val t = state.total
        assertEquals(224.0, t.kcal, 1e-9)
        assertEquals(112.0, t.carbs, 1e-9)
        assertEquals(28.0, t.protein, 1e-9)
        assertEquals(5.6, t.fat, 1e-9)
    }

    @Test
    fun unknownId_isNotFound() = runTest {
        assertTrue(viewModelFor(999).uiState.value.notFound)
    }

    @Test
    fun delete_marksDeleted() = runTest {
        val id = repos.saveMeal("2026-11-15T12:00", MealType.LUNCH, 1L to 100.0)
        val vm = viewModelFor(id)

        vm.delete()

        assertTrue(vm.uiState.value.deleted)
        assertTrue(repos.meals.getMeal(id) == null)
    }
}
