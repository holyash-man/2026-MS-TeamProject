package kr.ac.dongguk.diet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.ac.dongguk.diet.domain.model.DataSourceInfo
import kr.ac.dongguk.diet.ui.analysis.ANALYSIS_LIST_TAG
import kr.ac.dongguk.diet.ui.analysis.AnalysisContent
import kr.ac.dongguk.diet.ui.analysis.AnalysisPeriod
import kr.ac.dongguk.diet.ui.analysis.AnalysisUiState
import kr.ac.dongguk.diet.ui.theme.DietAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalysisScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val source = DataSourceInfo(
        provider = "식품의약품안전처",
        dataset = "전국통합식품영양성분정보(음식)표준데이터",
        url = "https://www.data.go.kr",
        referenceDate = "2026-01-01",
        note = "",
    )

    /** 과제 요구사항: 영양 데이터 출처가 앱 화면에 보여야 한다. */
    @Test
    fun showsDataSource() {
        composeRule.setContent {
            DietAppTheme { AnalysisContent(state = AnalysisUiState(isLoading = false, source = source), onPeriodChange = {}) }
        }

        composeRule.onNodeWithTag(ANALYSIS_LIST_TAG).performScrollToNode(hasText("영양 데이터 출처"))
        composeRule.onNodeWithText("식품의약품안전처", substring = true).assertIsDisplayed()
    }

    /** 기간 버튼을 누르면 그 기간으로 바꾸도록 요청한다. */
    @Test
    fun periodSelector_changesPeriod() {
        var selected: AnalysisPeriod? = null
        composeRule.setContent {
            DietAppTheme {
                AnalysisContent(state = AnalysisUiState(isLoading = false), onPeriodChange = { selected = it })
            }
        }

        composeRule.onNodeWithText(AnalysisPeriod.WEEK.label).performClick()
        assertEquals(AnalysisPeriod.WEEK, selected)
    }
}
