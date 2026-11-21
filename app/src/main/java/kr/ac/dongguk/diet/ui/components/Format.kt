package kr.ac.dongguk.diet.ui.components

import kr.ac.dongguk.diet.domain.model.FoodUnit
import kr.ac.dongguk.diet.ui.input.MealInputViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * 화면 표시용 글자 변환. 계산은 domain/calc 에서 끝난 값을 받고, 여기서는 반올림·단위만 붙인다.
 * 반올림 규칙은 Nutrition.rounded() 와 같다(열량 정수, 탄단지 소수 첫째 자리).
 */

private val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** 11월 16일 (월) */
fun LocalDate.toDisplay(): String = format(dateFormatter)

/** 2026년 11월 16일 (월) */
fun LocalDate.toFullDisplay(): String = format(fullDateFormatter)

/** 12:30 */
fun LocalTime.toDisplay(): String = format(timeFormatter)

/** 1,234 kcal */
fun formatKcal(kcal: Double): String = String.format(Locale.KOREA, "%,d kcal", Math.round(kcal))

/** 12.3 g (반올림된 값을 받는다) */
fun formatGram(gram: Double): String = String.format(Locale.KOREA, "%.1f g", gram)

/** 45% */
fun formatPercent(percent: Double): String = String.format(Locale.KOREA, "%.0f%%", percent)

/** 210g, 250ml */
fun formatAmount(amount: Double, unit: FoodUnit): String = MealInputViewModel.formatAmount(amount) + unit.symbol
