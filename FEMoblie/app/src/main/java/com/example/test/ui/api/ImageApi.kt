package com.example.test.ui.api

import com.example.test.ui.models.ImageUploadResp
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


interface ImageApi {
    @Multipart
    @POST("api/Image/imageUpload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Query("embedBase64") embedBase64: Boolean = false
    ): Response<ImageUploadResp>
}