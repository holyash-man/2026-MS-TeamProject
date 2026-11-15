package kr.ac.dongguk.diet.domain.model

/**
 * 식약처 식품영양성분 DB에서 가져온 음식 1개.
 * 영양 값은 DB 원본과 같이 100g 기준이다.
 */
data class Food(
    val id: Long,
    val name: String,
    val category: String,
    val per100g: Nutrition,
)
