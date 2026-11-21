package kr.ac.dongguk.diet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kr.ac.dongguk.diet.domain.model.MacroRatio
import kr.ac.dongguk.diet.domain.model.Nutrition
import kr.ac.dongguk.diet.domain.model.macroRatio
import kr.ac.dongguk.diet.domain.model.rounded

/** 탄수화물·단백질·지방 표시 색. 차트·범례가 같은 색을 쓰도록 한 곳에 둔다. */
object MacroColors {
    val carbs: Color @Composable get() = MaterialTheme.colorScheme.primary
    val protein: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val fat: Color @Composable get() = MaterialTheme.colorScheme.secondary
}

/**
 * 열량 + 탄단지 요약 카드. 입력 화면 미리보기, 상세 화면 합계, 분석 화면 총량에서 같이 쓴다.
 * 값은 [Nutrition.rounded] 를 거쳐 표시한다.
 */
@Composable
fun NutritionSummaryCard(
    title: String,
    nutrition: Nutrition,
    modifier: Modifier = Modifier,
    ratio: MacroRatio = nutrition.macroRatio(),
) {
    val shown = nutrition.rounded()
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(formatKcal(shown.kcal), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            MacroRatioBar(ratio)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroValue("탄수화물", shown.carbs, MacroColors.carbs)
                MacroValue("단백질", shown.protein, MacroColors.protein)
                MacroValue("지방", shown.fat, MacroColors.fat)
            }
        }
    }
}

@Composable
private fun MacroValue(label: String, gram: Double, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.size(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
        Text(formatGram(gram), style = MaterialTheme.typography.bodyLarge)
    }
}

/** 탄단지 열량 비율 가로 막대. 셋 다 0이면 빈 막대. */
@Composable
fun MacroRatioBar(ratio: MacroRatio, modifier: Modifier = Modifier) {
    val parts = listOf(
        ratio.carbs to MacroColors.carbs,
        ratio.protein to MacroColors.protein,
        ratio.fat to MacroColors.fat,
    ).filter { it.first > 0 }
    Row(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        parts.forEach { (percent, color) ->
            Box(Modifier.weight(percent.toFloat()).height(10.dp).background(color))
        }
    }
}
