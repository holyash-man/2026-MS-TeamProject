package kr.ac.dongguk.diet.data.food

import kr.ac.dongguk.diet.domain.model.FoodUnit
import kr.ac.dongguk.diet.domain.model.Nutrition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoodCatalogParserTest {

    @Test
    fun parse_readsFoodsAndSource() {
        val catalog = FoodCatalogParser.parse(SampleFoods.json())

        assertEquals(5, catalog.foods.size)
        assertEquals("식품의약품안전처", catalog.source.provider)
        assertEquals("2026-09-29", catalog.source.referenceDate)

        val rice = catalog.findById(1)!!
        assertEquals("쌀밥", rice.name)
        assertEquals(FoodUnit.GRAM, rice.unit)
        assertEquals(Nutrition(kcal = 100.0, carbs = 20.0, protein = 5.0, fat = 1.0), rice.per100)
        assertEquals(210.0, rice.servingSize!!, 0.0)
    }

    @Test
    fun parse_mapsMlUnitAndNullServing() {
        val coffee = FoodCatalogParser.parse(SampleFoods.json()).findById(5)!!

        assertEquals(FoodUnit.MILLILITER, coffee.unit)
        assertNull(coffee.servingSize)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_rejectsDuplicateIds() {
        val foods = listOf(SampleFoods.food(1, "쌀밥"), SampleFoods.food(1, "잡곡밥")).joinToString(",")
        FoodCatalogParser.parse(SampleFoods.json(foods))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_rejectsNegativeNutrition() {
        FoodCatalogParser.parse(SampleFoods.json(SampleFoods.food(1, "쌀밥", kcal = -1.0)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_rejectsUnknownUnit() {
        FoodCatalogParser.parse(SampleFoods.json(SampleFoods.food(1, "쌀밥", unit = "oz")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_rejectsMalformedJson() {
        FoodCatalogParser.parse("{ not json")
    }
}
