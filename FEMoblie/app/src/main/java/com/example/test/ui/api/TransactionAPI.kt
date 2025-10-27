package com.example.test.ui.api

import com.example.test.ui.models.CreateTransactionRequest
import com.example.test.ui.models.CreateTransactionResponse
import com.example.test.ui.models.DeleteTransactionRequest
import com.example.test.ui.models.DeleteTransactionResponse
import com.example.test.ui.models.GetAllTransactionResponse
import com.example.test.ui.models.UpdateTransactionRequest
import com.example.test.ui.models.UpdateTransactionResponse
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @GET("api/Transactions/GetAll")
    suspend fun getAllTransactions(
        @Query("userId") userId: String
    ): Response<GetAllTransactionResponse>


    @POST("api/Transactions/Create")
    suspend fun createTransaction(
        @Body request: CreateTransactionRequest
    ): Response<CreateTransactionResponse>

    @PUT("api/Transactions/Update")
    suspend fun updateTransaction(
        @Body request: UpdateTransactionRequest
    ): Response<UpdateTransactionResponse>

    @HTTP(method = "DELETE", path = "api/Transactions/Delete", hasBody = true)
    suspend fun deleteTransaction(
        @Body request: DeleteTransactionRequest
    ): Response<DeleteTransactionResponse>
}

