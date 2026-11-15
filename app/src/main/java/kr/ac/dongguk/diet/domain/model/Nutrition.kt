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

/** 화면 표시용 반올림: 열량은 정수, 탄단지는 소수 첫째 자리. */
fun Nutrition.rounded(): Nutrition = Nutrition(
    kcal = Math.round(kcal).toDouble(),
    carbs = Math.round(carbs * 10) / 10.0,
    protein = Math.round(protein * 10) / 10.0,
    fat = Math.round(fat * 10) / 10.0,
)

/**
 * 탄수화물·단백질·지방이 열량에서 차지하는 비율(%).
 * Atwater 계수(탄수화물·단백질 4kcal/g, 지방 9kcal/g)로 계산하며, 세 값의 합은 100이다.
 */
data class MacroRatio(val carbs: Double, val protein: Double, val fat: Double) {
    companion object {
        val ZERO = MacroRatio(0.0, 0.0, 0.0)
    }
}

fun Nutrition.macroRatio(): MacroRatio {
    val c = carbs * 4
    val p = protein * 4
    val f = fat * 9
    val total = c + p + f
    if (total <= 0) return MacroRatio.ZERO
    return MacroRatio(carbs = c / total * 100, protein = p / total * 100, fat = f / total * 100)
}
