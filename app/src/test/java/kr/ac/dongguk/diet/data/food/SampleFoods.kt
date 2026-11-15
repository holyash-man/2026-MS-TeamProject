package kr.ac.dongguk.diet.data.food

/** 테스트용 foods.json 조각 (실제 파일과 같은 형식). */
object SampleFoods {
    fun json(foods: String = DEFAULT_FOODS) = """
        {
          "source": {
            "provider": "식품의약품안전처",
            "dataset": "전국통합식품영양성분정보(음식)표준데이터",
            "url": "https://www.data.go.kr/data/15100070/standard.do",
            "referenceDate": "2026-09-29",
            "note": "테스트"
          },
          "foods": [$foods]
        }
    """.trimIndent()

    fun food(
        id: Long,
        name: String,
        sourceName: String = name,
        category: String = "밥",
        unit: String = "g",
        kcal: Double = 100.0,
        servingSize: String = "null",
    ) = """
        {"id": $id, "code": "D$id", "name": "$name", "sourceName": "$sourceName",
         "category": "$category", "unit": "$unit",
         "per100": {"kcal": $kcal, "carbs": 20.0, "protein": 5.0, "fat": 1.0},
         "servingSize": $servingSize, "origin": "가정식(분석 함량)"}
    """.trimIndent()

    val DEFAULT_FOODS = listOf(
        food(1, "쌀밥", servingSize = "210.0"),
        food(2, "김치볶음밥", sourceName = "볶음밥_김치"),
        food(3, "김치찌개", category = "찌개"),
        food(4, "배추김치", category = "김치·절임"),
        food(5, "아메리카노", sourceName = "커피_아메리카노", category = "음료", unit = "ml", kcal = 4.0),
    ).joinToString(",")
}
