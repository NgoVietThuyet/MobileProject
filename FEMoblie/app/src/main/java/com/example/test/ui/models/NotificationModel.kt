package com.example.test.ui.models

import com.google.gson.annotations.SerializedName

data class GetAllNotificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("notifications") val notifications: List<NotificationDto>
)
data class NotificationDto(
    @SerializedName("notificationId") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("content") val content: String,
    @SerializedName("createdDate") val createdAt: String,
    @SerializedName("updatedDate") val updatedDate: String?
)
