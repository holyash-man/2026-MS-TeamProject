package kr.ac.dongguk.diet.data.food

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalFoodRepositoryTest {

    @Test
    fun getById_returnsFoodOrNull() = runTest {
        val repo = LocalFoodRepository({ SampleFoods.json() }, StandardTestDispatcher(testScheduler))

        assertEquals("김치찌개", repo.getById(3)?.name)
        assertNull(repo.getById(999))
    }

    @Test
    fun jsonIsLoadedOnlyOnce() = runTest {
        var loads = 0
        val repo = LocalFoodRepository({ loads++; SampleFoods.json() }, StandardTestDispatcher(testScheduler))

        repo.getAll()
        repo.search("김치")
        repo.getSourceInfo()

        assertEquals(1, loads)
    }

    @Test
    fun getSourceInfo_returnsDatasetInfo() = runTest {
        val repo = LocalFoodRepository({ SampleFoods.json() }, StandardTestDispatcher(testScheduler))

        val source = repo.getSourceInfo()
        assertEquals("전국통합식품영양성분정보(음식)표준데이터", source.dataset)
        assertEquals("https://www.data.go.kr/data/15100070/standard.do", source.url)
    }
}
