package kr.ac.dongguk.diet.data.photo

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

/** 실제 기기 파일 시스템에서 사진 복사·삭제 확인. */
@RunWith(AndroidJUnit4::class)
class InternalPhotoStorageTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val storage = InternalPhotoStorage(context)

    private fun sourceFile(bytes: ByteArray): File =
        File(context.cacheDir, "picked-${System.nanoTime()}.jpg").apply { writeBytes(bytes) }

    @Test
    fun store_copiesBytesIntoAppStorage(): Unit = runBlocking {
        val bytes = ByteArray(2048) { it.toByte() }
        val source = sourceFile(bytes)

        val stored = storage.store(Uri.fromFile(source).toString())

        assertTrue(storage.isStored(stored))
        val copy = File(Uri.parse(stored).path!!)
        assertTrue(copy.parentFile!!.path.startsWith(context.filesDir.path))
        assertArrayEquals(bytes, copy.readBytes())

        source.delete() // 원본(갤러리 쪽)이 사라져도 복사본은 남는다
        assertTrue(copy.exists())
        storage.delete(stored)
    }

    @Test
    fun delete_removesStoredFile_andIgnoresOutsideUris(): Unit = runBlocking {
        val outside = sourceFile(byteArrayOf(1, 2, 3))
        val stored = storage.store(Uri.fromFile(outside).toString())

        storage.delete(stored)
        storage.delete(Uri.fromFile(outside).toString()) // 보관소 밖 → 지우지 않음

        assertFalse(File(Uri.parse(stored).path!!).exists())
        assertTrue(outside.exists())
        outside.delete()
    }

    @Test(expected = IOException::class)
    fun store_missingSource_throws(): Unit = runBlocking {
        storage.store(Uri.fromFile(File(context.cacheDir, "no-such-file.jpg")).toString())
    }
}
