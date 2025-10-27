package com.example.test.ui.api

import com.example.test.ui.models.BudgetCreateResponse
import com.example.test.ui.models.BudgetDto
import com.example.test.ui.models.CreateBudgetReq
import com.example.test.ui.models.UpdateBudgetAmountReq
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface BudgetApi {

    @GET("api/Budgets/GetAllBudgets")
    suspend fun getAllBudgets(
        @Query("userId") userId: String
    ): Response<List<BudgetDto>>

    @PUT("api/Budgets/Update")
    suspend fun updateBudget(
        @Body req: UpdateBudgetAmountReq
    ): Response<BudgetDto>

    @DELETE("api/Budgets/DeleteById")
    suspend fun deleteBudgetById(
        @Query("id") budgetId: String
    ): Response<Unit>

    @POST("api/Budgets/Create")
    suspend fun createBudget(
        @Body req: CreateBudgetReq
    ): Response<BudgetCreateResponse>
}
