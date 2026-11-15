package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.Nutrition

/**
 * 섭취량 → 영양성분 환산.
 *
 * 식약처 원본 값은 100g(음료는 100ml) 기준이므로 `값 × 섭취량 / 100` 으로 계산한다.
 * 계산 중에는 반올림하지 않고, 화면에 보여줄 때만 [Nutrition.rounded] 로 반올림한다.
 * (음식마다 먼저 반올림한 뒤 더하면 합계 오차가 쌓이기 때문)
 */
object NutritionCalculator {

    /** 100g(ml)당 영양성분 [per100] 을 [amount] 만큼 먹었을 때의 영양성분. */
    fun forAmount(per100: Nutrition, amount: Double): Nutrition {
        require(amount.isFinite() && amount > 0) { "섭취량은 0보다 커야 함: $amount" }
        val ratio = amount / 100.0
        return Nutrition(
            kcal = per100.kcal * ratio,
            carbs = per100.carbs * ratio,
            protein = per100.protein * ratio,
            fat = per100.fat * ratio,
        )
    }

    fun forFood(food: Food, amount: Double): Nutrition = forAmount(food.per100, amount)

    fun forItem(item: MealItem): Nutrition = forFood(item.food, item.amount)

    /** 입력 화면 미리보기용: 아직 저장하지 않은 음식 목록의 합계. */
    fun forItems(items: List<MealItem>): Nutrition =
        items.fold(Nutrition.ZERO) { sum, item -> sum + forItem(item) }

    fun forMeal(meal: Meal): Nutrition = forItems(meal.items)

    /** 섭취량을 입력하지 않았을 때 쓸 기본값: 1인분 참고량, 없으면 100. */
    fun defaultAmount(food: Food): Double = food.servingSize ?: 100.0
}
