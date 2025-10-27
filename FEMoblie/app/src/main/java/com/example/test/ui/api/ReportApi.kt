package com.example.test.ui.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportApi {

    @Streaming
    @GET("api/Reports/export-template")
    suspend fun exportReport(
        @Query("userId") userId: String,
        @Query("StartDate") startDate: String,
        @Query("EndDate") endDate: String
    ): Response<ResponseBody>
}