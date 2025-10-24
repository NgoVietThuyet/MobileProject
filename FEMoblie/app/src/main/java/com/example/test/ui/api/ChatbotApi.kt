package com.example.test.ui.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.test.ui.models.ChatbotRequest

interface ChatbotApi {
    @POST("api/Chatbot/extract")
    suspend fun extract(@Body body: ChatbotRequest): Response<ResponseBody>
}
