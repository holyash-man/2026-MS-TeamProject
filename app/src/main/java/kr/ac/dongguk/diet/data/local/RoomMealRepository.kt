package kr.ac.dongguk.diet.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal

/** Room 기반 [MealRepository]. 음식 정보는 [FoodRepository] 에서 id 로 찾아 붙인다. */
class RoomMealRepository(
    private val dao: MealDao,
    private val foodRepository: FoodRepository,
) : MealRepository {

    private suspend fun foodsById(): Map<Long, Food> = foodRepository.getAll().associateBy { it.id }

    override fun observeMeals(): Flow<List<Meal>> =
        dao.observeAll().map { rows ->
            val foods = foodsById()
            rows.map { MealMappers.toDomain(it, foods) }
        }

    override suspend fun getMeal(id: Long): Meal? =
        dao.getById(id)?.let { MealMappers.toDomain(it, foodsById()) }

    /**
     * id 가 0이면 새로 저장, 아니면 수정한다. 저장된 id 를 돌려준다.
     * @throws IllegalArgumentException 음식이 하나도 없거나 섭취량이 0 이하일 때
     */
    override suspend fun saveMeal(meal: Meal): Long {
        require(meal.items.isNotEmpty()) { "음식을 하나 이상 입력해야 함" }
        meal.items.forEach { item ->
            require(item.amount.isFinite() && item.amount > 0) {
                "섭취량은 0보다 커야 함: ${item.food.name} ${item.amount}"
            }
        }
        return dao.saveMealWithItems(MealMappers.toEntity(meal), MealMappers.toItemEntities(meal))
    }

    override suspend fun deleteMeal(id: Long) {
        dao.deleteMeal(id)
    }
}
