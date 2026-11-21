package kr.ac.dongguk.diet.ui

import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.FoodUnit
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.domain.model.Nutrition
import java.time.LocalDateTime

/** UI 테스트용 고정 데이터. 열량을 손으로 계산할 수 있도록 단순한 값을 쓴다. */
object TestFoods {
    /** 100g당 150kcal → 210g = 315kcal */
    val rice = Food(1, "T001", "쌀밥", "밥류", FoodUnit.GRAM, Nutrition(150.0, 33.0, 2.5, 0.3), 210.0)

    /** 100g당 50kcal → 300g = 150kcal */
    val soup = Food(2, "T002", "된장국", "국 및 탕류", FoodUnit.GRAM, Nutrition(50.0, 4.0, 3.0, 2.0), 300.0)

    /** 2026-11-16(월) 12:30 중식: 쌀밥 210g + 된장국 300g = 465kcal */
    val lunch = Meal(
        id = 1,
        dateTime = LocalDateTime.of(2026, 11, 16, 12, 30),
        type = MealType.LUNCH,
        photoUri = null,
        items = listOf(MealItem(rice, 210.0), MealItem(soup, 300.0)),
    )
}
