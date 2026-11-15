package kr.ac.dongguk.diet.data.local

import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import java.time.LocalDateTime
import java.time.ZoneOffset

/** Room 엔티티 ↔ 도메인 모델 변환. 안드로이드 의존이 없어 로컬 단위 테스트로 검증한다. */
object MealMappers {

    /**
     * 시간대 없는 로컬 시각 ↔ 정수.
     * 실제 시간대와 무관하게 UTC 로 간주해 인코딩만 한다(왕복 시 같은 값이 나오고, 크기 비교가 시각 순서와 같다).
     */
    fun toEpochSecond(dateTime: LocalDateTime): Long = dateTime.toEpochSecond(ZoneOffset.UTC)

    fun fromEpochSecond(epochSecond: Long): LocalDateTime =
        LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC)

    fun toEntity(meal: Meal): MealEntity = MealEntity(
        id = meal.id,
        dateTime = toEpochSecond(meal.dateTime),
        mealType = meal.type.name,
        photoUri = meal.photoUri,
    )

    fun toItemEntities(meal: Meal): List<MealItemEntity> =
        meal.items.mapIndexed { index, item ->
            MealItemEntity(mealId = meal.id, foodId = item.food.id, amount = item.amount, position = index)
        }

    /**
     * @param foodsById foods.json 음식. 목록에 없는 음식(데이터 교체 등으로 사라진 id)을 참조하는 항목은 빠진다.
     */
    fun toDomain(row: MealWithItems, foodsById: Map<Long, Food>): Meal = Meal(
        id = row.meal.id,
        dateTime = fromEpochSecond(row.meal.dateTime),
        type = MealType.valueOf(row.meal.mealType),
        photoUri = row.meal.photoUri,
        items = row.items
            .sortedBy { it.position }
            .mapNotNull { item -> foodsById[item.foodId]?.let { MealItem(food = it, amount = item.amount) } },
    )
}
