package kr.ac.dongguk.diet

import kr.ac.dongguk.diet.domain.model.Nutrition
import org.junit.Assert.assertEquals
import org.junit.Test

/** 단위 테스트 실행 환경 확인용. 영양 계산 로직 테스트는 domain/calc 구현과 함께 추가한다. */
class NutritionTest {
    @Test
    fun plus_addsEachNutrient() {
        val a = Nutrition(kcal = 100.0, carbs = 20.0, protein = 5.0, fat = 1.0)
        val b = Nutrition(kcal = 50.0, carbs = 10.0, protein = 2.5, fat = 0.5)

        assertEquals(Nutrition(150.0, 30.0, 7.5, 1.5), a + b)
    }

    @Test
    fun zero_isIdentity() {
        val a = Nutrition(kcal = 100.0, carbs = 20.0, protein = 5.0, fat = 1.0)
        assertEquals(a, a + Nutrition.ZERO)
    }
}
