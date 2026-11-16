package kr.ac.dongguk.diet.ui.input

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.domain.model.rounded
import kr.ac.dongguk.diet.ui.AppViewModelProvider
import kr.ac.dongguk.diet.ui.components.MealPhoto
import kr.ac.dongguk.diet.ui.components.NutritionSummaryCard
import kr.ac.dongguk.diet.ui.components.formatKcal
import kr.ac.dongguk.diet.ui.components.toDisplay
import java.time.LocalDate
import java.time.LocalTime

const val MEAL_INPUT_LIST_TAG = "meal_input_list"

/** 검색 결과는 이 개수까지만 보여준다(음식 100개 중 부분 일치). */
private const val MAX_SEARCH_RESULTS = 8

/**
 * 식사 입력 화면(새로 작성 / 수정).
 * @param onSaved 저장이 끝나면 저장된 끼니 id 와 수정 모드였는지를 넘긴다. 이동은 내비게이션이 정한다.
 * @param onBack 수정 모드일 때만 뒤로 가기 버튼을 보여준다(새 기록은 하단 탭 화면).
 */
@Composable
fun MealInputScreen(
    onSaved: (mealId: Long, wasEditing: Boolean) -> Unit,
    onBack: (() -> Unit)?,
    viewModel: MealInputViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedMealId) {
        val id = state.savedMealId ?: return@LaunchedEffect
        val wasEditing = state.isEditing
        viewModel.onSavedHandled()
        onSaved(id, wasEditing)
    }

    // 갤러리(Photo Picker): 저장소 권한이 필요 없다 (docs/decisions.md D3).
    // 받은 URI 를 그대로 넘기고, 내부 저장소 복사는 저장 단계에서 처리한다 (D4).
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoChange(uri.toString())
    }

    MealInputContent(
        state = state,
        onBack = onBack,
        onDateChange = viewModel::onDateChange,
        onTimeChange = viewModel::onTimeChange,
        onMealTypeChange = viewModel::onMealTypeChange,
        onPickPhoto = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemovePhoto = { viewModel.onPhotoChange(null) },
        onQueryChange = viewModel::onQueryChange,
        onAddFood = viewModel::addFood,
        onAmountChange = viewModel::onAmountChange,
        onRemoveItem = viewModel::removeItem,
        onSave = viewModel::save,
        onErrorShown = viewModel::onErrorShown,
    )
}

/** 상태만 받아 그리는 부분. UI 테스트는 이 함수에 가짜 상태를 넣어 확인한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealInputContent(
    state: MealInputUiState,
    onBack: (() -> Unit)? = null,
    onDateChange: (LocalDate) -> Unit = {},
    onTimeChange: (LocalTime) -> Unit = {},
    onMealTypeChange: (MealType) -> Unit = {},
    onPickPhoto: () -> Unit = {},
    onRemovePhoto: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onAddFood: (Food) -> Unit = {},
    onAmountChange: (Int, String) -> Unit = { _, _ -> },
    onRemoveItem: (Int) -> Unit = {},
    onSave: () -> Unit = {},
    onErrorShown: () -> Unit = {},
    today: LocalDate = LocalDate.now(),
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        onErrorShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "식사 수정" else "식사 기록") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) { Text(if (state.isEditing) "수정 완료" else "저장") }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val body = Modifier.padding(padding).fillMaxSize()
        if (state.isLoading) {
            Box(body, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            body.testTag(MEAL_INPUT_LIST_TAG),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { DateTimeSection(state, today, onDateChange, onTimeChange) }
            item { MealTypeSection(state.mealType, onMealTypeChange) }
            item { PhotoSection(state.photoUri, onPickPhoto, onRemovePhoto) }
            item { FoodSearchSection(state.query, state.searchResults, onQueryChange, onAddFood) }
            item {
                Text("담은 음식 ${state.items.size}개", style = MaterialTheme.typography.titleMedium)
                if (state.items.isEmpty()) {
                    Text(
                        "위에서 음식을 검색해 추가하세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            itemsIndexed(state.items) { index, item ->
                ItemRow(item, onAmountChange = { onAmountChange(index, it) }, onRemove = { onRemoveItem(index) })
            }
            item { NutritionSummaryCard("영양소 미리보기", state.preview) }
        }
    }
}

@Composable
private fun DateTimeSection(
    state: MealInputUiState,
    today: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val date = state.dateTime.toLocalDate()
    val time = state.dateTime.toLocalTime()

    Column {
        SectionTitle("날짜 · 시간")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showDate = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Text(date.toDisplay(), Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = { showTime = true }) {
                Icon(Icons.Outlined.Schedule, contentDescription = null)
                Text(time.toDisplay(), Modifier.padding(start = 8.dp))
            }
        }
    }
    if (showDate) {
        MealDatePickerDialog(date, today, onConfirm = onDateChange, onDismiss = { showDate = false })
    }
    if (showTime) {
        MealTimePickerDialog(time, onConfirm = onTimeChange, onDismiss = { showTime = false })
    }
}

/** 끼니 종류. 시각으로 자동 추천되고, 직접 고르면 그 값이 유지된다. */
@Composable
private fun MealTypeSection(selected: MealType, onChange: (MealType) -> Unit) {
    Column {
        SectionTitle("끼니")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { type ->
                FilterChip(
                    selected = type == selected,
                    onClick = { onChange(type) },
                    label = { Text(type.label) },
                )
            }
        }
    }
}

@Composable
private fun PhotoSection(photoUri: String?, onPick: () -> Unit, onRemove: () -> Unit) {
    Column {
        SectionTitle("사진")
        if (photoUri == null) {
            OutlinedButton(onClick = onPick) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                Text("갤러리에서 사진 선택", Modifier.padding(start = 8.dp))
            }
        } else {
            MealPhoto(photoUri, contentDescription = "선택한 음식 사진", modifier = Modifier.fillMaxWidth().height(200.dp))
            Row {
                TextButton(onClick = onPick) { Text("사진 변경") }
                TextButton(onClick = onRemove) { Text("사진 삭제") }
            }
        }
    }
}

@Composable
private fun FoodSearchSection(
    query: String,
    results: List<Food>,
    onQueryChange: (String) -> Unit,
    onAddFood: (Food) -> Unit,
) {
    Column {
        SectionTitle("음식 · 반찬 추가")
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예: 김치찌개, 밥") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
        )
        if (results.isNotEmpty()) {
            OutlinedCard(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                results.take(MAX_SEARCH_RESULTS).forEach { food ->
                    ListItem(
                        modifier = Modifier.clickable { onAddFood(food) },
                        headlineContent = { Text(food.name) },
                        supportingContent = { Text(food.category) },
                        trailingContent = {
                            Text("100${food.unit.symbol}당 ${formatKcal(food.per100.rounded().kcal)}")
                        },
                    )
                }
            }
        } else if (query.isNotBlank()) {
            Text(
                "검색 결과가 없어요.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** 담은 음식 한 줄: 이름, 섭취량 입력(단위 g/ml), 이 음식의 열량, 빼기 버튼. */
@Composable
private fun ItemRow(item: MealItemInput, onAmountChange: (String) -> Unit, onRemove: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.food.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                item.nutrition?.let { formatKcal(it.rounded().kcal) } ?: "섭취량을 확인하세요",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.isValid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
        }
        OutlinedTextField(
            value = item.amountText,
            onValueChange = onAmountChange,
            modifier = Modifier.width(120.dp),
            singleLine = true,
            isError = !item.isValid,
            suffix = { Text(item.food.unit.symbol) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            label = { Text("섭취량") },
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "${item.food.name} 빼기")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
}
