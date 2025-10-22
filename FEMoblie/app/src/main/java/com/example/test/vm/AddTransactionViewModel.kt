package com.example.test.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.TransactionRepo
import com.example.test.ui.api.AuthStore
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.CreateTransactionRequest
import com.example.test.ui.models.TransactionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject


data class AddTransactionUiState(
    val isLoading: Boolean = true,
    val incomeCategories: List<CategoryDto> = emptyList(),
    val expenseCategories: List<CategoryDto> = emptyList(),
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val errorMessage: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    val repository: TransactionRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val apiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val allCategories = repository.getLocalCategories()
        val incomeCategoryNames = setOf("Lương", "Thưởng", "Đầu tư", "Việc tự do", "Bán hàng", "Thu nhập khác")

        _uiState.update {
            it.copy(
                isLoading = false,
                incomeCategories = allCategories.filter { cat -> cat.name in incomeCategoryNames },
                expenseCategories = allCategories.filterNot { cat -> cat.name in incomeCategoryNames }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransaction(
        amount: String,
        categoryId: String,
        note: String,
        dateMillis: Long,
        type: String
    ) {
        viewModelScope.launch {
            val uid = AuthStore.userId
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = "User not logged in") }
                return@launch
            }

            _uiState.update { it.copy(saveStatus = SaveStatus.LOADING) }

            val createdDateString = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(apiDateFormatter)

            val transactionDto = TransactionDto(
                userId = uid,
                categoryId = categoryId,
                type = type,
                amount = amount,
                note = note,
                createdDate = createdDateString,

                transactionId = UUID.randomUUID().toString(),
                updatedDate = null
            )

            val request = CreateTransactionRequest(transaction = transactionDto)

            try {
                val response = repository.createTransaction(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update { it.copy(saveStatus = SaveStatus.SUCCESS) }
                } else {
                    val msg = response.body()?.message ?: response.errorBody()?.string() ?: "Lỗi không xác định"
                    _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = msg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = e.message ?: "Lỗi kết nối") }
            }
        }
    }

    fun resetStatus() {
        _uiState.update { it.copy(saveStatus = SaveStatus.IDLE, errorMessage = null) }
    }
}

