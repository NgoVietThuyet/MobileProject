package com.example.test.data

import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.ChatbotApi
import com.example.test.ui.models.ChatbotRequest
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatbotRepo @Inject constructor(
    private val api: ChatbotApi,
    private val gson: Gson = Gson()
) {
    suspend fun extract(text: String): Result<String> {
        return try {
            val res: Response<ResponseBody> = api.extract(ChatbotRequest(text, userid = AuthStore.userId))
            if (!res.isSuccessful) return Result.failure(Exception("HTTP ${res.code()}"))
            val raw = res.body()?.string().orEmpty()

            val parsed = runCatching {
                val obj = JSONObject(raw)
                obj.optString("text", raw)
            }.getOrElse { raw }

            Result.success(parsed.trim().ifEmpty { "Không có nội dung trả lời." })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
