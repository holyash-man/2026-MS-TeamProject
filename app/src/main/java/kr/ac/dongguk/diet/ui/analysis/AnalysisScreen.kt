package kr.ac.dongguk.diet.ui.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.ac.dongguk.diet.domain.calc.PeriodAnalysis
import kr.ac.dongguk.diet.domain.model.DataSourceInfo
import kr.ac.dongguk.diet.domain.model.rounded
import kr.ac.dongguk.diet.ui.AppViewModelProvider
import kr.ac.dongguk.diet.ui.components.NutritionSummaryCard
import kr.ac.dongguk.diet.ui.components.formatGram
import kr.ac.dongguk.diet.ui.components.formatKcal
import kr.ac.dongguk.diet.ui.components.formatPercent
import kr.ac.dongguk.diet.ui.components.toDisplay

const val ANALYSIS_LIST_TAG = "analysis_list"

/** 식사 분석 화면: 기간 열량 총량, 끼니 종류별 영양 분석, 영양 데이터 출처. */
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AnalysisContent(state = state, onPeriodChange = viewModel::onPeriodChange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisContent(
    state: AnalysisUiState,
    onPeriodChange: (AnalysisPeriod) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("식사 분석") }) }) { padding ->
        val body = Modifier.padding(padding).fillMaxSize()
        if (state.isLoading) {
            Box(body, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            body.testTag(ANALYSIS_LIST_TAG),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PeriodSelector(state.period, onPeriodChange) }
            state.analysis?.let { analysis ->
                item { PeriodOverview(analysis) }
                item { NutritionSummaryCard("${state.period.label} 섭취 영양소", analysis.total, ratio = state.macroRatio) }
            }
            item { Text("끼니별 분석", style = MaterialTheme.typography.titleMedium) }
            items(state.byType, key = { it.summary.type }) { row -> MealTypeCard(row) }
            item { SourceCard(state.source) }
        }
    }
}

@Composable
private fun PeriodSelector(selected: AnalysisPeriod, onChange: (AnalysisPeriod) -> Unit) {
    val periods = AnalysisPeriod.entries
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                selected = period == selected,
                onClick = { onChange(period) },
                shape = SegmentedButtonDefaults.itemShape(index, periods.size),
            ) { Text(period.label) }
        }
    }
}

/** 기간 · 칼로리 총량 · 하루 평균 · 끼니 수. */
@Composable
private fun PeriodOverview(analysis: PeriodAnalysis) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${analysis.from.toDisplay()} ~ ${analysis.to.toDisplay()}",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(4.dp))
            Text("칼로리 총량", style = MaterialTheme.typography.titleSmall)
            Text(formatKcal(analysis.total.rounded().kcal), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "하루 평균 ${formatKcal(analysis.averageDailyKcal)} · 총 ${analysis.mealCount}끼",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** 끼니 종류 한 줄: 끼니 수, 열량과 기간 전체 중 비율, 탄단지. */
@Composable
private fun MealTypeCard(row: MealTypeRow) {
    val summary = row.summary
    val n = summary.nutrition.rounded()
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(summary.type.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("${summary.mealCount}끼 · ${formatKcal(n.kcal)} (${formatPercent(row.kcalShare)})")
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (row.kcalShare / 100).toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "탄수화물 ${formatGram(n.carbs)} · 단백질 ${formatGram(n.protein)} · 지방 ${formatGram(n.fat)}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** 영양 데이터 출처 (과제 필수 표시 항목). */
@Composable
private fun SourceCard(source: DataSourceInfo?) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("영양 데이터 출처", style = MaterialTheme.typography.titleSmall)
            if (source == null) {
                Text("불러오는 중…", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("${source.provider} · ${source.dataset}", style = MaterialTheme.typography.bodySmall)
                Text("기준일: ${source.referenceDate}", style = MaterialTheme.typography.bodySmall)
                Text(source.url, style = MaterialTheme.typography.bodySmall)
                if (source.note.isNotBlank()) Text(source.note, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
