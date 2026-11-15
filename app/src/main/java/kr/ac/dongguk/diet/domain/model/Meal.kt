package kr.ac.dongguk.diet.domain.model

import java.time.LocalDateTime

/**
 * 한 끼에 먹은 음식 하나(밥, 국, 반찬 등)와 섭취량.
 * @property amount 섭취량. 단위는 [Food.unit] (대부분 g, 음료는 ml).
 */
data class MealItem(
    val food: Food,
    val amount: Double,
)

/** 한 끼 기록. 사진 1장 + 음식/반찬 여러 개. */
data class Meal(
    val id: Long = 0,
    val dateTime: LocalDateTime,
    val type: MealType,
    val photoUri: String?,
    val items: List<MealItem>,
)
