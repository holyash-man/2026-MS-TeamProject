package kr.ac.dongguk.diet.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * 끼니 1건.
 * @property dateTime 식사 시각. 시간대 없는 로컬 시각을 [MealMappers.toEpochSecond] 로 바꾼 값(정렬·기간 조회용).
 * @property mealType [kr.ac.dongguk.diet.domain.model.MealType] 이름 (BREAKFAST, LUNCH, DINNER, SNACK)
 */
@Entity(
    tableName = "meals",
    indices = [Index("dateTime")],
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTime: Long,
    val mealType: String,
    val photoUri: String?,
)

/**
 * 끼니에 담긴 음식 하나. 음식 정보는 저장하지 않고 foods.json 의 id 만 참조한다.
 * @property position 입력한 순서(0부터). 상세 화면에서 같은 순서로 보여주기 위함.
 */
@Entity(
    tableName = "meal_items",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mealId")],
)
data class MealItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val foodId: Long,
    val amount: Double,
    val position: Int,
)

data class MealWithItems(
    @Embedded val meal: MealEntity,
    @Relation(parentColumn = "id", entityColumn = "mealId")
    val items: List<MealItemEntity>,
)
