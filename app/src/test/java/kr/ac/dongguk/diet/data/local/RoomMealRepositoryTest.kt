package kr.ac.dongguk.diet.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kr.ac.dongguk.diet.data.food.LocalFoodRepository
import kr.ac.dongguk.diet.data.food.SampleFoods
import kr.ac.dongguk.diet.data.photo.FakePhotoStorage
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class RoomMealRepositoryTest {
    private val dao = FakeMealDao()
    private val photos = FakePhotoStorage()

    private suspend fun repoWithFoods(scheduler: kotlinx.coroutines.test.TestCoroutineScheduler): Pair<RoomMealRepository, LocalFoodRepository> {
        val foods = LocalFoodRepository({ SampleFoods.json() }, StandardTestDispatcher(scheduler))
        return RoomMealRepository(dao, foods, photos) to foods
    }

    private suspend fun meal(
        foods: LocalFoodRepository,
        at: String,
        vararg items: Pair<Long, Double>,
        id: Long = 0,
        photo: String? = null,
    ) = Meal(
        id = id,
        dateTime = LocalDateTime.parse(at),
        type = MealType.LUNCH,
        photoUri = photo,
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

    // ── 사진 ───────────────────────────────────────────────

    @Test
    fun save_copiesPickerPhotoIntoStorage() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)

        val id = repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, photo = "content://picker/42"))

        val saved = repo.getMeal(id)!!.photoUri!!
        assertTrue(photos.isStored(saved))           // 내부 보관소 주소로 바뀌어 저장됨
        assertEquals(listOf(saved), photos.stored)
    }

    @Test
    fun editWithoutChangingPhoto_doesNotCopyAgain() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val id = repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, photo = "content://picker/42"))
        val stored = repo.getMeal(id)!!.photoUri

        repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 150.0, id = id, photo = stored))

        assertEquals(listOf(stored), photos.stored)
        assertTrue(photos.deleted.isEmpty())
    }

    @Test
    fun editWithNewPhoto_deletesOldFile() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val id = repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, photo = "content://picker/1"))
        val old = repo.getMeal(id)!!.photoUri!!

        repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, id = id, photo = "content://picker/2"))

        val new = repo.getMeal(id)!!.photoUri!!
        assertNotEquals(old, new)
        assertEquals(listOf(old), photos.deleted)
        assertEquals(listOf(new), photos.stored)
    }

    @Test
    fun editRemovingPhoto_deletesFile() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val id = repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, photo = "content://picker/1"))

        repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, id = id, photo = null))

        assertNull(repo.getMeal(id)!!.photoUri)
        assertTrue(photos.stored.isEmpty())
    }

    @Test
    fun deleteMeal_deletesPhotoFile() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        val id = repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, photo = "content://picker/1"))

        repo.deleteMeal(id)

        assertTrue(photos.stored.isEmpty())
        assertEquals(1, photos.deleted.size)
    }

    @Test
    fun photoCopyFailure_savesNothing() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)
        photos.failNextStore = true

        val error = runCatching { repo.saveMeal(meal(foods, "2026-11-21T12:00", 1L to 210.0, photo = "content://picker/1")) }
            .exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(repo.observeMeals().first().isEmpty())
    }

    @Test
    fun saveMeals_insertsAllWithoutPhotos() = runTest {
        val (repo, foods) = repoWithFoods(testScheduler)

        repo.saveMeals(
            listOf(
                meal(foods, "2026-11-20T12:00", 1L to 210.0, photo = "content://picker/1"),
                meal(foods, "2026-11-21T12:00", 3L to 300.0),
            ),
        )

        val all = repo.observeMeals().first()
        assertEquals(2, all.size)
        assertTrue(all.all { it.photoUri == null })
        assertTrue(photos.stored.isEmpty())
    }
}
