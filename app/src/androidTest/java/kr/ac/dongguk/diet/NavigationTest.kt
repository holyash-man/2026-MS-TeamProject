package kr.ac.dongguk.diet

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.ac.dongguk.diet.ui.navigation.DietNavHost
import kr.ac.dongguk.diet.ui.theme.DietAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Compose UI 테스트 실행 환경 확인용. 화면 구현 후 B가 실제 시나리오 테스트로 교체/추가한다. */
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomTab_navigatesToAnalysis() {
        composeRule.setContent { DietAppTheme { DietNavHost() } }

        composeRule.onNodeWithText("분석").performClick()
        composeRule.onNodeWithText("식사 분석").assertIsDisplayed()
    }
}
