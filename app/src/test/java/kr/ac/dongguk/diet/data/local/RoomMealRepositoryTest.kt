package kr.ac.dongguk.diet.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kr.ac.dongguk.diet.data.food.LocalFoodRepository
import kr.ac.dongguk.diet.data.food.SampleFoods
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class RoomMealRepositoryTest {
    private val dao = FakeMealDao()

    private suspend fun repoWithFoods(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler): Pair<RoomMealRepository, LocalFoodRepository> {
        val foods = LocalFoodRepository({ SampleFoods.json() }, StandardTestDispatcher(scheduler))
        return RoomMealRepository(dao, foods) to foods
    }

    private suspend fun meal(foods: LocalFoodRepository, at: String, vararg items: Pair<Long, Double>, id: Long = 0) = Meal(
        id = id,
        dateTime = LocalDateTime.parse(at),
        type = MealType.LUNCH,
        photoUri = null,
        items = items.map { (foodId, amount) -> MealItem(foods.getById(foodId)!!, amount) },
    )

    @Test
    fun saveNew_thenGet_returnsSameContent() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val input = meal(foods, "2026-11-15T12:30", 1L to 210.0, 3L to 300.0)

        val id = repo.saveMeal(input)

        assertEquals(input.copy(id = id), repo.getMeal(id))
    }

    @Test
    fun saveExisting_replacesItems() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val id = repo.saveMeal(meal(foods, "2026-11-15T12:30", 1L to 210.0, 3L to 300.0))

        repo.saveMeal(meal(foods, "2026-11-15T13:00", 5L to 350.0, id = id))

        val saved = repo.getMeal(id)!!
        assertEquals(listOf("아메리카노"), saved.items.map { it.food.name })
        assertEquals(LocalDateTime.parse("2026-11-15T13:00"), saved.dateTime)
        assertEquals(1, dao.itemCount())
    }

    @Test
    fun observeMeals_newestFirst_andUpdatesAfterDelete() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val morning = repo.saveMeal(meal(foods, "2026-11-15T08:00", 1L to 100.0))
        val evening = repo.saveMeal(meal(foods, "2026-11-15T19:00", 3L to 300.0))

        assertEquals(listOf(evening, morning), repo.observeMeals().first().map { it.id })

        repo.deleteMeal(evening)
        assertEquals(listOf(morning), repo.observeMeals().first().map { it.id })
        assertNull(repo.getMeal(evening))
        assertEquals(1, dao.itemCount()) // 지운 끼니의 음식도 함께 삭제
    }

    @Test(expected = IllegalArgumentException::class)
    fun save_rejectsMealWithoutItems() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        repo.saveMeal(meal(foods, "2026-11-15T12:30"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun save_rejectsZeroAmount() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        repo.saveMeal(meal(foods, "2026-11-15T12:30", 1L to 0.0))
    }

    @Test(expected = IllegalStateException::class)
    fun save_withUnknownId_fails() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        repo.saveMeal(meal(foods, "2026-11-15T12:30", 1L to 100.0, id = 42))
    }
}
