package kr.ac.dongguk.diet.data.sample

import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 시연용 샘플 식사 기록 생성기. 학생식당 이용 패턴을 흉내 낸다.
 *
 * - 기간: [now] 기준 오늘 포함 [days]일. [now] 이후(미래) 식사는 만들지 않는다.
 * - 조식 60% · 중식 매일 · 석식 85% · 간식 45% 확률
 * - 중식·석식은 백반(밥 + 국/찌개 + 메인 반찬 + 채소 반찬 + 김치) 65%, 일품요리 35%
 * - 섭취량은 1인분 참고량(없으면 [SERVING_OVERRIDES], 그래도 없으면 100)에 0.8/1.0/1.2배
 * - 같은 [seed] 면 항상 같은 결과 (테스트·시연 재현용)
 */
class SampleMealGenerator(foods: List<Food>) {

    private val byName: Map<String, Food> = foods.associateBy { it.name }

    init {
        val missing = ALL_TEMPLATE_NAMES.filterNot { it in byName }
        require(missing.isEmpty()) { "샘플 메뉴에 쓰는 음식이 foods.json 에 없음: $missing" }
    }

    fun generate(now: LocalDateTime, days: Int = 30, seed: Int = DEFAULT_SEED): List<Meal> {
        require(days > 0) { "일수는 1 이상이어야 함: $days" }
        val random = Random(seed)
        val today = now.toLocalDate()
        val meals = mutableListOf<Meal>()

        for (offset in days - 1 downTo 0) {
            val date = today.minusDays(offset.toLong())
            if (random.nextDouble() < 0.60) meals += meal(random, date, MealType.BREAKFAST, BREAKFAST_WINDOW, pick(random, BREAKFASTS))
            meals += meal(random, date, MealType.LUNCH, LUNCH_WINDOW, mainMeal(random))
            if (random.nextDouble() < 0.85) meals += meal(random, date, MealType.DINNER, DINNER_WINDOW, mainMeal(random))
            if (random.nextDouble() < 0.45) meals += meal(random, date, MealType.SNACK, SNACK_WINDOW, pick(random, SNACKS))
        }
        return meals.filter { !it.dateTime.isAfter(now) }
    }

    private fun mainMeal(random: Random): List<String> =
        if (random.nextDouble() < 0.65) {
            listOf(pick(random, RICE), pick(random, SOUPS), pick(random, MAIN_SIDES), pick(random, VEGETABLE_SIDES), pick(random, KIMCHI))
        } else {
            pick(random, ONE_DISH)
        }

    private fun meal(random: Random, date: LocalDate, type: MealType, window: TimeWindow, names: List<String>) = Meal(
        dateTime = LocalDateTime.of(date, window.randomTime(random)),
        type = type,
        photoUri = null,
        items = names.map { name ->
            val food = byName.getValue(name)
            MealItem(food, amountFor(food, random))
        },
    )

    private fun amountFor(food: Food, random: Random): Double {
        val base = food.servingSize ?: SERVING_OVERRIDES[food.name] ?: 100.0
        val factor = pick(random, listOf(0.8, 1.0, 1.0, 1.2))
        return (base * factor).roundToInt().toDouble().coerceAtLeast(1.0)
    }

    private fun <T> pick(random: Random, options: List<T>): T = options[random.nextInt(options.size)]

    /** [start, end) 사이 5분 단위 시각. */
    private class TimeWindow(val start: LocalTime, val end: LocalTime) {
        fun randomTime(random: Random): LocalTime {
            val slots = (end.toSecondOfDay() - start.toSecondOfDay()) / 300
            return start.plusMinutes(5L * random.nextInt(slots))
        }
    }

    companion object {
        const val DEFAULT_SEED = 2026

        // 끼니 시간대는 MealTypeClassifier 의 추천 시간대 안에 있다.
        private val BREAKFAST_WINDOW = TimeWindow(LocalTime.of(7, 30), LocalTime.of(9, 30))
        private val LUNCH_WINDOW = TimeWindow(LocalTime.of(11, 40), LocalTime.of(13, 20))
        private val DINNER_WINDOW = TimeWindow(LocalTime.of(17, 50), LocalTime.of(19, 40))
        private val SNACK_WINDOW = TimeWindow(LocalTime.of(15, 0), LocalTime.of(16, 50))

        /** 원본에 1인분 참고량이 없는 음식의 샘플용 섭취량. */
        private val SERVING_OVERRIDES = mapOf(
            "쌀밥" to 210.0, "비빔국수" to 400.0, "소고기장조림" to 50.0, "고등어조림" to 120.0,
            "배추김치" to 40.0, "총각김치" to 40.0, "열무김치" to 40.0,
            "햄치즈샌드위치" to 150.0, "인절미" to 100.0, "삶은달걀" to 50.0,
            "아메리카노" to 355.0, "카페라떼" to 355.0, "과일주스" to 250.0,
        )

        private val RICE = listOf("쌀밥", "쌀밥", "잡곡밥", "현미밥")
        private val SOUPS = listOf(
            "미역국", "콩나물국", "소고기무국", "북어국", "두부된장국", "달걀국", "육개장", "김칫국", "어묵국",
            "김치찌개", "된장찌개", "순두부찌개", "청국장찌개", "동태찌개",
        )
        private val MAIN_SIDES = listOf(
            "제육볶음", "소불고기", "닭갈비", "돈가스", "닭강정", "탕수육", "떡갈비", "찜닭", "돼지갈비찜",
            "고등어구이", "삼치구이", "고등어조림", "오징어볶음", "동태전",
            "달걀말이", "두부조림", "마파두부", "어묵볶음", "소시지볶음", "잡채", "동그랑땡",
        )
        private val VEGETABLE_SIDES = listOf(
            "감자볶음", "감자조림", "연근조림", "시금치나물", "콩나물무침", "숙주나물", "고사리나물",
            "무생채", "오이무침", "도토리묵무침", "호박전", "감자샐러드", "마카로니샐러드", "양배추샐러드",
            "멸치볶음", "콩자반",
        )
        private val KIMCHI = listOf("배추김치", "배추김치", "깍두기", "총각김치", "열무김치")
        private val ONE_DISH = listOf(
            listOf("비빔밥", "콩나물국"), listOf("카레라이스", "배추김치"), listOf("오므라이스", "양배추샐러드"),
            listOf("짜장밥", "단무지"), listOf("제육덮밥", "미역국"), listOf("불고기덮밥", "어묵국"),
            listOf("김치볶음밥", "달걀국"), listOf("순대국밥", "깍두기"), listOf("잔치국수", "김밥"),
            listOf("비빔국수", "군만두"), listOf("물냉면"), listOf("비빔냉면"), listOf("짜장면", "단무지"),
            listOf("짬뽕", "단무지"), listOf("우동", "김밥"), listOf("라면", "김밥"), listOf("떡국", "배추김치"),
            listOf("쌀밥", "부대찌개", "배추김치"), listOf("쌀밥", "갈비탕", "깍두기"),
        )
        private val BREAKFASTS = listOf(
            listOf("햄치즈샌드위치", "아메리카노"), listOf("삶은달걀", "찐고구마", "카페라떼"), listOf("김밥"),
            listOf("쌀밥", "미역국", "달걀말이", "배추김치"), listOf("소보로빵", "카페라떼"), listOf("호박죽"),
            listOf("쌀밥", "콩나물국", "달걀찜", "깍두기"), listOf("인절미", "식혜"),
        )
        private val SNACKS = listOf(
            listOf("아메리카노"), listOf("카페라떼"), listOf("과일주스"), listOf("식혜"), listOf("미숫가루"),
            listOf("떡볶이", "김말이튀김"), listOf("햄버거"), listOf("인절미"), listOf("닭튀김"), listOf("소보로빵", "아메리카노"),
        )

        val ALL_TEMPLATE_NAMES: Set<String> =
            (RICE + SOUPS + MAIN_SIDES + VEGETABLE_SIDES + KIMCHI + (ONE_DISH + BREAKFASTS + SNACKS).flatten()).toSet()
    }
}
