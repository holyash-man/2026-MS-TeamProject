package kr.ac.dongguk.diet.domain.calc

import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class MealTypeClassifierTest {
    private fun at(time: String) = MealTypeClassifier.suggest(LocalTime.parse(time))

    @Test
    fun breakfast_from0500_to1029() {
        assertEquals(MealType.BREAKFAST, at("05:00"))
        assertEquals(MealType.BREAKFAST, at("08:00"))
        assertEquals(MealType.BREAKFAST, at("10:29"))
    }

    @Test
    fun lunch_from1030_to1459() {
        assertEquals(MealType.LUNCH, at("10:30"))
        assertEquals(MealType.LUNCH, at("12:30"))
        assertEquals(MealType.LUNCH, at("14:59"))
    }

    @Test
    fun dinner_from1700_to2059() {
        assertEquals(MealType.DINNER, at("17:00"))
        assertEquals(MealType.DINNER, at("20:59"))
    }

    @Test
    fun otherTimes_areSnack() {
        assertEquals(MealType.SNACK, at("15:00"))
        assertEquals(MealType.SNACK, at("16:59"))
        assertEquals(MealType.SNACK, at("21:00"))
        assertEquals(MealType.SNACK, at("00:00"))
        assertEquals(MealType.SNACK, at("04:59"))
    }
}
