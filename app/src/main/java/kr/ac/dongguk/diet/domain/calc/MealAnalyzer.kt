package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.domain.model.Nutrition
import java.time.LocalDate

/** 하루 요약: 목록 화면의 '식사 날짜별 열량, 끼니 수'. */
data class DailySummary(
    val date: LocalDate,
    val mealCount: Int,
    val nutrition: Nutrition,
) {
    val kcal: Double get() = nutrition.kcal
}

/** 끼니 종류 하나(조식/중식/석식/간식)의 기간 합계. */
data class MealTypeSummary(
    val type: MealType,
    val mealCount: Int,
    val nutrition: Nutrition,
)

/**
 * 기간 분석 결과: 분석 화면의 '최근 1달 칼로리 총량'과 '끼니 종류별 영양소'.
 * @property byType 4가지 끼니가 항상 모두 들어 있다(기록이 없으면 0). [MealType] 선언 순서.
 * @property daily 기록이 있는 날만, 최신 날짜부터.
 */
data class PeriodAnalysis(
    val from: LocalDate,
    val to: LocalDate,
    val mealCount: Int,
    val total: Nutrition,
    val byType: List<MealTypeSummary>,
    val daily: List<DailySummary>,
) {
    val totalKcal: Double get() = total.kcal

    /** 기간 일수(양 끝 포함)로 나눈 하루 평균 열량. 기록이 없는 날도 일수에 포함한다. */
    val averageDailyKcal: Double
        get() = total.kcal / (to.toEpochDay() - from.toEpochDay() + 1)
}

object MealAnalyzer {

    /** 최근 1달 = 오늘 포함 30일. */
    const val RECENT_DAYS = 30

    /** 날짜별 요약, 최신 날짜부터. */
    fun dailySummaries(meals: List<Meal>): List<DailySummary> =
        meals.groupBy { it.dateTime.toLocalDate() }
            .map { (date, dayMeals) ->
                DailySummary(
                    date = date,
                    mealCount = dayMeals.size,
                    nutrition = sumOf(dayMeals),
                )
            }
            .sortedByDescending { it.date }

    /** [from]~[to] (양 끝 포함) 기간의 식사를 분석한다. */
    fun analyze(meals: List<Meal>, from: LocalDate, to: LocalDate): PeriodAnalysis {
        require(!from.isAfter(to)) { "기간 시작이 끝보다 늦음: $from > $to" }
        val inRange = meals.filter { meal ->
            val date = meal.dateTime.toLocalDate()
            !date.isBefore(from) && !date.isAfter(to)
        }
        val byType = MealType.entries.map { type ->
            val typed = inRange.filter { it.type == type }
            MealTypeSummary(type = type, mealCount = typed.size, nutrition = sumOf(typed))
        }
        return PeriodAnalysis(
            from = from,
            to = to,
            mealCount = inRange.size,
            total = sumOf(inRange),
            byType = byType,
            daily = dailySummaries(inRange),
        )
    }

    /** 오늘 포함 최근 [days]일. 기본 30일(최근 1달), 7을 넣으면 최근 1주. */
    fun recent(meals: List<Meal>, today: LocalDate, days: Int = RECENT_DAYS): PeriodAnalysis {
        require(days > 0) { "일수는 1 이상이어야 함: $days" }
        return analyze(meals, from = today.minusDays(days - 1L), to = today)
    }

    private fun sumOf(meals: List<Meal>): Nutrition =
        meals.fold(Nutrition.ZERO) { sum, meal -> sum + NutritionCalculator.forMeal(meal) }
}
