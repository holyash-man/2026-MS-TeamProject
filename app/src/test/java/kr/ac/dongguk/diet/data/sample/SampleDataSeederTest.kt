package kr.ac.dongguk.diet.data.sample

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kr.ac.dongguk.diet.data.food.LocalFoodRepository
import kr.ac.dongguk.diet.data.local.FakeMealDao
import kr.ac.dongguk.diet.data.local.RoomMealRepository
import kr.ac.dongguk.diet.data.photo.FakePhotoStorage
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDateTime

class SampleDataSeederTest {
    private val now = LocalDateTime.parse("2026-11-15T20:00")

    @Test
    fun seedsAllGeneratedMeals_whenEmpty() = runTest {
        val foods = LocalFoodRepository({ File("src/main/assets/foods.json").readText() }, StandardTestDispatcher(testScheduler))
        val meals = RoomMealRepository(FakeMealDao(), foods, FakePhotoStorage())

        val inserted = SampleDataSeeder(foods, meals) { now }.seedIfEmpty()

        assertTrue(inserted >= 60)
        assertEquals(inserted, meals.observeMeals().first().size)
    }

    @Test
    fun doesNothing_whenRecordsExist() = runTest {
        val foods = LocalFoodRepository({ File("src/main/assets/foods.json").readText() }, StandardTestDispatcher(testScheduler))
        val meals = RoomMealRepository(FakeMealDao(), foods, FakePhotoStorage())
        meals.saveMeal(Meal(dateTime = now, type = MealType.DINNER, photoUri = null, items = listOf(MealItem(foods.getById(1)!!, 210.0))))

        val inserted = SampleDataSeeder(foods, meals) { now }.seedIfEmpty()

        assertEquals(0, inserted)
        assertEquals(1, meals.observeMeals().first().size)
    }
}
