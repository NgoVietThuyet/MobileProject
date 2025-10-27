package com.example.test.ui.models

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("note") val note: String?,
    @SerializedName("createdDate") val createdDate: String?,
    @SerializedName("updatedDate") val updatedDate: String?
)

data class GetAllTransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("transactions") val transactions: List<TransactionDto>
)

data class CreateTransactionRequest(
    @SerializedName("transaction") val transaction: TransactionDto
)

data class CreateTransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("transaction") val transaction: TransactionDto?
)

data class UpdateTransactionRequest(
    @SerializedName("transaction") val transaction: TransactionDto
)

data class UpdateTransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("updatedTransaction") val updatedTransaction: TransactionDto?
)

data class DeleteTransactionRequest(
    @SerializedName("transactionId") val transactionId: String
)

data class DeleteTransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)