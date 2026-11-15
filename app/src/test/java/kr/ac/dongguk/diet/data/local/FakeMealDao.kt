package kr.ac.dongguk.diet.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [MealDao] 메모리 구현. SQL 쿼리만 흉내 내고, saveMealWithItems 같은 기본 메서드는 실제 코드를 그대로 쓴다.
 * 실제 SQL 동작은 androidTest 의 MealDaoTest 에서 검증한다.
 */
class FakeMealDao : MealDao {
    private val meals = MutableStateFlow<Map<Long, MealEntity>>(emptyMap())
    private val items = MutableStateFlow<List<MealItemEntity>>(emptyList())
    private var nextMealId = 1L
    private var nextItemId = 1L

    private fun rows(): List<MealWithItems> =
        meals.value.values
            .sortedWith(compareByDescending<MealEntity> { it.dateTime }.thenByDescending { it.id })
            .map { m -> MealWithItems(m, items.value.filter { it.mealId == m.id }) }

    override fun observeAll(): Flow<List<MealWithItems>> =
        kotlinx.coroutines.flow.combine(meals, items) { _, _ -> rows() }

    override suspend fun getById(id: Long): MealWithItems? = rows().firstOrNull { it.meal.id == id }

    override suspend fun insertMeal(meal: MealEntity): Long {
        val id = nextMealId++
        meals.value = meals.value + (id to meal.copy(id = id))
        return id
    }

    override suspend fun updateMeal(meal: MealEntity): Int {
        if (meal.id !in meals.value) return 0
        meals.value = meals.value + (meal.id to meal)
        return 1
    }

    override suspend fun insertItems(items: List<MealItemEntity>) {
        this.items.value = this.items.value + items.map { it.copy(id = nextItemId++) }
    }

    override suspend fun deleteItemsOf(mealId: Long) {
        items.value = items.value.filterNot { it.mealId == mealId }
    }

    override suspend fun deleteMeal(id: Long): Int {
        if (id !in meals.value) return 0
        meals.value = meals.value - id
        deleteItemsOf(id) // 외래키 CASCADE 흉내
        return 1
    }

    fun itemCount(): Int = items.value.size
}
