package com.textworld.app.data.network

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.textworld.app.BuildConfig
import com.textworld.app.core.Constants
import com.textworld.app.core.Outcome
import com.textworld.app.data.model.UpdateInfo
import com.textworld.app.data.model.parsePayload
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class UpdateRepository(private val context: Context) {

    suspend fun check(): Outcome<UpdateInfo> = withContext(Dispatchers.IO) {
        val client = ApiClient.create()
        try {
            val response = client.prepareGet(BuildConfig.UPDATE_VERSION_URL).execute()
            val text = response.bodyAsText()
            Outcome.Success(parsePayload<UpdateInfo>(text))
        } catch (t: Throwable) {
            Outcome.Failure(t, "Не удалось проверить обновления")
        } finally {
            client.close()
        }
    }

    suspend fun download(
        info: UpdateInfo,
        onProgress: (downloaded: Long, total: Long) -> Unit,
    ): Outcome<File> = withContext(Dispatchers.IO) {
        val dir = File(context.externalCacheDir, "apk").apply { mkdirs() }
        val file = File(dir, Constants.UPDATE_FILE_NAME)
        val client = ApiClient.create()
        try {
            client.prepareGet(info.apkUrl).execute { response ->
                val total = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1L
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(64 * 1024)
                var downloaded = 0L
                FileOutputStream(file).use { out ->
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer)
                        if (read <= 0) break
                        out.write(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded, total)
                    }
                }
            }
            if (!verify(file, info.sha256)) {
                file.delete()
                Outcome.Failure(IllegalStateException("bad sha256"), "Обновление повреждено. Попробуй ещё раз.")
            } else {
                Outcome.Success(file)
            }
        } catch (t: Throwable) {
            file.delete()
            Outcome.Failure(t, "Не удалось скачать обновление")
        } finally {
            client.close()
        }
    }

    private fun verify(file: File, expected: String): Boolean {
        if (expected.isBlank()) return true
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStreamCompat(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.equals(expected.trim(), ignoreCase = true)
    }

    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    fun installIntent(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun settingsIntent(): Intent = Intent(
        android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        Uri.parse("package:${context.packageName}"),
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    private class FileInputStreamCompat(file: File) : java.io.FileInputStream(file)
}
