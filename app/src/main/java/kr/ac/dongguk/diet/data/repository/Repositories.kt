package kr.ac.dongguk.diet.data.repository

import kotlinx.coroutines.flow.Flow
import kr.ac.dongguk.diet.domain.model.DataSourceInfo
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal

/*
 * A(데이터) ↔ B(화면) 사이의 계약 초안.
 * 팀원과 합의 후 확정한다. B는 구현이 나오기 전까지 이 인터페이스의 가짜 구현으로 화면을 만들 수 있다.
 */

/** 식약처 음식 데이터 100개 조회 (읽기 전용). */
interface FoodRepository {
    /** 전체 음식, id 순. */
    suspend fun getAll(): List<Food>

    /**
     * 이름·원본 식품명·분류로 검색한다. 공백과 대소문자는 무시한다.
     * 정확히 일치 → 앞부분 일치 → 포함 순으로 정렬하고, 검색어가 비어 있으면 전체를 돌려준다.
     */
    suspend fun search(query: String): List<Food>

    suspend fun getById(id: Long): Food?

    /** 화면에 표시할 데이터 출처. */
    suspend fun getSourceInfo(): DataSourceInfo
}

/** 식사 기록 저장·조회. */
interface MealRepository {
    /** 전체 식사, 최신 시각부터. 저장·삭제하면 새 목록을 다시 내보낸다. */
    fun observeMeals(): Flow<List<Meal>>

    suspend fun getMeal(id: Long): Meal?

    /**
     * id 가 0이면 새로 저장, 아니면 수정. 저장된 id 를 돌려준다.
     * 사진은 Photo Picker 등에서 받은 URI 를 그대로 넘기면 앱 내부로 복사해 저장한다.
     * 음식이 없거나 섭취량이 0 이하면 IllegalArgumentException, 사진을 복사할 수 없으면 IllegalStateException.
     */
    suspend fun saveMeal(meal: Meal): Long

    /** 여러 끼니를 한 번에 새로 저장(시연용 샘플 데이터). 목록이 한 번만 갱신된다. */
    suspend fun saveMeals(meals: List<Meal>)

    /** 끼니와 그 사진 파일을 함께 지운다. */
    suspend fun deleteMeal(id: Long)
}
