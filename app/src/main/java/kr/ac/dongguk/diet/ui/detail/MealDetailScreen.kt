package kr.ac.dongguk.diet.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.rounded
import kr.ac.dongguk.diet.ui.AppViewModelProvider
import kr.ac.dongguk.diet.ui.components.EmptyState
import kr.ac.dongguk.diet.ui.components.MealPhoto
import kr.ac.dongguk.diet.ui.components.NutritionSummaryCard
import kr.ac.dongguk.diet.ui.components.formatAmount
import kr.ac.dongguk.diet.ui.components.formatGram
import kr.ac.dongguk.diet.ui.components.formatKcal
import kr.ac.dongguk.diet.ui.components.toDisplay
import kr.ac.dongguk.diet.ui.components.toFullDisplay

/** 식사 상세 화면: 입력했던 내용과 함께 음식별·끼니 전체 영양소와 열량을 보여준다. */
@Composable
fun MealDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MealDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    MealDetailContent(state = state, onBack = onBack, onEdit = onEdit, onDelete = viewModel::delete)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailContent(
    state: MealDetailUiState,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val meal = state.meal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("식사 상세") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    if (meal != null) {
                        IconButton(onClick = { onEdit(meal.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "수정")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "삭제")
                        }
                    }
                },
            )
        },
    ) { padding ->
        val body = Modifier.padding(padding).fillMaxSize()
        when {
            state.isLoading -> Box(body, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            meal == null -> EmptyState("식사 기록을 찾을 수 없어요.", body)
            else -> MealDetailBody(state, meal, body)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("이 식사 기록을 삭제할까요?") },
            text = { Text("삭제하면 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun MealDetailBody(state: MealDetailUiState, meal: Meal, modifier: Modifier) {
    LazyColumn(
        modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MealPhoto(
                uri = meal.photoUri,
                contentDescription = "${meal.type.label} 사진",
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
        }
        item {
            Text(meal.dateTime.toLocalDate().toFullDisplay(), style = MaterialTheme.typography.titleMedium)
            Text(
                "${meal.type.label} · ${meal.dateTime.toLocalTime().toDisplay()}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item { NutritionSummaryCard("이 끼니 영양 섭취량", state.total, ratio = state.macroRatio) }
        item { Text("먹은 음식", style = MaterialTheme.typography.titleMedium) }
        items(state.items) { row ->
            val n = row.nutrition.rounded()
            ListItem(
                headlineContent = { Text(row.item.food.name) },
                supportingContent = {
                    Text(
                        "${formatAmount(row.item.amount, row.item.food.unit)} · " +
                            "탄 ${formatGram(n.carbs)} · 단 ${formatGram(n.protein)} · 지 ${formatGram(n.fat)}",
                    )
                },
                trailingContent = { Text(formatKcal(n.kcal)) },
            )
        }
    }
}
