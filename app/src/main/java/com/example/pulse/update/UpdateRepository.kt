package com.example.pulse.update

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class UpdateRepository(context: Context) {

    private val appContext = context.applicationContext

    fun currentVersionCode(): Long =
        appContext.packageManager
            .getPackageInfo(appContext.packageName, 0)
            .longVersionCode

    suspend fun fetchLatest(): UpdateInfo = withContext(Dispatchers.IO) {
        val connection = open(VERSION_URL)
        try {
            check(connection.responseCode == 200) { "HTTP ${connection.responseCode}" }
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            UpdateInfo(
                versionCode = json.getLong("versionCode"),
                versionName = json.getString("versionName"),
                apkUrl = json.getString("apkUrl"),
                sha256 = json.optString("sha256")
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun download(info: UpdateInfo): File = withContext(Dispatchers.IO) {
        val dir = File(appContext.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }   // убираем старые загрузки

        val part = File(dir, "pulse-update.part")
        val target = File(dir, "pulse-update.apk")
        val digest = MessageDigest.getInstance("SHA-256")

        val connection = open(info.apkUrl)
        try {
            check(connection.responseCode == 200) { "HTTP ${connection.responseCode}" }
            connection.inputStream.use { input ->
                part.outputStream().use { output ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        ensureActive()   // если корутину отменили, выходим
                    }
                }
            }
        } catch (e: Exception) {
            part.delete()
            throw e
        } finally {
            connection.disconnect()
        }

        check(part.renameTo(target)) { "Не удалось сохранить файл" }

        if (!isTrusted(target, info.sha256, digest.digest().toHex())) {
            target.delete()
            error("Проверка файла не пройдена")
        }
        target
    }

    private fun isTrusted(file: File, expected: String, fileHash: String): Boolean {
        val wanted = normalize(expected)
        if (wanted.isEmpty()) return false

        val archive = appContext.packageManager.getPackageArchiveInfo(
            file.path,
            PackageManager.GET_SIGNING_CERTIFICATES
        ) ?: return false
        if (archive.packageName != appContext.packageName) return false

        val signer = archive.signingInfo?.apkContentsSigners?.firstOrNull()
        val certHash = signer?.let {
            MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).toHex()
        }
        return wanted == fileHash || wanted == certHash
    }

    private fun normalize(value: String) =
        value.filter { it != ':' && !it.isWhitespace() }.uppercase()

    private fun ByteArray.toHex() = joinToString("") { "%02X".format(it) }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 30_000
            instanceFollowRedirects = true
        }

    private companion object {
        const val VERSION_URL =
            "https://raw.githubusercontent.com/FindTheBugg/Pulse/main/version.json"
    }
}