package kr.ac.dongguk.diet.data.food

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.domain.model.DataSourceInfo
import kr.ac.dongguk.diet.domain.model.Food

/**
 * foods.json 기반 [FoodRepository].
 * 음식 100개는 읽기 전용이라 Room 에 넣지 않고, 처음 접근할 때 한 번 파싱해 메모리에 둔다.
 *
 * @param loadJson foods.json 내용을 돌려주는 함수. 앱에서는 assets, 테스트에서는 문자열/파일.
 */
class LocalFoodRepository(
    private val loadJson: () -> String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FoodRepository {

    private val mutex = Mutex()

    @Volatile
    private var cache: FoodCatalog? = null

    private suspend fun catalog(): FoodCatalog =
        cache ?: mutex.withLock {
            cache ?: withContext(ioDispatcher) { FoodCatalogParser.parse(loadJson()) }
                .also { cache = it }
        }

    override suspend fun getAll(): List<Food> = catalog().foods

    override suspend fun search(query: String): List<Food> = FoodSearch.search(catalog(), query)

    override suspend fun getById(id: Long): Food? = catalog().findById(id)

    override suspend fun getSourceInfo(): DataSourceInfo = catalog().source
}
