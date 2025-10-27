package com.example.test.ui.api

import com.example.test.ui.models.LoginReq
import com.example.test.ui.models.LoginResp
import com.example.test.ui.models.SignUpRequest
import com.example.test.ui.models.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface UsersApi {

    @POST("api/Users/login")
    suspend fun login(@Body body: LoginReq): Response<LoginResp>
    @POST("api/Users/Create")
    suspend fun register(@Body body: SignUpRequest): Response<Void>
    @PUT("api/Users/Update")
    suspend fun updateUser(@Body userDto: UserDto): Response<Void>
}

