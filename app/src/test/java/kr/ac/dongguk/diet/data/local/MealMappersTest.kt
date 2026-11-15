package kr.ac.dongguk.diet.data.local

import kr.ac.dongguk.diet.data.food.FoodCatalogParser
import kr.ac.dongguk.diet.data.food.SampleFoods
import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class MealMappersTest {
    private val foodsById = FoodCatalogParser.parse(SampleFoods.json()).foods.associateBy { it.id }

    @Test
    fun dateTime_roundTripsExactly() {
        val dt = LocalDateTime.parse("2026-11-15T12:34:56")
        assertEquals(dt, MealMappers.fromEpochSecond(MealMappers.toEpochSecond(dt)))
    }

    @Test
    fun epochSecond_preservesChronologicalOrder() {
        val earlier = MealMappers.toEpochSecond(LocalDateTime.parse("2026-11-15T08:00"))
        val later = MealMappers.toEpochSecond(LocalDateTime.parse("2026-11-15T19:00"))
        assertTrue(earlier < later)
    }

    @Test
    fun toDomain_mapsTypeAndOrdersItemsByPosition() {
        val row = MealWithItems(
            meal = MealEntity(id = 7, dateTime = 0, mealType = "DINNER", photoUri = "content://p/1"),
            items = listOf(
                MealItemEntity(mealId = 7, foodId = 3, amount = 300.0, position = 1),
                MealItemEntity(mealId = 7, foodId = 1, amount = 210.0, position = 0),
            ),
        )
        val meal = MealMappers.toDomain(row, foodsById)

        assertEquals(MealType.DINNER, meal.type)
        assertEquals("content://p/1", meal.photoUri)
        assertEquals(listOf("쌀밥", "김치찌개"), meal.items.map { it.food.name })
        assertEquals(listOf(210.0, 300.0), meal.items.map { it.amount })
    }

    @Test
    fun toDomain_dropsItemsWithUnknownFood() {
        val row = MealWithItems(
            meal = MealEntity(id = 1, dateTime = 0, mealType = "LUNCH", photoUri = null),
            items = listOf(
                MealItemEntity(mealId = 1, foodId = 1, amount = 100.0, position = 0),
                MealItemEntity(mealId = 1, foodId = 999, amount = 100.0, position = 1),
            ),
        )
        assertEquals(listOf("쌀밥"), MealMappers.toDomain(row, foodsById).items.map { it.food.name })
    }
}
