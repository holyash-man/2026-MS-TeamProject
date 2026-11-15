package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.model.MacroRatio
import kr.ac.dongguk.diet.domain.model.Nutrition
import kr.ac.dongguk.diet.domain.model.macroRatio
import kr.ac.dongguk.diet.domain.model.rounded
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionFormatTest {

    @Test
    fun rounded_kcalToInteger_macrosToOneDecimal() {
        val n = Nutrition(kcal = 348.6, carbs = 78.393, protein = 7.05, fat = 0.672)
        assertEquals(Nutrition(349.0, 78.4, 7.1, 0.7), n.rounded())
    }

    @Test
    fun macroRatio_usesAtwaterFactors() {
        // 탄 50g(200kcal) / 단 25g(100kcal) / 지 100/9g(100kcal) → 50% / 25% / 25%
        val ratio = Nutrition(kcal = 400.0, carbs = 50.0, protein = 25.0, fat = 100.0 / 9).macroRatio()
        assertEquals(50.0, ratio.carbs, 1e-9)
        assertEquals(25.0, ratio.protein, 1e-9)
        assertEquals(25.0, ratio.fat, 1e-9)
    }

    @Test
    fun macroRatio_zeroNutrition_isZero() {
        assertEquals(MacroRatio.ZERO, Nutrition.ZERO.macroRatio())
    }
}
