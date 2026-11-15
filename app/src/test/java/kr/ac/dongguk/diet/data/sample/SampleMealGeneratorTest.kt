package kr.ac.dongguk.diet.data.sample

import kr.ac.dongguk.diet.data.food.FoodCatalogParser
import kr.ac.dongguk.diet.data.food.SampleFoods
import kr.ac.dongguk.diet.domain.calc.MealAnalyzer
import kr.ac.dongguk.diet.domain.calc.MealTypeClassifier
import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

class SampleMealGeneratorTest {
    private val realFoods = FoodCatalogParser.parse(File("src/main/assets/foods.json").readText()).foods
    private val generator = SampleMealGenerator(realFoods)
    private val now = LocalDateTime.parse("2026-11-15T10:00")
    private val meals = generator.generate(now)

    @Test(expected = IllegalArgumentException::class)
    fun templateFoodMissingFromData_isRejected() {
        SampleMealGenerator(FoodCatalogParser.parse(SampleFoods.json()).foods)
    }

    @Test
    fun sameSeed_sameMeals_differentSeed_differentMeals() {
        assertEquals(meals, generator.generate(now))
        assertNotEquals(meals, generator.generate(now, seed = 7))
    }

    @Test
    fun mealsAreWithinLast30Days_andNeverInFuture() {
        val first = LocalDate.parse("2026-10-17")
        assertTrue(meals.all { !it.dateTime.toLocalDate().isBefore(first) })
        assertTrue(meals.all { !it.dateTime.isAfter(now) })
        // 오늘은 10시 기준이라 점심(11:40~)은 아직 없음
        assertTrue(meals.none { it.dateTime.toLocalDate() == now.toLocalDate() && it.type == MealType.LUNCH })
    }

    @Test
    fun everyPastDay_hasLunch() {
        val lunchDays = meals.filter { it.type == MealType.LUNCH }.map { it.dateTime.toLocalDate() }.toSet()
        val pastDays = (29 downTo 1).map { now.toLocalDate().minusDays(it.toLong()) }.toSet()
        assertEquals(pastDays, lunchDays)
    }

    @Test
    fun mealTypeMatchesTimeClassifier() {
        assertTrue(meals.all { MealTypeClassifier.suggest(it.dateTime.toLocalTime()) == it.type })
    }

    @Test
    fun enoughVarietyForDemo() {
        assertTrue("끼니 수 ${meals.size}", meals.size >= 60)
        assertEquals(MealType.entries.toSet(), meals.map { it.type }.toSet())
        assertTrue(meals.all { m -> m.items.isNotEmpty() && m.items.all { it.amount > 0 } })
        assertTrue(meals.flatMap { it.items }.map { it.food.id }.toSet().size >= 50) // 음식 50종 이상 등장
    }

    @Test
    fun averageDailyKcal_isRealistic() {
        // 오늘을 제외한 29일(끼니가 다 채워진 날) 기준 하루 평균 열량
        val full = generator.generate(LocalDateTime.parse("2026-11-15T23:59"))
        val avg = MealAnalyzer.analyze(full, LocalDate.parse("2026-10-17"), LocalDate.parse("2026-11-14")).averageDailyKcal
        assertTrue("하루 평균 ${"%.0f".format(avg)} kcal", avg in 1200.0..3000.0)
        println("샘플 하루 평균 열량: ${"%.0f".format(avg)} kcal, 끼니 ${full.size}개")
    }
}
