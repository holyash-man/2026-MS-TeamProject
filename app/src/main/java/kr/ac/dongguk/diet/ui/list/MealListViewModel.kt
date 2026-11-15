package kr.ac.dongguk.diet.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.domain.calc.MealAnalyzer
import kr.ac.dongguk.diet.domain.calc.NutritionCalculator
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.Nutrition
import kr.ac.dongguk.diet.domain.calc.DailySummary

/** 목록의 끼니 한 줄: 사진·시각·끼니 종류는 [meal], 열량은 [nutrition]. */
data class MealRow(val meal: Meal, val nutrition: Nutrition)

/** 날짜 한 묶음: 머리글에 [summary](열량·끼니 수), 아래에 그날 끼니들(시간 순). */
data class DaySection(val summary: DailySummary, val meals: List<MealRow>)

data class MealListUiState(
    val isLoading: Boolean = true,
    /** 최신 날짜부터. */
    val days: List<DaySection> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && days.isEmpty()
}

class MealListViewModel(
    private val mealRepository: MealRepository,
) : ViewModel() {

    val uiState: StateFlow<MealListUiState> = mealRepository.observeMeals()
        .map { meals -> MealListUiState(isLoading = false, days = toSections(meals)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealListUiState())

    fun delete(mealId: Long) {
        viewModelScope.launch { mealRepository.deleteMeal(mealId) }
    }

    companion object {
        fun toSections(meals: List<Meal>): List<DaySection> {
            val byDate = meals.groupBy { it.dateTime.toLocalDate() }
            return MealAnalyzer.dailySummaries(meals).map { summary ->
                DaySection(
                    summary = summary,
                    meals = byDate.getValue(summary.date)
                        .sortedBy { it.dateTime }
                        .map { MealRow(it, NutritionCalculator.forMeal(it)) },
                )
            }
        }
    }
}
