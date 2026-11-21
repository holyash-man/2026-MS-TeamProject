package kr.ac.dongguk.diet.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.ac.dongguk.diet.data.photo.PhotoStorage
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal
import java.io.IOException

/**
 * Room 기반 [MealRepository]. 음식 정보는 [FoodRepository] 에서 id 로 찾아 붙인다.
 * 사진은 저장할 때 [PhotoStorage] 로 앱 내부에 복사하고, 끼니를 지우거나 사진을 바꾸면 이전 파일을 지운다.
 */
class RoomMealRepository(
    private val dao: MealDao,
    private val foodRepository: FoodRepository,
    private val photoStorage: PhotoStorage,
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
     * 사진이 보관소 밖의 주소(예: Photo Picker 의 content://)면 내부로 복사해서 저장한다.
     * @throws IllegalArgumentException 음식이 하나도 없거나 섭취량이 0 이하일 때
     * @throws IllegalStateException 사진을 복사할 수 없을 때, 수정할 끼니가 없을 때
     */
    override suspend fun saveMeal(meal: Meal): Long {
        validate(meal)
        val previousPhoto = if (meal.id != 0L) dao.getById(meal.id)?.meal?.photoUri else null

        val requested = meal.photoUri
        val storedPhoto = when {
            requested == null -> null
            photoStorage.isStored(requested) -> requested
            else -> try {
                photoStorage.store(requested)
            } catch (e: IOException) {
                throw IllegalStateException("사진을 저장할 수 없어요", e)
            }
        }
        val copiedNow = if (storedPhoto != null && storedPhoto != requested) storedPhoto else null

        val id = try {
            dao.saveMealWithItems(
                MealMappers.toEntity(meal).copy(photoUri = storedPhoto),
                MealMappers.toItemEntities(meal),
            )
        } catch (e: Exception) {
            if (copiedNow != null) photoStorage.delete(copiedNow) // 저장 실패 시 방금 복사한 파일 정리
            throw e
        }
        if (previousPhoto != null && previousPhoto != storedPhoto) photoStorage.delete(previousPhoto)
        return id
    }

    /** 여러 끼니를 한 트랜잭션으로 새로 저장한다(샘플 데이터용). 목록이 한 번만 갱신된다. 사진은 저장하지 않는다. */
    override suspend fun saveMeals(meals: List<Meal>) {
        meals.forEach { validate(it) }
        dao.insertAll(meals.map { MealMappers.toEntity(it).copy(id = 0, photoUri = null) to MealMappers.toItemEntities(it) })
    }

    override suspend fun deleteMeal(id: Long) {
        val photo = dao.getById(id)?.meal?.photoUri
        dao.deleteMeal(id)
        if (photo != null) photoStorage.delete(photo)
    }

    private fun validate(meal: Meal) {
        require(meal.items.isNotEmpty()) { "음식을 하나 이상 입력해야 함" }
        meal.items.forEach { item ->
            require(item.amount.isFinite() && item.amount > 0) {
                "섭취량은 0보다 커야 함: ${item.food.name} ${item.amount}"
            }
        }
    }
}
