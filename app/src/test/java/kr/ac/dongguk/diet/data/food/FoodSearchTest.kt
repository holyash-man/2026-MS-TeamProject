package kr.ac.dongguk.diet.data.food

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodSearchTest {
    private val catalog = FoodCatalogParser.parse(SampleFoods.json())

    private fun names(query: String) = FoodSearch.search(catalog, query).map { it.name }

    @Test
    fun blankQuery_returnsAllInIdOrder() {
        assertEquals(listOf("쌀밥", "김치볶음밥", "김치찌개", "배추김치", "아메리카노"), names("  "))
    }

    @Test
    fun prefixMatch_ranksBeforeContains_thenShorterName() {
        // 김치찌개(앞부분, 4자) → 김치볶음밥(앞부분, 5자) → 배추김치(포함)
        assertEquals(listOf("김치찌개", "김치볶음밥", "배추김치"), names("김치"))
    }

    @Test
    fun exactMatch_comesFirst() {
        assertEquals("배추김치", names("배추김치").first())
    }

    @Test
    fun ignoresSpacesInQuery() {
        assertEquals(listOf("김치찌개"), names("김치 찌개"))
    }

    @Test
    fun matchesSourceName() {
        // 표시명에는 없지만 원본 식품명 "커피_아메리카노" 로 찾는다
        assertEquals(listOf("아메리카노"), names("커피"))
    }

    @Test
    fun matchesCategory() {
        assertEquals(listOf("아메리카노"), names("음료"))
    }

    @Test
    fun noMatch_returnsEmpty() {
        assertTrue(names("피자").isEmpty())
    }
}
