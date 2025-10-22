package com.example.test.ui.models

import com.google.gson.annotations.SerializedName
data class GetAccountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("accountId") val accountId: String?,
    @SerializedName("balance") val balance: String?
)

data class AccountDto(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("balance") val balance: String
)
