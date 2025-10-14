package com.example.test.ui.api

import com.google.gson.annotations.SerializedName
data class LoginReq(val email: String, val password: String)

data class LoginResp(
    val token: String,
    val expires: String? = null
)

data class RegisterReq(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)

data class UserDto(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("facebook") val facebook: String? = null,
    @SerializedName("twitter") val twitter: String? = null,
    @SerializedName("createdDate") val createdDate: String? = null,
    @SerializedName("updatedDate") val updatedDate: String? = null
)

data class SignUpRequest(@SerializedName("userDto") val userDto: UserDto)

data class SignUpResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserDto? = null
)

data class ImageUploadResp(
    val success: Boolean,
    val message: String?,
    val transactions: List<ImageUploadTx>?
)

data class ImageUploadTx(
    val transactionId: String?,
    val userId: String?,
    val categoryId: String?,
    val type: String?,
    val amount: Long?,
    val note: String?,
    val createdDate: String?,
    val updatedDate: String?
)
