package kr.ac.dongguk.diet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.ac.dongguk.diet.ui.list.MealListContent
import kr.ac.dongguk.diet.ui.list.MealListUiState
import kr.ac.dongguk.diet.ui.list.MealListViewModel
import kr.ac.dongguk.diet.ui.theme.DietAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** 기록이 없으면 '샘플 데이터 불러오기' 버튼이 보이고, 누르면 불러오기가 호출된다. */
    @Test
    fun emptyList_showsSampleButton_andLoadsSample() {
        var loadCalled = false
        composeRule.setContent {
            DietAppTheme {
                MealListContent(
                    state = MealListUiState(isLoading = false),
                    onMealClick = {},
                    onLoadSample = { loadCalled = true },
                )
            }
        }

        composeRule.onNodeWithText("샘플 데이터 불러오기").assertIsDisplayed().performClick()
        assertTrue(loadCalled)
    }

    /** 날짜 머리글에 끼니 수·열량이, 줄에 끼니 종류·시각이 보이고, 누르면 그 끼니 id 로 이동한다. */
    @Test
    fun daySection_showsSummary_andOpensDetail() {
        var clickedId: Long? = null
        composeRule.setContent {
            DietAppTheme {
                MealListContent(
                    state = MealListUiState(
                        isLoading = false,
                        days = MealListViewModel.toSections(listOf(TestFoods.lunch)),
                    ),
                    onMealClick = { clickedId = it },
                    onLoadSample = {},
                )
            }
        }

        composeRule.onNodeWithText("11월 16일 (월)").assertIsDisplayed()
        composeRule.onNodeWithText("1끼 · 465 kcal").assertIsDisplayed()
        composeRule.onNodeWithText("중식 · 12:30").assertIsDisplayed().performClick()
        assertTrue(clickedId == TestFoods.lunch.id)
    }
}
