package com.example.test.ui.api

import com.example.test.ui.models.LoginReq
import com.example.test.ui.models.LoginResp
import com.example.test.ui.models.PhoneLoginRequest
import com.example.test.ui.models.PhoneLoginResponse
import com.example.test.ui.models.RefreshTokenReq
import com.example.test.ui.models.RefreshTokenResp
import com.example.test.ui.models.SignUpRequest
import com.example.test.ui.models.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsersApi {
    @GET("api/Users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserDto>

    @POST("api/Users/login")
    suspend fun login(@Body body: LoginReq): Response<LoginResp>

    @POST("api/Users/phone-login")
    suspend fun phoneLogin(@Body body: PhoneLoginRequest): Response<PhoneLoginResponse>

    @POST("api/Users/Create")
    suspend fun register(@Body body: SignUpRequest): Response<Void>

    @PUT("api/Users/Update")
    suspend fun updateUser(@Body userDto: UserDto): Response<Void>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenReq): Response<RefreshTokenResp>

    // Change Password
    @POST("api/Users/ChangePassword")
    suspend fun changePassword(@Body body: com.example.test.ui.models.ChangePasswordRequest): Response<com.example.test.ui.models.ChangePasswordResponse>
}

