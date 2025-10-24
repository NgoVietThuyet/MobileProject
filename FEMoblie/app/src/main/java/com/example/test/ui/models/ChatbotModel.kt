package com.example.test.ui.models

import com.google.gson.annotations.SerializedName

data class ChatbotRequest(
    @SerializedName("text") val text: String
)

data class ChatbotResponse(
    @SerializedName("text") val text: String?
)