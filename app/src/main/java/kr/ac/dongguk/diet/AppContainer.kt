package kr.ac.dongguk.diet

import android.content.Context
import kr.ac.dongguk.diet.data.food.LocalFoodRepository
import kr.ac.dongguk.diet.data.local.DietDatabase
import kr.ac.dongguk.diet.data.local.RoomMealRepository
import kr.ac.dongguk.diet.data.photo.InternalPhotoStorage
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.data.sample.SampleDataSeeder

/**
 * 수동 DI 컨테이너. 화면(ViewModel)은 여기서 Repository 를 받아 쓴다.
 * 사용 예: (LocalContext.current.applicationContext as DietApplication).container.foodRepository
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val foodRepository: FoodRepository = LocalFoodRepository(
        loadJson = { appContext.assets.open(FOODS_ASSET).bufferedReader().use { it.readText() } },
    )

    private val database: DietDatabase by lazy { DietDatabase.create(appContext) }

    val mealRepository: MealRepository by lazy {
        RoomMealRepository(database.mealDao(), foodRepository, InternalPhotoStorage(appContext))
    }

    /** 시연용 샘플 식사(최근 30일). 기록이 없을 때만 넣는다. */
    val sampleDataSeeder: SampleDataSeeder by lazy { SampleDataSeeder(foodRepository, mealRepository) }

    companion object {
        const val FOODS_ASSET = "foods.json"
    }
}
