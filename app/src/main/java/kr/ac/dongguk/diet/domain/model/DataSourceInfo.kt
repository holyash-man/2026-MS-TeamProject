package kr.ac.dongguk.diet.domain.model

/** 영양 데이터 출처. 과제 요구사항에 따라 앱 화면에 표시한다. */
data class DataSourceInfo(
    val provider: String,
    val dataset: String,
    val url: String,
    val referenceDate: String,
    val note: String,
)
