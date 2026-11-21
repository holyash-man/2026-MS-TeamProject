package kr.ac.dongguk.diet.ui.navigation

import kotlinx.serialization.Serializable

/*
 * 타입 세이프 내비게이션 라우트 (8주차 강의 방식, docs/decisions.md D2).
 * 프로퍼티 이름 mealId 는 ViewModel 이 SavedStateHandle["mealId"] 로 읽으므로 바꾸지 않는다.
 */

/** 식사 목록 (시작 화면). */
@Serializable
object ListRoute

/** 식사 입력. mealId 가 0이면 새로 작성, 0이 아니면 그 끼니를 수정한다. */
@Serializable
data class InputRoute(val mealId: Long = 0L)

/** 식사 상세. */
@Serializable
data class DetailRoute(val mealId: Long)

/** 식사 분석. */
@Serializable
object AnalysisRoute
