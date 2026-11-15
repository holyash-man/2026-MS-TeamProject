package kr.ac.dongguk.diet.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.domain.calc.NutritionCalculator
import kr.ac.dongguk.diet.domain.model.MacroRatio
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.Nutrition
import kr.ac.dongguk.diet.domain.model.macroRatio

/** 상세 화면의 음식 한 줄: 입력했던 음식·섭취량과 그 영양성분. */
data class MealItemRow(val item: MealItem, val nutrition: Nutrition)

data class MealDetailUiState(
    val isLoading: Boolean = true,
    val meal: Meal? = null,
    val items: List<MealItemRow> = emptyList(),
    /** 끼니 전체 영양소·열량 섭취량. */
    val total: Nutrition = Nutrition.ZERO,
    val macroRatio: MacroRatio = MacroRatio.ZERO,
    /** 삭제가 끝나면 true. 화면은 이 값을 보고 뒤로 간다. */
    val deleted: Boolean = false,
) {
    val notFound: Boolean get() = !isLoading && meal == null && !deleted
}

/** 식사 상세 화면. 내비게이션 인자 [MEAL_ID_ARG] 로 끼니 id 를 받는다. */
class MealDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val mealRepository: MealRepository,
) : ViewModel() {

    private val mealId: Long = checkNotNull(savedStateHandle.get<Long>(MEAL_ID_ARG)) { "mealId 인자가 필요함" }
    private val deleted = MutableStateFlow(false)

    val uiState: StateFlow<MealDetailUiState> =
        combine(mealRepository.observeMeals(), deleted) { meals, isDeleted ->
            val meal = meals.firstOrNull { it.id == mealId }
            if (meal == null) {
                MealDetailUiState(isLoading = false, deleted = isDeleted)
            } else {
                val total = NutritionCalculator.forMeal(meal)
                MealDetailUiState(
                    isLoading = false,
                    meal = meal,
                    items = meal.items.map { MealItemRow(it, NutritionCalculator.forItem(it)) },
                    total = total,
                    macroRatio = total.macroRatio(),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealDetailUiState())

    fun delete() {
        viewModelScope.launch {
            mealRepository.deleteMeal(mealId)
            deleted.value = true
        }
    }

    companion object {
        const val MEAL_ID_ARG = "mealId"
    }
}
