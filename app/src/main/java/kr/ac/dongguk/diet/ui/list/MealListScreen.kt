package kr.ac.dongguk.diet.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.ac.dongguk.diet.domain.calc.DailySummary
import kr.ac.dongguk.diet.domain.model.rounded
import kr.ac.dongguk.diet.ui.AppViewModelProvider
import kr.ac.dongguk.diet.ui.components.EmptyState
import kr.ac.dongguk.diet.ui.components.MealPhoto
import kr.ac.dongguk.diet.ui.components.formatKcal
import kr.ac.dongguk.diet.ui.components.toDisplay

const val MEAL_LIST_TAG = "meal_list"

/** 식사 목록 화면: 날짜별 머리글(열량·끼니 수) 아래에 사진과 함께 끼니를 시간 순으로 보여준다. */
@Composable
fun MealListScreen(
    onMealClick: (Long) -> Unit,
    viewModel: MealListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MealListContent(
        state = state,
        onMealClick = onMealClick,
        onLoadSample = viewModel::loadSampleData,
    )
}

/** 상태만 받아 그리는 부분. UI 테스트는 이 함수에 가짜 상태를 넣어 확인한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListContent(
    state: MealListUiState,
    onMealClick: (Long) -> Unit,
    onLoadSample: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("식사 목록") }) }) { padding ->
        val body = Modifier.padding(padding).fillMaxSize()
        when {
            state.isLoading -> Box(body, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.isEmpty -> EmptyState(
                message = "아직 기록한 식사가 없어요.\n'기록' 탭에서 추가하거나 샘플 데이터를 불러오세요.",
                modifier = body,
                actionLabel = "샘플 데이터 불러오기",
                actionEnabled = !state.isLoadingSample,
                onAction = onLoadSample,
            )
            else -> MealList(state.days, onMealClick, body)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MealList(days: List<DaySection>, onMealClick: (Long) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.testTag(MEAL_LIST_TAG)) {
        days.forEach { day ->
            stickyHeader(key = "day-${day.summary.date}") { DayHeader(day.summary) }
            items(day.meals, key = { it.meal.id }) { row ->
                MealRowItem(row, onClick = { onMealClick(row.meal.id) })
                HorizontalDivider()
            }
        }
    }
}

/** 날짜 머리글: 날짜 · 끼니 수 · 그날 열량. */
@Composable
private fun DayHeader(summary: DailySummary) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(summary.date.toDisplay(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                "${summary.mealCount}끼 · ${formatKcal(summary.nutrition.rounded().kcal)}",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MealRowItem(row: MealRow, onClick: () -> Unit) {
    val meal = row.meal
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            MealPhoto(
                uri = meal.photoUri,
                contentDescription = "${meal.type.label} 사진",
                modifier = Modifier.size(56.dp),
            )
        },
        headlineContent = { Text("${meal.type.label} · ${meal.dateTime.toLocalTime().toDisplay()}") },
        supportingContent = {
            Text(
                meal.items.joinToString(", ") { it.food.name },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = { Text(formatKcal(row.nutrition.rounded().kcal)) },
    )
}
