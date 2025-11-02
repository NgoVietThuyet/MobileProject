package com.example.test.ui.models

data class PhoneLoginRequest(
    val phoneNumber: String,
    val firebaseIdToken: String
)

data class PhoneLoginResponse(
    val success: Boolean,
    val message: String,
    val accessToken: String?,
    val refreshToken: String?,
    val user: UserDto?
)
