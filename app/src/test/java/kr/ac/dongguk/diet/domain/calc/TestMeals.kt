package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.FoodUnit
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.domain.model.Nutrition
import java.time.LocalDateTime

/** 계산 테스트용 음식·식사 생성 도우미. */
object TestMeals {
    /** 100g당 100kcal / 탄 10g / 단 5g / 지 2g 인 단순한 음식. */
    val SIMPLE = food(id = 1, per100 = Nutrition(kcal = 100.0, carbs = 10.0, protein = 5.0, fat = 2.0))

    fun food(
        id: Long,
        per100: Nutrition,
        unit: FoodUnit = FoodUnit.GRAM,
        servingSize: Double? = null,
    ) = Food(
        id = id, code = "T$id", name = "음식$id", category = "테스트",
        unit = unit, per100 = per100, servingSize = servingSize,
    )

    fun meal(
        at: String,
        type: MealType = MealType.LUNCH,
        vararg items: Pair<Food, Double> = arrayOf(SIMPLE to 100.0),
    ) = Meal(
        dateTime = LocalDateTime.parse(at),
        type = type,
        photoUri = null,
        items = items.map { (food, amount) -> MealItem(food, amount) },
    )
}
