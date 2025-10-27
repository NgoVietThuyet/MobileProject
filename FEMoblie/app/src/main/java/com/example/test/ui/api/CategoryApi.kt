package com.example.test.ui.api

import com.example.test.ui.models.CategoryCreateResponse
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.CreateCategoryReq
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CategoryApi {

    @POST("api/Category/Create")
    suspend fun createCategory(
        @Body req: CreateCategoryReq
    ): Response<CategoryCreateResponse>

    @GET("api/Category/GetAll")
    suspend fun getAllCategories(): Response<List<CategoryDto>>
}
