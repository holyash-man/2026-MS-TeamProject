package kr.ac.dongguk.diet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green40,
    secondary = Amber40,
    tertiary = Blue40,
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    secondary = Amber80,
    tertiary = Blue80,
)

/** 앱 테마. 화면은 색·글꼴을 직접 쓰지 않고 MaterialTheme 값을 참조한다. 다크 모드는 시스템 설정을 따른다. */
@Composable
fun DietAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DietTypography,
        content = content,
    )
}
