package com.example.test.data

import com.example.test.ui.api.AccountApi
import com.example.test.ui.api.AuthStore
import com.example.test.ui.models.GetAccountResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepo @Inject constructor(
    private val api: AccountApi
) {
    suspend fun getAccount(): Response<GetAccountResponse> {
        val userId = AuthStore.userId ?: throw IllegalStateException("User ID is null. User must be logged in.")
        return api.getAccountByUserId(userId)
    }
}

