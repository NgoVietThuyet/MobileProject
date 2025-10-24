package com.example.test.ui.api

import com.example.test.ui.models.CreateSavingGoalRequest
import com.example.test.ui.models.SavingGoalDto
import com.example.test.ui.models.UpdateSavingGoalAmountRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface SavingGoalApi {

    @GET("api/SavingGoal/GetAllSavingGoals")
    suspend fun getAllSavingGoals(
        @Query("userId") userId: String
    ): Response<List<SavingGoalDto>>

    @POST("api/SavingGoal/Create")
    suspend fun createSavingGoal(
        @Body request: CreateSavingGoalRequest
    ): Response<SavingGoalDto>

    @PUT("api/SavingGoal/UpdateAmount")
    suspend fun updateSavingGoalAmount(
        @Body request: UpdateSavingGoalAmountRequest
    ): Response<SavingGoalDto>
}
