package com.example.test.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.test.ui.api.ImageApi
import com.example.test.ui.scan.UploadResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

val Context.settingsDataStore by preferencesDataStore("settings")
private object Keys { val DARK = booleanPreferencesKey("dark_mode") }

class SettingsRepository(private val context: Context) {
    val darkFlow = context.settingsDataStore.data.map { it[Keys.DARK] ?: false }
    suspend fun setDark(v: Boolean) = context.settingsDataStore.edit { it[Keys.DARK] = v }
}

class ImageRepo @Inject constructor(
    private val api: ImageApi,
    @ApplicationContext private val context: Context
) {
    suspend fun upload(uri: Uri): UploadResult = withContext(Dispatchers.IO) {
        try {
            val part = buildStreamingImagePartFromUri(
                context = context,
                uri = uri,
                fieldName = "file"
            )

            val response = api.uploadImage(part)

            if (response.isSuccessful) {
                response.body() ?: UploadResult(success = false, url = null, message = "Body rỗng")
            } else {
                val msg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                UploadResult(success = false, url = null, message = msg)
            }
        } catch (e: Exception) {
            UploadResult(success = false, url = null, message = e.message ?: "Lỗi không xác định")
        } as UploadResult
    }
}

fun buildStreamingImagePartFromUri(
    context: Context,
    uri: Uri,
    fieldName: String,
    fileName: String? = null
): MultipartBody.Part {
    val cr = context.contentResolver
    val mimeStr = cr.getType(uri) ?: "image/jpeg"
    val mediaType: MediaType? = mimeStr.toMediaTypeOrNull()
    val finalName = fileName ?: queryDisplayName(context, uri) ?: "receipt.jpg"

    val body = object : RequestBody() {
        override fun contentType(): MediaType? = mediaType
        override fun contentLength(): Long =
            try { cr.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L }
            catch (_: Exception) { -1L }

        override fun writeTo(sink: BufferedSink) {
            val input = cr.openInputStream(uri) ?: error("Cannot open input stream")
            input.source().use { source -> sink.writeAll(source) }
        }
    }
    return MultipartBody.Part.createFormData(fieldName, finalName, body)
}

private fun queryDisplayName(context: Context, uri: Uri): String? =
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
    }
