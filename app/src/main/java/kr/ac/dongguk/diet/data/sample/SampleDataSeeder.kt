package kr.ac.dongguk.diet.data.sample

import kotlinx.coroutines.flow.first
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.data.repository.MealRepository
import java.time.LocalDateTime

/**
 * 시연용 샘플 식사를 저장소에 넣는다.
 * 사용자의 실제 기록과 섞이지 않도록 기록이 하나도 없을 때만 넣는다.
 */
class SampleDataSeeder(
    private val foodRepository: FoodRepository,
    private val mealRepository: MealRepository,
    private val now: () -> LocalDateTime = LocalDateTime::now,
) {
    /** 넣은 끼니 수. 이미 기록이 있으면 아무것도 하지 않고 0. */
    suspend fun seedIfEmpty(days: Int = 30): Int {
        if (mealRepository.observeMeals().first().isNotEmpty()) return 0
        val meals = SampleMealGenerator(foodRepository.getAll()).generate(now(), days)
        // 한 트랜잭션으로 저장해야 목록이 한 번만 갱신된다.
        // (하나씩 저장하면 가장 오래된 날이 먼저 그려져, 목록이 그 위치에 머무는 문제가 있었다)
        mealRepository.saveMeals(meals)
        return meals.size
    }
}
