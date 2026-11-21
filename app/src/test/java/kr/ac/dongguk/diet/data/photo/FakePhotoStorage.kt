package kr.ac.dongguk.diet.data.photo

import java.io.IOException

/** [PhotoStorage] 메모리 구현. 복사·삭제 기록을 남기고, [failNextStore] 로 복사 실패를 흉내 낸다. */
class FakePhotoStorage : PhotoStorage {
    val stored = mutableListOf<String>()   // 지금 보관 중인 사진
    val deleted = mutableListOf<String>()
    var failNextStore = false
    private var next = 1

    override fun isStored(uri: String) = uri.startsWith(PREFIX)

    override suspend fun store(sourceUri: String): String {
        if (failNextStore) {
            failNextStore = false
            throw IOException("복사 실패(테스트)")
        }
        return "$PREFIX${next++}.jpg".also { stored += it }
    }

    override suspend fun delete(uri: String) {
        if (!isStored(uri)) return
        stored -= uri
        deleted += uri
    }

    companion object {
        const val PREFIX = "file:///fake/meal_photos/"
    }
}
