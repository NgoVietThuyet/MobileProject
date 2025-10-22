package com.example.test.data

import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.NotificationApi
import com.example.test.ui.models.GetAllNotificationResponse

import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepo @Inject constructor(
    private val api: NotificationApi
) {

    suspend fun getAllNotifications(): Response<GetAllNotificationResponse> {
        val userId = AuthStore.userId ?: throw IllegalStateException("User ID is null.")
        return api.getAllNotifications(userId)
    }

    suspend fun markAsRead(notificationId: String): Response<Unit> {
        return api.markAsRead(notificationId)
    }

    suspend fun deleteNotification(notificationId: String): Response<Unit> {
        return api.deleteNotification(notificationId)
    }
}
