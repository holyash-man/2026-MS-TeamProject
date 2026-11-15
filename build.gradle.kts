// Top-level build file. AGP 9 는 Kotlin 지원이 내장되어 kotlin-android 플러그인이 필요 없음.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
