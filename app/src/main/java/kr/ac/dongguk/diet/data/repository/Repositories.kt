package kr.ac.dongguk.diet.data.repository

import kotlinx.coroutines.flow.Flow
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal

/*
 * A(데이터) ↔ B(화면) 사이의 계약 초안.
 * 팀원과 합의 후 확정한다. B는 구현이 나오기 전까지 이 인터페이스의 가짜 구현으로 화면을 만들 수 있다.
 */

/** 식약처 음식 데이터 100개 조회. */
interface FoodRepository {
    suspend fun search(query: String): List<Food>
    suspend fun getById(id: Long): Food?
}

/** 식사 기록 저장·조회. */
interface MealRepository {
    fun observeMeals(): Flow<List<Meal>>
    suspend fun getMeal(id: Long): Meal?
    suspend fun saveMeal(meal: Meal): Long
    suspend fun deleteMeal(id: Long)
}
