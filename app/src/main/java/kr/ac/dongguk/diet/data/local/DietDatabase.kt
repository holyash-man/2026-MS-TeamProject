package kr.ac.dongguk.diet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** 식사 기록 DB. 음식 100개는 assets/foods.json 에 있으므로 여기에 넣지 않는다. */
@Database(
    entities = [MealEntity::class, MealItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class DietDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    companion object {
        const val NAME = "diet.db"

        fun create(context: Context): DietDatabase =
            Room.databaseBuilder(context.applicationContext, DietDatabase::class.java, NAME).build()
    }
}
