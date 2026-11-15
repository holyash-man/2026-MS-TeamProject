package kr.ac.dongguk.diet.domain.model

/** 열량(kcal)과 탄수화물·단백질·지방(g). */
data class Nutrition(
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
) {
    operator fun plus(other: Nutrition) = Nutrition(
        kcal = kcal + other.kcal,
        carbs = carbs + other.carbs,
        protein = protein + other.protein,
        fat = fat + other.fat,
    )

    companion object {
        val ZERO = Nutrition(0.0, 0.0, 0.0, 0.0)
    }
}
