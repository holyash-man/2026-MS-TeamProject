@file:OptIn(ExperimentalCoroutinesApi::class)

package kr.ac.dongguk.diet.ui.analysis

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.ui.MainDispatcherRule
import kr.ac.dongguk.diet.ui.TestRepositories
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AnalysisViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()
    private val repos = TestRepositories(main.dispatcher)
    private val eps = 1e-9

    private suspend fun seed() {
        repos.saveMeal("2026-11-15T08:00", MealType.BREAKFAST, 1L to 100.0) // 100 kcal
        repos.saveMeal("2026-11-15T12:00", MealType.LUNCH, 1L to 300.0)     // 300 kcal
        repos.saveMeal("2026-11-01T19:00", MealType.DINNER, 3L to 400.0)    // 400 kcal, 1주 밖·1달 안
        repos.saveMeal("2026-10-01T12:00", MealType.LUNCH, 1L to 999.0)     // 1달 밖
    }

    private fun kotlinx.coroutines.test.TestScope.newViewModel(): AnalysisViewModel {
        val vm = AnalysisViewModel(repos.meals, repos.foods, today = { LocalDate.parse("2026-11-15") })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun month_totalAndShareByMealType() = runTest {
        seed()
        val state = newViewModel().uiState.value

        assertEquals(AnalysisPeriod.MONTH, state.period)
        assertEquals(800.0, state.analysis!!.totalKcal, eps)
        assertEquals(MealType.entries, state.byType.map { it.summary.type })
        val share = state.byType.associate { it.summary.type to it.kcalShare }
        assertEquals(12.5, share.getValue(MealType.BREAKFAST), eps) // 100 / 800
        assertEquals(37.5, share.getValue(MealType.LUNCH), eps)
        assertEquals(50.0, share.getValue(MealType.DINNER), eps)
        assertEquals(0.0, share.getValue(MealType.SNACK), eps)
    }

    @Test
    fun switchToWeek_excludesOlderMeals() = runTest {
        seed()
        val vm = newViewModel()

        vm.onPeriodChange(AnalysisPeriod.WEEK)

        val state = vm.uiState.value
        assertEquals(AnalysisPeriod.WEEK, state.period)
        assertEquals(400.0, state.analysis!!.totalKcal, eps)
        assertEquals(LocalDate.parse("2026-11-09"), state.analysis.from)
    }

    @Test
    fun exposesDataSourceForDisplay() = runTest {
        val state = newViewModel().uiState.value

        val source = state.source!!
        assertEquals("식품의약품안전처", source.provider)
        assertEquals("https://www.data.go.kr/data/15100070/standard.do", source.url)
    }

    @Test
    fun macroRatio_ofWholePeriod() = runTest {
        seed()
        val ratio = newViewModel().uiState.value.macroRatio
        // 모든 음식이 탄 20 / 단 5 / 지 1 (g/100g) → 80 : 20 : 9 kcal
        assertEquals(80.0 / 109 * 100, ratio.carbs, 1e-6)
        assertEquals(20.0 / 109 * 100, ratio.protein, 1e-6)
        assertEquals(9.0 / 109 * 100, ratio.fat, 1e-6)
    }
}
