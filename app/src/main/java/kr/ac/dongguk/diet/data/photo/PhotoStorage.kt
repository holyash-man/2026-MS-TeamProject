package kr.ac.dongguk.diet.data.photo

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * 식사 사진 보관소.
 *
 * Photo Picker 가 주는 content:// URI 는 앱이 다시 시작되면 읽을 권한이 사라진다.
 * 그래서 저장할 때 앱 내부 저장소로 복사하고, 그 파일 주소(file://...)를 DB 에 넣는다.
 */
interface PhotoStorage {
    /** 이미 이 보관소에 복사된 사진인지. */
    fun isStored(uri: String): Boolean

    /** [sourceUri] 사진을 보관소로 복사하고 새 주소를 돌려준다. @throws IOException 읽거나 쓸 수 없을 때 */
    suspend fun store(sourceUri: String): String

    /** 보관소의 사진 파일을 지운다. 보관소 밖의 주소면 아무것도 하지 않는다. */
    suspend fun delete(uri: String)
}

/** 앱 내부 저장소(filesDir/meal_photos)에 사진을 보관한다. 앱을 지우면 함께 지워진다. */
class InternalPhotoStorage(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PhotoStorage {
    private val appContext = context.applicationContext
    private val dir = File(appContext.filesDir, DIR_NAME)
    private val dirUri = Uri.fromFile(dir).toString() + "/"

    override fun isStored(uri: String): Boolean = uri.startsWith(dirUri)

    override suspend fun store(sourceUri: String): String = withContext(ioDispatcher) {
        if (!dir.exists() && !dir.mkdirs()) throw IOException("사진 폴더를 만들 수 없음: $dir")
        val target = File(dir, "${UUID.randomUUID()}.jpg")
        try {
            val input = appContext.contentResolver.openInputStream(Uri.parse(sourceUri))
                ?: throw IOException("사진을 열 수 없음: $sourceUri")
            input.use { src -> target.outputStream().use { dst -> src.copyTo(dst) } }
        } catch (e: IOException) {
            target.delete()
            throw e
        } catch (e: SecurityException) {
            target.delete()
            throw IOException("사진을 읽을 권한이 없음: $sourceUri", e)
        }
        Uri.fromFile(target).toString()
    }

    override suspend fun delete(uri: String) {
        if (!isStored(uri)) return
        withContext(ioDispatcher) { Uri.parse(uri).path?.let { File(it).delete() } }
    }

    companion object {
        const val DIR_NAME = "meal_photos"
    }
}
