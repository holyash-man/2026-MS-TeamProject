package kr.ac.dongguk.diet.ui.input

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.ac.dongguk.diet.data.repository.FoodRepository
import kr.ac.dongguk.diet.data.repository.MealRepository
import kr.ac.dongguk.diet.domain.calc.MealTypeClassifier
import kr.ac.dongguk.diet.domain.calc.NutritionCalculator
import kr.ac.dongguk.diet.domain.model.Food
import kr.ac.dongguk.diet.domain.model.Meal
import kr.ac.dongguk.diet.domain.model.MealItem
import kr.ac.dongguk.diet.domain.model.MealType
import kr.ac.dongguk.diet.domain.model.Nutrition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 입력 중인 음식 한 줄.
 * @property amountText 섭취량 입력칸의 글자 그대로. 숫자가 아니거나 0 이하면 [isValid] = false.
 */
data class MealItemInput(
    val food: Food,
    val amountText: String,
) {
    val amount: Double? = amountText.trim().toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
    val isValid: Boolean get() = amount != null

    /** 이 음식의 영양성분 미리보기. 섭취량이 잘못되었으면 null. */
    val nutrition: Nutrition? = amount?.let { NutritionCalculator.forFood(food, it) }
}

data class MealInputUiState(
    val mealId: Long = 0,
    val isLoading: Boolean = false,
    val dateTime: LocalDateTime,
    val mealType: MealType,
    /** 사용자가 끼니 종류를 직접 골랐으면 true. 그 뒤로는 시간을 바꿔도 자동 추천하지 않는다. */
    val mealTypeChosen: Boolean = false,
    val photoUri: String? = null,
    val items: List<MealItemInput> = emptyList(),
    val query: String = "",
    val searchResults: List<Food> = emptyList(),
    val isSaving: Boolean = false,
    /** 저장이 끝나면 저장된 id. 화면은 이 값을 보고 이동한 뒤 [MealInputViewModel.onSavedHandled] 를 부른다. */
    val savedMealId: Long? = null,
    val errorMessage: String? = null,
) {
    val isEditing: Boolean get() = mealId != 0L

    /** 입력 화면 영양소 미리보기: 섭취량이 올바른 음식들의 합계. */
    val preview: Nutrition
        get() = items.mapNotNull { it.nutrition }.fold(Nutrition.ZERO) { sum, n -> sum + n }

    val canSave: Boolean
        get() = items.isNotEmpty() && items.all { it.isValid } && !isSaving && !isLoading
}

/**
 * 식사 입력(새로 작성 / 수정) 화면 ViewModel.
 * 수정할 때는 내비게이션 인자 [MEAL_ID_ARG] 에 끼니 id 를 넘긴다(없거나 0이면 새로 작성).
 */
class MealInputViewModel(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val mealRepository: MealRepository,
    now: () -> LocalDateTime = LocalDateTime::now,
) : ViewModel() {

    private val _uiState: MutableStateFlow<MealInputUiState>
    val uiState: StateFlow<MealInputUiState>

    private var searchJob: Job? = null

    init {
        val start = now().withSecond(0).withNano(0)
        _uiState = MutableStateFlow(
            MealInputUiState(dateTime = start, mealType = MealTypeClassifier.suggest(start.toLocalTime())),
        )
        uiState = _uiState.asStateFlow()

        val mealId = savedStateHandle.get<Long>(MEAL_ID_ARG) ?: 0L
        if (mealId != 0L) loadForEdit(mealId)
    }

    private fun loadForEdit(id: Long) {
        _uiState.update { it.copy(mealId = id, isLoading = true) }
        viewModelScope.launch {
            val meal = mealRepository.getMeal(id)
            _uiState.update { state ->
                if (meal == null) {
                    state.copy(isLoading = false, errorMessage = "식사 기록을 찾을 수 없어요")
                } else {
                    state.copy(
                        isLoading = false,
                        dateTime = meal.dateTime,
                        mealType = meal.type,
                        mealTypeChosen = true,
                        photoUri = meal.photoUri,
                        items = meal.items.map { MealItemInput(it.food, formatAmount(it.amount)) },
                    )
                }
            }
        }
    }

    fun onDateChange(date: LocalDate) = _uiState.update {
        it.copy(dateTime = LocalDateTime.of(date, it.dateTime.toLocalTime()))
    }

    fun onTimeChange(time: LocalTime) = _uiState.update {
        it.copy(
            dateTime = LocalDateTime.of(it.dateTime.toLocalDate(), time.withSecond(0).withNano(0)),
            mealType = if (it.mealTypeChosen) it.mealType else MealTypeClassifier.suggest(time),
        )
    }

    fun onMealTypeChange(type: MealType) = _uiState.update { it.copy(mealType = type, mealTypeChosen = true) }

    fun onPhotoChange(uri: String?) = _uiState.update { it.copy(photoUri = uri) }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            val results = foodRepository.search(query)
            _uiState.update { if (it.query == query) it.copy(searchResults = results) else it }
        }
    }

    /** 검색 결과에서 음식을 고르면 1인분 참고량(없으면 100)으로 추가하고 검색어를 비운다. */
    fun addFood(food: Food) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                items = it.items + MealItemInput(food, formatAmount(NutritionCalculator.defaultAmount(food))),
                query = "",
                searchResults = emptyList(),
            )
        }
    }

    fun onAmountChange(index: Int, text: String) = _uiState.update { state ->
        if (index !in state.items.indices) return@update state
        state.copy(items = state.items.toMutableList().also { it[index] = it[index].copy(amountText = text) })
    }

    fun removeItem(index: Int) = _uiState.update { state ->
        if (index !in state.items.indices) state
        else state.copy(items = state.items.toMutableList().also { it.removeAt(index) })
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val meal = Meal(
                id = state.mealId,
                dateTime = state.dateTime,
                type = state.mealType,
                photoUri = state.photoUri,
                items = state.items.map { MealItem(it.food, it.amount!!) },
            )
            try {
                val id = mealRepository.saveMeal(meal)
                _uiState.update { it.copy(isSaving = false, savedMealId = id) }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "저장할 수 없어요") }
            } catch (e: IllegalStateException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "저장할 수 없어요") }
            }
        }
    }

    fun onSavedHandled() = _uiState.update { it.copy(savedMealId = null) }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        const val MEAL_ID_ARG = "mealId"

        /** 210.0 → "210", 136.4 → "136.4" */
        fun formatAmount(amount: Double): String =
            if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()
    }
}
