package com.example.test.ui.api

import com.example.test.ui.models.GetAllNotificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface NotificationApi {

    @GET("api/Notifications/GetAll")
    suspend fun getAllNotifications(
        @Query("userId") userId: String
    ): Response<GetAllNotificationResponse>

    @PUT("api/Notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): Response<Unit>

    @DELETE("api/Notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    ): Response<Unit>

}