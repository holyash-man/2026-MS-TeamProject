package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.calc.TestMeals.SIMPLE
import kr.ac.dongguk.diet.domain.calc.TestMeals.meal
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.domain.model.Nutrition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MealAnalyzerTest {
    private val eps = 1e-9
    private val today = LocalDate.parse("2026-11-15")

    // 모두 SIMPLE 음식: 100g = 100kcal
    private val meals = listOf(
        meal("2026-11-15T08:00", MealType.BREAKFAST, SIMPLE to 100.0),  // 100 kcal
        meal("2026-11-15T12:30", MealType.LUNCH, SIMPLE to 300.0),      // 300 kcal
        meal("2026-11-14T19:00", MealType.DINNER, SIMPLE to 250.0),     // 250 kcal
        meal("2026-11-14T15:30", MealType.SNACK, SIMPLE to 50.0),       //  50 kcal
        meal("2026-10-17T12:00", MealType.LUNCH, SIMPLE to 200.0),      // 200 kcal, 30일 범위 첫날
        meal("2026-10-16T12:00", MealType.LUNCH, SIMPLE to 999.0),      // 31일 전 → 제외
    )

    @Test
    fun dailySummaries_groupByDate_newestFirst() {
        val daily = MealAnalyzer.dailySummaries(meals)

        assertEquals(
            listOf("2026-11-15", "2026-11-14", "2026-10-17", "2026-10-16").map(LocalDate::parse),
            daily.map { it.date },
        )
        assertEquals(2, daily[0].mealCount)
        assertEquals(400.0, daily[0].kcal, eps)
        assertEquals(2, daily[1].mealCount)
        assertEquals(300.0, daily[1].kcal, eps)
    }

    @Test
    fun recent30Days_includesFirstDay_excludesOlder() {
        val result = MealAnalyzer.recent(meals, today)

        assertEquals(LocalDate.parse("2026-10-17"), result.from)
        assertEquals(today, result.to)
        assertEquals(5, result.mealCount)
        assertEquals(900.0, result.totalKcal, eps)   // 100+300+250+50+200
    }

    @Test
    fun byType_alwaysHasFourTypes_andSumsToTotal() {
        val result = MealAnalyzer.recent(meals, today)

        assertEquals(MealType.entries, result.byType.map { it.type })
        val kcalByType = result.byType.associate { it.type to it.nutrition.kcal }
        assertEquals(100.0, kcalByType.getValue(MealType.BREAKFAST), eps)
        assertEquals(500.0, kcalByType.getValue(MealType.LUNCH), eps)   // 300 + 200
        assertEquals(250.0, kcalByType.getValue(MealType.DINNER), eps)
        assertEquals(50.0, kcalByType.getValue(MealType.SNACK), eps)

        val summed = result.byType.fold(Nutrition.ZERO) { acc, s -> acc + s.nutrition }
        assertEquals(result.total.kcal, summed.kcal, eps)
        assertEquals(result.total.carbs, summed.carbs, eps)
    }

    @Test
    fun noMeals_givesZeroTotalsButFourTypes() {
        val result = MealAnalyzer.recent(emptyList(), today)

        assertEquals(0, result.mealCount)
        assertEquals(0.0, result.totalKcal, eps)
        assertEquals(4, result.byType.size)
        assertTrue(result.byType.all { it.mealCount == 0 })
        assertTrue(result.daily.isEmpty())
    }

    @Test
    fun averageDailyKcal_dividesByAllDaysInPeriod() {
        // 최근 7일: 11/9~11/15, 총 700kcal → 하루 평균 100
        val week = MealAnalyzer.recent(meals, today, days = 7)
        assertEquals(700.0, week.totalKcal, eps)
        assertEquals(100.0, week.averageDailyKcal, eps)
    }

    @Test(expected = IllegalArgumentException::class)
    fun analyze_rejectsReversedPeriod() {
        MealAnalyzer.analyze(meals, from = today, to = today.minusDays(1))
    }
}
