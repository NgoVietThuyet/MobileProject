package com.example.test.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.TransactionRepo
import com.example.test.ui.api.AuthStore
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.DeleteTransactionRequest
import com.example.test.ui.models.TransactionDto
import com.example.test.ui.models.UpdateTransactionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


data class EditTransactionUiState(
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val deleteStatus: SaveStatus = SaveStatus.IDLE,
    val errorMessage: String? = null,
    val incomeCategories: List<CategoryDto> = emptyList(),
    val expenseCategories: List<CategoryDto> = emptyList()
)

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    init {
        val allCategories = repository.getLocalCategories()
        _uiState.update {
            it.copy(
                incomeCategories = allCategories.filter { cat ->
                    listOf("Lương", "Thưởng", "Đầu tư", "Việc tự do", "Bán hàng", "Thu nhập khác").contains(cat.name)
                },
                expenseCategories = allCategories.filterNot { cat ->
                    listOf("Lương", "Thưởng", "Đầu tư", "Việc tự do", "Bán hàng", "Thu nhập khác").contains(cat.name)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransaction(
        transactionId: String,
        amount: String,
        categoryId: String,
        note: String,
        date: LocalDate,
        originalDateTime: LocalDateTime,
        type: String
    ) {
        viewModelScope.launch {
            val uid = AuthStore.userId
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = "User not logged in") }
                return@launch
            }

            _uiState.update { it.copy(saveStatus = SaveStatus.LOADING) }

            val newDateTime = date.atTime(originalDateTime.toLocalTime())
            val dateString = newDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))

            val transactionDto = TransactionDto(
                transactionId = transactionId,
                userId = uid,
                categoryId = categoryId,
                type = type,
                amount = amount,
                note = note,
                createdDate = dateString,
                updatedDate = dateString
            )

            val request = UpdateTransactionRequest(transaction = transactionDto)

            try {
                val response = repository.updateTransaction(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update { it.copy(saveStatus = SaveStatus.SUCCESS) }
                } else {
                    val msg = response.body()?.message ?: response.errorBody()?.string() ?: response.message()
                    _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = msg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            val uid = AuthStore.userId
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(deleteStatus = SaveStatus.ERROR, errorMessage = "User not logged in") }
                return@launch
            }
            _uiState.update { it.copy(deleteStatus = SaveStatus.LOADING) }
            try {
                val request = DeleteTransactionRequest(transactionId = txId)
                val response = repository.deleteTransaction(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update { it.copy(deleteStatus = SaveStatus.SUCCESS) }
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: response.message()
                    _uiState.update { it.copy(deleteStatus = SaveStatus.ERROR, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(deleteStatus = SaveStatus.ERROR, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    fun resetSaveStatus() {
        _uiState.update { it.copy(saveStatus = SaveStatus.IDLE, errorMessage = null) }
    }

    fun resetDeleteStatus() {
        _uiState.update { it.copy(deleteStatus = SaveStatus.IDLE, errorMessage = null) }
    }
}
