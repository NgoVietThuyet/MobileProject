package com.example.test.data

import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.TransactionApi
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.CreateTransactionRequest
import com.example.test.ui.models.CreateTransactionResponse
import com.example.test.ui.models.DeleteTransactionRequest
import com.example.test.ui.models.DeleteTransactionResponse
import com.example.test.ui.models.GetAllTransactionResponse
import com.example.test.ui.models.UpdateTransactionRequest
import com.example.test.ui.models.UpdateTransactionResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepo @Inject constructor(
    private val api: TransactionApi
) {

    suspend fun getAllFromAuth(): Response<GetAllTransactionResponse> {
        val id = AuthStore.userId ?: throw IllegalStateException("User ID is null. User must be logged in.")
        return api.getAllTransactions(id)
    }

    fun getLocalCategories(): List<CategoryDto> {
        return LocalCategoryDataSource.allCategories
    }

    suspend fun createTransaction(request: CreateTransactionRequest): Response<CreateTransactionResponse> {
        return api.createTransaction(request)
    }

    suspend fun updateTransaction(request: UpdateTransactionRequest): Response<UpdateTransactionResponse> {
        return api.updateTransaction(request)
    }
    suspend fun deleteTransaction(request: DeleteTransactionRequest): Response<DeleteTransactionResponse> {
        return api.deleteTransaction(request)
    }
}

