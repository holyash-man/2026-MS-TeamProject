package kr.ac.dongguk.diet.ui.input

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.ui.MainDispatcherRule
import kr.ac.dongguk.diet.ui.TestRepositories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class MealInputViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()
    private val repos = TestRepositories(main.dispatcher)
    private val eps = 1e-9

    private fun newViewModel(handle: SavedStateHandle = SavedStateHandle()) =
        MealInputViewModel(handle, repos.foods, repos.meals, now = { LocalDateTime.parse("2026-11-15T12:30:45") })

    @Test
    fun initialState_usesNowAndSuggestsMealType() {
        val state = newViewModel().uiState.value

        assertEquals(LocalDateTime.parse("2026-11-15T12:30"), state.dateTime) // 초 단위 버림
        assertEquals(MealType.LUNCH, state.mealType)
        assertFalse(state.canSave) // 음식이 없으면 저장 불가
    }

    @Test
    fun timeChange_resuggestsMealType_untilUserChooses() {
        val vm = newViewModel()

        vm.onTimeChange(LocalTime.of(8, 0))
        assertEquals(MealType.BREAKFAST, vm.uiState.value.mealType)

        vm.onMealTypeChange(MealType.SNACK)
        vm.onTimeChange(LocalTime.of(19, 0))
        assertEquals(MealType.SNACK, vm.uiState.value.mealType) // 직접 고른 값 유지
    }

    @Test
    fun searchAndAdd_usesServingSize_andClearsQuery() = runTest {
        val vm = newViewModel()

        vm.onQueryChange("쌀")
        assertEquals(listOf("쌀밥"), vm.uiState.value.searchResults.map { it.name })

        vm.addFood(repos.food(1))
        val state = vm.uiState.value
        assertEquals("210", state.items.single().amountText) // 쌀밥 1인분 210g
        assertEquals("", state.query)
        assertTrue(state.searchResults.isEmpty())
    }

    @Test
    fun preview_sumsValidItems_andInvalidAmountBlocksSave() = runTest {
        val vm = newViewModel()
        vm.addFood(repos.food(1)) // 쌀밥 210g → 210 kcal
        vm.addFood(repos.food(3)) // 김치찌개 100g(1인분 없음) → 100 kcal

        vm.onAmountChange(1, "abc")
        assertEquals(210.0, vm.uiState.value.preview.kcal, eps) // 잘못된 줄은 미리보기에서 제외
        assertFalse(vm.uiState.value.canSave)

        vm.onAmountChange(1, "300")
        val state = vm.uiState.value
        assertEquals(510.0, state.preview.kcal, eps)
        assertEquals(102.0, state.preview.carbs, eps) // (210 + 300) × 0.20
        assertTrue(state.canSave)
    }

    @Test
    fun save_persistsMeal_andReportsSavedId() = runTest {
        val vm = newViewModel()
        vm.addFood(repos.food(1))
        vm.onPhotoChange("content://photo/1")

        vm.save()

        val id = vm.uiState.value.savedMealId!!
        val saved = repos.meals.getMeal(id)!!
        assertEquals(MealType.LUNCH, saved.type)
        // Photo Picker 주소는 저장할 때 앱 내부 보관소로 복사된 주소로 바뀐다
        assertTrue(repos.photos.isStored(saved.photoUri!!))
        assertEquals(listOf(saved.photoUri), repos.photos.stored)
        assertEquals(210.0, saved.items.single().amount, eps)

        vm.onSavedHandled()
        assertNull(vm.uiState.value.savedMealId)
    }

    @Test
    fun editMode_loadsMeal_andSaveUpdatesSameId() = runTest {
        val id = repos.saveMeal("2026-11-14T19:00", MealType.DINNER, 3L to 250.0)
        val vm = newViewModel(SavedStateHandle(mapOf(MealInputViewModel.MEAL_ID_ARG to id)))

        val loaded = vm.uiState.value
        assertTrue(loaded.isEditing)
        assertEquals(MealType.DINNER, loaded.mealType)
        assertEquals("250", loaded.items.single().amountText)

        vm.onAmountChange(0, "120.5")
        vm.save()

        assertEquals(id, vm.uiState.value.savedMealId)
        assertEquals(120.5, repos.meals.getMeal(id)!!.items.single().amount, eps)
    }

    @Test
    fun removeItem_updatesPreview() = runTest {
        val vm = newViewModel()
        vm.addFood(repos.food(1))
        vm.addFood(repos.food(3))

        vm.removeItem(0)

        assertEquals(listOf("김치찌개"), vm.uiState.value.items.map { it.food.name })
        assertEquals(100.0, vm.uiState.value.preview.kcal, eps)
    }
}
