package kr.ac.dongguk.diet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.ui.input.MEAL_INPUT_LIST_TAG
import kr.ac.dongguk.diet.ui.input.MealInputContent
import kr.ac.dongguk.diet.ui.input.MealInputUiState
import kr.ac.dongguk.diet.ui.input.MealItemInput
import kr.ac.dongguk.diet.ui.theme.DietAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class MealInputScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun state(vararg items: MealItemInput) = MealInputUiState(
        dateTime = LocalDateTime.of(2026, 11, 16, 12, 30),
        mealType = MealType.LUNCH,
        items = items.toList(),
    )

    /** 담은 음식의 섭취량대로 영양소 미리보기 합계가 보이고 저장할 수 있다. 쌀밥 210g(315) + 된장국 300g(150) = 465kcal */
    @Test
    fun preview_showsSumOfItems_andSaveEnabled() {
        composeRule.setContent {
            DietAppTheme {
                MealInputContent(
                    state = state(MealItemInput(TestFoods.rice, "210"), MealItemInput(TestFoods.soup, "300")),
                )
            }
        }

        composeRule.onNodeWithTag(MEAL_INPUT_LIST_TAG).performScrollToNode(hasText("영양소 미리보기"))
        composeRule.onNodeWithText("영양소 미리보기").assertIsDisplayed()
        composeRule.onNodeWithText("465 kcal").assertIsDisplayed()
        composeRule.onNodeWithText("저장").assertIsEnabled()
    }

    /** 섭취량이 숫자가 아니면 그 음식은 경고가 뜨고 저장 버튼이 꺼진다. */
    @Test
    fun invalidAmount_disablesSave() {
        composeRule.setContent {
            DietAppTheme {
                MealInputContent(state = state(MealItemInput(TestFoods.rice, "abc")))
            }
        }

        composeRule.onNodeWithTag(MEAL_INPUT_LIST_TAG).performScrollToNode(hasText("섭취량을 확인하세요"))
        composeRule.onNodeWithText("섭취량을 확인하세요").assertIsDisplayed()
        composeRule.onNodeWithText("저장").assertIsNotEnabled()
    }
}
