package kr.ac.dongguk.diet

import android.app.Application

/** 앱 전역 객체. 의존성은 [AppContainer] 에 모은다. */
class DietApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
