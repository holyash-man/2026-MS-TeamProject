package kr.ac.dongguk.diet.data.food

import kr.ac.dongguk.diet.domain.model.Food

/**
 * 음식 검색 규칙.
 * - 공백·밑줄은 무시하고 대소문자를 구분하지 않는다. ("김치 찌개" → 김치찌개)
 * - 순위: 표시명 정확히 일치 > 표시명 앞부분 일치 > 표시명 포함 > 원본 식품명 포함 > 분류 포함
 * - 같은 순위는 이름이 짧은 순, 그다음 id 순
 * - 검색어가 비어 있으면 전체를 id 순으로 돌려준다.
 */
object FoodSearch {

    fun search(catalog: FoodCatalog, query: String): List<Food> {
        val q = normalize(query)
        if (q.isEmpty()) return catalog.foods

        return catalog.foods
            .mapNotNull { food -> rank(food, catalog.sourceNames[food.id].orEmpty(), q)?.let { food to it } }
            .sortedWith(compareBy({ it.second }, { it.first.name.length }, { it.first.id }))
            .map { it.first }
    }

    private fun rank(food: Food, sourceName: String, q: String): Int? {
        val name = normalize(food.name)
        return when {
            name == q -> 0
            name.startsWith(q) -> 1
            name.contains(q) -> 2
            normalize(sourceName).contains(q) -> 3
            normalize(food.category).contains(q) -> 4
            else -> null
        }
    }

    internal fun normalize(text: String): String =
        text.filterNot { it.isWhitespace() || it == '_' }.lowercase()
}
