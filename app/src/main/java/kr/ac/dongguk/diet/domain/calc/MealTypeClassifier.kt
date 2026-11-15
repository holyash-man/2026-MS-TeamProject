package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.model.MealType
import java.time.LocalTime

/**
 * 식사 시각으로 끼니 종류를 추천한다. 입력 화면의 기본 선택값이며, 사용자가 바꿀 수 있다.
 *
 * | 시간대          | 끼니      |
 * |-----------------|-----------|
 * | 05:00 ~ 10:29   | 조식      |
 * | 10:30 ~ 14:59   | 중식      |
 * | 17:00 ~ 20:59   | 석식      |
 * | 그 외           | 간식/음료 |
 */
object MealTypeClassifier {
    private val BREAKFAST_START: LocalTime = LocalTime.of(5, 0)
    private val LUNCH_START: LocalTime = LocalTime.of(10, 30)
    private val LUNCH_END: LocalTime = LocalTime.of(15, 0)
    private val DINNER_START: LocalTime = LocalTime.of(17, 0)
    private val DINNER_END: LocalTime = LocalTime.of(21, 0)

    fun suggest(time: LocalTime): MealType = when {
        time >= BREAKFAST_START && time < LUNCH_START -> MealType.BREAKFAST
        time >= LUNCH_START && time < LUNCH_END -> MealType.LUNCH
        time >= DINNER_START && time < DINNER_END -> MealType.DINNER
        else -> MealType.SNACK
    }
}
