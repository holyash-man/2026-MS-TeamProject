package kr.ac.dongguk.diet.data.food

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs

/**
 * 실제 앱에 들어가는 assets/foods.json 의 품질 검사.
 * (로컬 단위 테스트의 작업 디렉터리는 app 모듈 폴더)
 */
class FoodsAssetTest {
    private val catalog = FoodCatalogParser.parse(File("src/main/assets/foods.json").readText())

    @Test
    fun has100FoodsWithIds1To100() {
        assertEquals(100, catalog.foods.size)
        assertEquals((1L..100L).toList(), catalog.foods.map { it.id })
    }

    @Test
    fun displayNamesAreUnique() {
        val duplicated = catalog.foods.groupBy { it.name }.filterValues { it.size > 1 }.keys
        assertTrue("중복 이름: $duplicated", duplicated.isEmpty())
    }

    @Test
    fun kcalMatchesMacros_within15Percent() {
        // 탄수화물·단백질 4kcal/g, 지방 9kcal/g (Atwater) 로 계산한 값과 원본 열량 비교
        val outliers = catalog.foods.filter { f ->
            val n = f.per100
            val fromMacros = n.carbs * 4 + n.protein * 4 + n.fat * 9
            n.kcal > 0 && abs(fromMacros - n.kcal) / n.kcal > 0.15
        }.map { it.name }
        assertTrue("열량 불일치: $outliers", outliers.isEmpty())
    }

    @Test
    fun sourceIsMfdsDataset() {
        assertEquals("식품의약품안전처", catalog.source.provider)
        assertTrue(catalog.source.url.startsWith("https://www.data.go.kr/"))
        assertTrue(catalog.source.referenceDate.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }
}
