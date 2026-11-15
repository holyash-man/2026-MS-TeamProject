package kr.ac.dongguk.diet.domain.model

/** 섭취량 단위. 음료만 ml, 나머지는 g. */
enum class FoodUnit(val symbol: String) {
    GRAM("g"),
    MILLILITER("ml"),
}

/**
 * 식약처 「전국통합식품영양성분정보(음식)표준데이터」에서 가져온 음식 1개.
 *
 * @property id selection.csv 순서(1~100). 식사 기록이 이 값을 참조하므로 바뀌지 않는다.
 * @property code 식약처 식품코드
 * @property per100 100g(음료는 100ml)당 영양성분. 원본 값 그대로.
 * @property servingSize 1인분 참고 섭취량(단위는 [unit]). 없으면 null → 화면에서 100을 기본값으로 쓴다.
 */
data class Food(
    val id: Long,
    val code: String,
    val name: String,
    val category: String,
    val unit: FoodUnit,
    val per100: Nutrition,
    val servingSize: Double?,
)
