package com.example.test.vm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.TransactionRepo
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.TransactionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TransactionUiState(
    val isLoading: Boolean = true,
    val transactions: List<TxUi> = emptyList(),
    val error: String? = null
)

private val apiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

@HiltViewModel
class AllTransactionViewModel @Inject constructor(
    private val repository: TransactionRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transactionResponse = repository.getAllFromAuth()
                val categories = repository.getLocalCategories()

                if (transactionResponse.isSuccessful) {
                    val transactionBody = transactionResponse.body()
                    if (transactionBody != null) {
                        val dtos = transactionBody.transactions ?: emptyList()
                        val categoryMap = categories.associateBy { it.categoryId }
                        val uiModels = dtos.map { it.toTxUi(categoryMap) }
                            .sortedByDescending { it.dateTime }

                        _uiState.update {
                            it.copy(isLoading = false, transactions = uiModels, error = null)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Không có dữ liệu giao dịch.") }
                    }
                } else {
                    val errorMsg = "Lỗi tải giao dịch: ${transactionResponse.code()} - ${transactionResponse.message()}"
                    Log.e("ViewModelDebug", errorMsg)
                    _uiState.update { it.copy(isLoading = false, error = "Không thể tải giao dịch. Vui lòng thử lại.") }
                }

            } catch (e: Exception) {
                Log.e("ViewModelDebug", "Lỗi ngoại lệ khi fetch: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Đã xảy ra lỗi không mong muốn.") }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun TransactionDto.toTxUi(categoryMap: Map<String, CategoryDto>): TxUi {
    val localDateTime = try {
        LocalDateTime.parse(this.createdDate, apiDateFormatter)
    } catch (e: Exception) {
        LocalDateTime.now()
    }

    val isIncome = this.type.equals("Income", ignoreCase = true)
    val category = categoryMap[this.categoryId]
    val title = category?.name ?: (if (isIncome) "Thu nhập" else "Chi tiêu")
    val noteText = this.note?.takeIf { it.isNotBlank() && it != "string" } ?: "Không có ghi chú"
    val timeString = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    val subtitle = "$noteText • $timeString"

    return TxUi(
        id = this.transactionId,
        title = title,
        category = subtitle,
        emoji = category?.icon ?: "💰",
        dateTime = localDateTime,
        amount = this.amount,
        type = if (isIncome) TxType.INCOME else TxType.EXPENSE
    )
}

