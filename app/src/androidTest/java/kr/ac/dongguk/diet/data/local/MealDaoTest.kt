package kr.ac.dongguk.diet.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 실제 Room(SQLite) 동작 검증: 정렬, 트랜잭션 저장, 외래키 CASCADE 삭제. */
@RunWith(AndroidJUnit4::class)
class MealDaoTest {
    private lateinit var db: DietDatabase
    private lateinit var dao: MealDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietDatabase::class.java,
        ).build()
        dao = db.mealDao()
    }

    @After
    fun tearDown() = db.close()

    private fun meal(dateTime: Long, type: String = "LUNCH", id: Long = 0) =
        MealEntity(id = id, dateTime = dateTime, mealType = type, photoUri = null)

    private fun item(foodId: Long, amount: Double, position: Int) =
        MealItemEntity(mealId = 0, foodId = foodId, amount = amount, position = position)

    @Test
    fun saveAndRead_withItems() = runBlocking {
        val id = dao.saveMealWithItems(meal(1_000), listOf(item(1, 210.0, 0), item(3, 300.0, 1)))

        val row = dao.getById(id)!!
        assertEquals("LUNCH", row.meal.mealType)
        assertEquals(listOf(1L, 3L), row.items.sortedBy { it.position }.map { it.foodId })
        assertTrue(row.items.all { it.mealId == id })
    }

    @Test
    fun observeAll_isNewestFirst() = runBlocking {
        val old = dao.saveMealWithItems(meal(1_000), listOf(item(1, 100.0, 0)))
        val new = dao.saveMealWithItems(meal(9_000), listOf(item(1, 100.0, 0)))

        assertEquals(listOf(new, old), dao.observeAll().first().map { it.meal.id })
    }

    @Test
    fun update_replacesItems() = runBlocking {
        val id = dao.saveMealWithItems(meal(1_000), listOf(item(1, 100.0, 0), item(2, 50.0, 1)))

        dao.saveMealWithItems(meal(2_000, type = "DINNER", id = id), listOf(item(5, 350.0, 0)))

        val row = dao.getById(id)!!
        assertEquals("DINNER", row.meal.mealType)
        assertEquals(listOf(5L), row.items.map { it.foodId })
    }

    @Test
    fun deleteMeal_cascadesToItems() = runBlocking {
        val id = dao.saveMealWithItems(meal(1_000), listOf(item(1, 100.0, 0), item(2, 50.0, 1)))

        assertEquals(1, dao.deleteMeal(id))

        assertNull(dao.getById(id))
        val remaining = db.query("SELECT COUNT(*) FROM meal_items", null).use { c -> c.moveToFirst(); c.getInt(0) }
        assertEquals(0, remaining)
    }
}
