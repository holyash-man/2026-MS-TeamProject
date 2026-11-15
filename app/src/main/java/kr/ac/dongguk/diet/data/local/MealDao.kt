package kr.ac.dongguk.diet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    /** 전체 끼니, 최신 시각부터. 데이터가 바뀌면 다시 내보낸다. */
    @Transaction
    @Query("SELECT * FROM meals ORDER BY dateTime DESC, id DESC")
    fun observeAll(): Flow<List<MealWithItems>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getById(id: Long): MealWithItems?

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity): Int

    @Insert
    suspend fun insertItems(items: List<MealItemEntity>)

    @Query("DELETE FROM meal_items WHERE mealId = :mealId")
    suspend fun deleteItemsOf(mealId: Long)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: Long): Int

    /**
     * 끼니와 음식 목록을 한 트랜잭션으로 저장한다.
     * id 가 0이면 새로 추가, 아니면 수정(기존 음식 목록은 모두 교체). 저장된 끼니 id 를 돌려준다.
     */
    @Transaction
    suspend fun saveMealWithItems(meal: MealEntity, items: List<MealItemEntity>): Long {
        val mealId = if (meal.id == 0L) {
            insertMeal(meal)
        } else {
            check(updateMeal(meal) == 1) { "수정할 끼니가 없음: id=${meal.id}" }
            deleteItemsOf(meal.id)
            meal.id
        }
        insertItems(items.map { it.copy(id = 0, mealId = mealId) })
        return mealId
    }
}
