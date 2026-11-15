package kr.ac.dongguk.diet.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kr.ac.dongguk.diet.AppContainer
import kr.ac.dongguk.diet.DietApplication
import kr.ac.dongguk.diet.ui.analysis.AnalysisViewModel
import kr.ac.dongguk.diet.ui.detail.MealDetailViewModel
import kr.ac.dongguk.diet.ui.input.MealInputViewModel
import kr.ac.dongguk.diet.ui.list.MealListViewModel

/**
 * 모든 화면 ViewModel 의 팩토리. Compose 에서:
 * ```
 * val vm: MealListViewModel = viewModel(factory = AppViewModelProvider.Factory)
 * ```
 * 상세/수정 화면은 내비게이션 route 에 `mealId`(Long) 인자를 넣으면 SavedStateHandle 로 전달된다.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MealListViewModel(container().mealRepository, container().sampleDataSeeder)
        }
        initializer {
            MealInputViewModel(createSavedStateHandle(), container().foodRepository, container().mealRepository)
        }
        initializer {
            MealDetailViewModel(createSavedStateHandle(), container().mealRepository)
        }
        initializer {
            AnalysisViewModel(container().mealRepository, container().foodRepository)
        }
    }

    private fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as DietApplication).container
}
