package com.example.test.ui.api


import com.example.test.ui.models.SavingGoalCreatePayload
import com.example.test.ui.models.SavingGoalCreateResponse
import com.example.test.ui.models.SavingGoalDeleteReq
import com.example.test.ui.models.SavingGoalDto
import com.example.test.ui.models.SavingGoalUpdateAmountReq
import com.example.test.ui.models.SimpleSuccessResponse
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
        @Body req: SavingGoalCreatePayload
    ): Response<SavingGoalCreateResponse>

    @PUT("api/SavingGoal/UpdateAmount")
    suspend fun updateSavingGoalAmount(
        @Body req: SavingGoalUpdateAmountReq
    ): Response<SavingGoalDto>

    @PUT("api/SavingGoal/DeleteSaving")
    suspend fun deleteSavingGoal(
        @Body req: SavingGoalDeleteReq
    ): Response<SimpleSuccessResponse>
}