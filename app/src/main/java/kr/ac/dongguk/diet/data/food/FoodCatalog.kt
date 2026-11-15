package kr.ac.dongguk.diet.data.food

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kr.ac.dongguk.diet.domain.model.DataSourceInfo
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.FoodUnit
import kr.ac.dongguk.diet.domain.model.Nutrition

/**
 * foods.json 을 읽어 만든 음식 목록 + 출처.
 * @property sourceNames id → 식약처 원본 식품명(예: "커피_아메리카노"). 검색 보조용.
 */
data class FoodCatalog(
    val source: DataSourceInfo,
    val foods: List<Food>,
    val sourceNames: Map<Long, String> = emptyMap(),
) {
    private val byId: Map<Long, Food> = foods.associateBy { it.id }

    fun findById(id: Long): Food? = byId[id]
}

/**
 * assets/foods.json(tools/food-data/build_foods.py 가 생성) 파서.
 * 안드로이드에 의존하지 않으므로 로컬 단위 테스트로 검증할 수 있다.
 */
object FoodCatalogParser {
    private val json = Json { ignoreUnknownKeys = true }

    /** @throws IllegalArgumentException 형식이 잘못되었거나 값이 규칙에 맞지 않을 때 */
    fun parse(text: String): FoodCatalog {
        val dto = try {
            json.decodeFromString<FoodFileDto>(text)
        } catch (e: kotlinx.serialization.SerializationException) {
            throw IllegalArgumentException("foods.json 형식 오류: ${e.message}", e)
        }
        val foods = dto.foods.map { it.toDomain() }

        val duplicateIds = foods.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicateIds.isEmpty()) { "중복된 음식 id: $duplicateIds" }

        return FoodCatalog(
            source = dto.source.toDomain(),
            foods = foods.sortedBy { it.id },
            sourceNames = dto.foods.associate { it.id to it.sourceName },
        )
    }

    private fun FoodDto.toDomain(): Food {
        val n = per100
        require(n.kcal >= 0 && n.carbs >= 0 && n.protein >= 0 && n.fat >= 0) {
            "음수 영양값: id=$id $name"
        }
        require(servingSize == null || servingSize > 0) { "잘못된 1인분 중량: id=$id $name" }
        return Food(
            id = id,
            code = code,
            name = name,
            category = category,
            unit = when (unit) {
                "g" -> FoodUnit.GRAM
                "ml" -> FoodUnit.MILLILITER
                else -> throw IllegalArgumentException("알 수 없는 단위 '$unit': id=$id $name")
            },
            per100 = Nutrition(kcal = n.kcal, carbs = n.carbs, protein = n.protein, fat = n.fat),
            servingSize = servingSize,
        )
    }

    private fun SourceDto.toDomain() = DataSourceInfo(
        provider = provider,
        dataset = dataset,
        url = url,
        referenceDate = referenceDate,
        note = note,
    )
}

@Serializable
private data class FoodFileDto(
    val source: SourceDto,
    val foods: List<FoodDto>,
)

@Serializable
private data class SourceDto(
    val provider: String,
    val dataset: String,
    val url: String,
    val referenceDate: String,
    val note: String = "",
)

@Serializable
private data class FoodDto(
    val id: Long,
    val code: String,
    val name: String,
    val sourceName: String = "",
    val category: String,
    val unit: String,
    val per100: NutritionDto,
    val servingSize: Double? = null,
)

@Serializable
private data class NutritionDto(
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
)
