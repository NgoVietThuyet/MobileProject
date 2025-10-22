package com.example.test.ui.api

import com.example.test.ui.models.GetAccountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AccountApi {
    @GET("api/Accounts/GetAccountByUserId")
    suspend fun getAccountByUserId(
        @Query("id") userId: String
    ): Response<GetAccountResponse>
}
