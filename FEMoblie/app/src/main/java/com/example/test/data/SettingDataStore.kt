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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

val Context.settingsDataStore by preferencesDataStore("settings")
val Context.authDataStore by preferencesDataStore("auth")

private object Keys {
    val DARK = booleanPreferencesKey("dark_mode")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
}

private object AuthKeys {
    val USER_ID = androidx.datastore.preferences.core.stringPreferencesKey("user_id")
    val USER_NAME = androidx.datastore.preferences.core.stringPreferencesKey("user_name")
    val USER_EMAIL = androidx.datastore.preferences.core.stringPreferencesKey("user_email")
    val USER_PHONE = androidx.datastore.preferences.core.stringPreferencesKey("user_phone")
    val USER_CREATION_DATE = androidx.datastore.preferences.core.stringPreferencesKey("user_creation_date")
    val TOKEN = androidx.datastore.preferences.core.stringPreferencesKey("token")
    val REFRESH_TOKEN = androidx.datastore.preferences.core.stringPreferencesKey("refresh_token")
}

class SettingsRepository(private val context: Context) {
    val darkFlow = context.settingsDataStore.data.map { it[Keys.DARK] ?: false }
    val soundEnabledFlow = context.settingsDataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
    suspend fun setDark(v: Boolean) = context.settingsDataStore.edit { it[Keys.DARK] = v }
    suspend fun setSoundEnabled(v: Boolean) = context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = v }
}

class AuthRepository(private val context: Context) {
    val userIdFlow = context.authDataStore.data.map { it[AuthKeys.USER_ID] }
    val userNameFlow = context.authDataStore.data.map { it[AuthKeys.USER_NAME] }
    val userEmailFlow = context.authDataStore.data.map { it[AuthKeys.USER_EMAIL] }
    val userPhoneFlow = context.authDataStore.data.map { it[AuthKeys.USER_PHONE] }
    val userCreationDateFlow = context.authDataStore.data.map { it[AuthKeys.USER_CREATION_DATE] }
    val tokenFlow = context.authDataStore.data.map { it[AuthKeys.TOKEN] }
    val refreshTokenFlow = context.authDataStore.data.map { it[AuthKeys.REFRESH_TOKEN] }

    suspend fun saveUserInfo(
        userId: String?,
        userName: String,
        userEmail: String,
        userPhone: String?,
        userCreationDate: String?,
        token: String? = null,
        refreshToken: String? = null
    ) {
        context.authDataStore.edit { prefs ->
            userId?.let { prefs[AuthKeys.USER_ID] = it }
            prefs[AuthKeys.USER_NAME] = userName
            prefs[AuthKeys.USER_EMAIL] = userEmail
            userPhone?.let { prefs[AuthKeys.USER_PHONE] = it }
            userCreationDate?.let { prefs[AuthKeys.USER_CREATION_DATE] = it }
            token?.let { prefs[AuthKeys.TOKEN] = it }
            refreshToken?.let { prefs[AuthKeys.REFRESH_TOKEN] = it }
        }
    }

    suspend fun updateTokens(token: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[AuthKeys.TOKEN] = token
            prefs[AuthKeys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearUserInfo() {
        context.authDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun getUserId(): String? {
        val prefs = context.authDataStore.data.map { it[AuthKeys.USER_ID] }
        return prefs.map { it }.first()
    }

    suspend fun getRefreshToken(): String? {
        return context.authDataStore.data.map { it[AuthKeys.REFRESH_TOKEN] }.first()
    }
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
