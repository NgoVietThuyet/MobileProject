package com.example.test.data

import com.example.test.ui.api.UsersApi
import com.example.test.ui.models.UserDto
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepo @Inject constructor(
    private val api: UsersApi
) {
    suspend fun updateUser(userDto: UserDto): Response<Void> {
        return api.updateUser(userDto)
    }
}

