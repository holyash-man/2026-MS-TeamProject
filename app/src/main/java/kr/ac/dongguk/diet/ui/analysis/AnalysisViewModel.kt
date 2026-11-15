package kr.ac.dongguk.diet.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.domain.calc.MealAnalyzer
import kr.ac.dongguk.diet.domain.calc.MealTypeSummary
import kr.ac.dongguk.diet.domain.calc.PeriodAnalysis
import kr.ac.dongguk.diet.domain.model.DataSourceInfo
import kr.ac.dongguk.diet.domain.model.MacroRatio
import kr.ac.dongguk.diet.domain.model.macroRatio
import java.time.LocalDate

/** 분석 기간. 과제 필수는 최근 1달이고, 최근 1주는 '한 주 영양 균형' 확인용. */
enum class AnalysisPeriod(val days: Int, val label: String) {
    MONTH(MealAnalyzer.RECENT_DAYS, "최근 1달"),
    WEEK(7, "최근 1주"),
}

/**
 * 끼니 종류 한 줄(조식/중식/석식/간식).
 * @property kcalShare 기간 전체 열량 중 이 끼니가 차지하는 비율(%). 전체가 0이면 0.
 */
data class MealTypeRow(
    val summary: MealTypeSummary,
    val macroRatio: MacroRatio,
    val kcalShare: Double,
)

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val period: AnalysisPeriod = AnalysisPeriod.MONTH,
    /** 기간 합계·하루 평균·날짜별 요약. 로딩 중에는 null. */
    val analysis: PeriodAnalysis? = null,
    /** 항상 4줄(조식·중식·석식·간식). */
    val byType: List<MealTypeRow> = emptyList(),
    /** 기간 전체 탄단지 열량 비율. */
    val macroRatio: MacroRatio = MacroRatio.ZERO,
    /** 화면에 표시할 영양 데이터 출처(과제 요구사항). */
    val source: DataSourceInfo? = null,
)

class AnalysisViewModel(
    mealRepository: MealRepository,
    foodRepository: FoodRepository,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val period = MutableStateFlow(AnalysisPeriod.MONTH)

    private val source = flow<DataSourceInfo?> { emit(foodRepository.getSourceInfo()) }.onStart { emit(null) }

    val uiState: StateFlow<AnalysisUiState> =
        combine(mealRepository.observeMeals(), period, source) { meals, selected, src ->
            val analysis = MealAnalyzer.recent(meals, today(), selected.days)
            val totalKcal = analysis.totalKcal
            AnalysisUiState(
                isLoading = false,
                period = selected,
                analysis = analysis,
                byType = analysis.byType.map { s ->
                    MealTypeRow(
                        summary = s,
                        macroRatio = s.nutrition.macroRatio(),
                        kcalShare = if (totalKcal > 0) s.nutrition.kcal / totalKcal * 100 else 0.0,
                    )
                },
                macroRatio = analysis.total.macroRatio(),
                source = src,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalysisUiState())

    fun onPeriodChange(newPeriod: AnalysisPeriod) {
        period.value = newPeriod
    }
}
