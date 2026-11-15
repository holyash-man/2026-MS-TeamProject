package kr.ac.dongguk.diet.domain.model

/** 끼니 종류. 분석 화면에서 이 4가지로 나눠 영양소를 집계한다. */
enum class MealType(val label: String) {
    BREAKFAST("조식"),
    LUNCH("중식"),
    DINNER("석식"),
    SNACK("간식/음료"),
}
