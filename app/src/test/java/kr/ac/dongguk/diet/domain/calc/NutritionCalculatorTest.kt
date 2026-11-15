package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.data.food.FoodCatalogParser
import kr.ac.dongguk.diet.domain.calc.TestMeals.SIMPLE
import kr.ac.dongguk.diet.domain.model.FoodUnit
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.Nutrition
import kr.ac.dongguk.diet.domain.model.rounded
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class NutritionCalculatorTest {
    private val eps = 1e-9

    private fun assertNutrition(expected: Nutrition, actual: Nutrition) {
        assertEquals("kcal", expected.kcal, actual.kcal, eps)
        assertEquals("carbs", expected.carbs, actual.carbs, eps)
        assertEquals("protein", expected.protein, actual.protein, eps)
        assertEquals("fat", expected.fat, actual.fat, eps)
    }

    @Test
    fun amount100_equalsPer100Values() {
        assertNutrition(SIMPLE.per100, NutritionCalculator.forFood(SIMPLE, 100.0))
    }

    @Test
    fun scalesLinearlyWithAmount() {
        // 250g = 100g 기준 × 2.5
        assertNutrition(Nutrition(250.0, 25.0, 12.5, 5.0), NutritionCalculator.forFood(SIMPLE, 250.0))
        // 30g = × 0.3
        assertNutrition(Nutrition(30.0, 3.0, 1.5, 0.6), NutritionCalculator.forFood(SIMPLE, 30.0))
    }

    @Test
    fun mlFood_usesSameFormula() {
        val drink = TestMeals.food(2, Nutrition(57.0, 3.11, 2.72, 3.72), unit = FoodUnit.MILLILITER)
        assertNutrition(Nutrition(199.5, 10.885, 9.52, 13.02), NutritionCalculator.forFood(drink, 350.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroAmount_isRejected() {
        NutritionCalculator.forFood(SIMPLE, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeAmount_isRejected() {
        NutritionCalculator.forFood(SIMPLE, -50.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nanAmount_isRejected() {
        NutritionCalculator.forFood(SIMPLE, Double.NaN)
    }

    @Test
    fun meal_sumsAllItems() {
        val other = TestMeals.food(2, Nutrition(200.0, 30.0, 4.0, 6.0))
        val meal = TestMeals.meal("2026-11-15T12:00", items = arrayOf(SIMPLE to 200.0, other to 50.0))
        // 200g×SIMPLE(200, 20, 10, 4) + 50g×other(100, 15, 2, 3)
        assertNutrition(Nutrition(300.0, 35.0, 12.0, 7.0), NutritionCalculator.forMeal(meal))
    }

    @Test
    fun emptyItems_isZero() {
        assertNutrition(Nutrition.ZERO, NutritionCalculator.forItems(emptyList()))
    }

    @Test
    fun defaultAmount_usesServingSizeOr100() {
        assertEquals(100.0, NutritionCalculator.defaultAmount(SIMPLE), eps)
        val served = TestMeals.food(3, SIMPLE.per100, servingSize = 136.4)
        assertEquals(136.4, NutritionCalculator.defaultAmount(served), eps)
    }

    @Test
    fun realMfdsData_handCalculatedLunch() {
        // 실제 식약처 값으로 손계산한 점심: 쌀밥 210g + 김치찌개 300g + 배추김치 40g
        val catalog = FoodCatalogParser.parse(File("src/main/assets/foods.json").readText())
        fun food(name: String) = catalog.foods.first { it.name == name }
        val items = listOf(
            MealItem(food("쌀밥"), 210.0),     // 166 kcal, 37.33 / 3.36 / 0.32 (per 100g)
            MealItem(food("김치찌개"), 300.0), //  61 kcal,  2.98 / 3.77 / 3.75
            MealItem(food("배추김치"), 40.0),  //  38 kcal,  6.49 / 1.98 / 0.43
        )
        val total = NutritionCalculator.forItems(items)

        // 348.6 + 183 + 15.2 / 78.393 + 8.94 + 2.596 / 7.056 + 11.31 + 0.792 / 0.672 + 11.25 + 0.172
        assertNutrition(Nutrition(546.8, 89.929, 19.158, 12.094), total)
        assertEquals(Nutrition(547.0, 89.9, 19.2, 12.1), total.rounded())
    }
}
