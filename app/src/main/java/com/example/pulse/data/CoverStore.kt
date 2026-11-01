package com.example.pulse.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.max

class CoverStore(context: Context) {

    private val appContext = context.applicationContext
    private val dir = File(appContext.cacheDir, "covers").apply { mkdirs() }

    fun cached(trackId: String): Bitmap? = memory.get(trackId)

    suspend fun loadBitmap(track: Track): Bitmap? = withContext(Dispatchers.IO) {
        memory.get(track.id)?.let { return@withContext it }
        val file = coverFile(track.id, track.uri) ?: return@withContext null
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return@withContext null
        memory.put(track.id, bitmap)
        bitmap
    }

    suspend fun coverFile(trackId: String, uri: Uri): File? = withContext(Dispatchers.IO) {
        val name = nameFor(trackId)
        val file = File(dir, "$name.jpg")
        val noCover = File(dir, "$name.none")
        if (file.exists()) return@withContext file
        if (noCover.exists()) return@withContext null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(appContext, uri)
            val bytes = retriever.embeddedPicture
            if (bytes == null) {
                noCover.createNewFile()
                return@withContext null
            }
            val bitmap = decodeScaled(bytes) ?: return@withContext null

            val tmp = File(dir, "$name.tmp")
            FileOutputStream(tmp).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            tmp.renameTo(file)
            memory.put(trackId, bitmap)
            file
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    fun clear() {
        memory.evictAll()
        dir.listFiles()?.forEach { it.delete() }
    }

    private fun decodeScaled(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        val longest = max(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (longest / (sample * 2) >= MAX_SIDE) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun nameFor(trackId: String): String =
        MessageDigest.getInstance("SHA-1")
            .digest(trackId.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val MAX_SIDE = 800

        val memory = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
            override fun sizeOf(key: String, value: Bitmap) = value.byteCount
        }
    }
}