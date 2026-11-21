package kr.ac.dongguk.diet.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kr.ac.dongguk.diet.data.food.LocalFoodRepository
import kr.ac.dongguk.diet.data.food.SampleFoods
import kr.ac.dongguk.diet.data.local.FakeMealDao
import kr.ac.dongguk.diet.data.local.RoomMealRepository
import kr.ac.dongguk.diet.data.photo.FakePhotoStorage
import kr.ac.dongguk.diet.data.sample.SampleDataSeeder
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDateTime

/** viewModelScope 가 쓰는 Dispatchers.Main 을 테스트 디스패처로 바꾼다. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/**
 * ViewModel 테스트용 저장소: 실제 구현(LocalFoodRepository, RoomMealRepository)에 가짜 DAO 만 끼운다.
 * 음식(SampleFoods): 1 쌀밥(1인분 210g), 2 김치볶음밥, 3 김치찌개, 4 배추김치, 5 아메리카노(ml)
 * — 모두 100g당 탄 20 / 단 5 / 지 1, 열량은 100kcal (아메리카노 4kcal)
 */
class TestRepositories(dispatcher: TestDispatcher, foodsJson: () -> String = { SampleFoods.json() }) {
    val foods = LocalFoodRepository(foodsJson, dispatcher)
    val photos = FakePhotoStorage()
    val meals = RoomMealRepository(FakeMealDao(), foods, photos)
    val seeder = SampleDataSeeder(foods, meals, now = { LocalDateTime.parse("2026-11-15T20:00") })

    suspend fun food(id: Long): Food = foods.getById(id)!!

    suspend fun saveMeal(at: String, type: MealType, vararg items: Pair<Long, Double>): Long =
        meals.saveMeal(
            Meal(
                dateTime = LocalDateTime.parse(at),
                type = type,
                photoUri = null,
                items = items.map { (id, amount) -> MealItem(food(id), amount) },
            ),
        )
}
