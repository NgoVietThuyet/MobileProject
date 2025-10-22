package com.example.test.vm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.AccountRepo
import com.example.test.data.TransactionRepo
import com.example.test.ui.api.AuthStore
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.TransactionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String? = null,
    val balance: String? = null,
    val recentTransactions: List<TxUi> = emptyList(),
    val error: String? = null
)

private val apiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val accountRepo: AccountRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val transactionDeferred = async { transactionRepo.getAllFromAuth() }
                val accountDeferred = async { accountRepo.getAccount() }

                val transactionResponse = transactionDeferred.await()
                val accountResponse = accountDeferred.await()

                if (transactionResponse.isSuccessful) {
                    val transactionBody = transactionResponse.body()
                    if (transactionBody != null) {
                        val categories = transactionRepo.getLocalCategories()
                        val dtos = transactionBody.transactions ?: emptyList()
                        val categoryMap = categories.associateBy { it.categoryId }
                        val uiModels = dtos
                            .map { it.toTxUi(categoryMap) }
                            .sortedByDescending { it.dateTime }
                            .take(4)

                        _uiState.update { it.copy(recentTransactions = uiModels) }
                    }
                } else {
                    val errorMsg = "Lỗi tải giao dịch: ${transactionResponse.code()}"
                    Log.e("HomeViewModelLog", errorMsg)
                    _uiState.update { it.copy(error = "Không thể tải giao dịch.") }
                }

                if (accountResponse.isSuccessful) {
                    val accountBody = accountResponse.body()
                    if (accountBody != null && accountBody.balance != null) {
                        val userName = AuthStore.userName
                        val balance = accountBody.balance.toLongOrNull() ?: 0L

                        _uiState.update { it.copy(userName = userName, balance = vn(balance)) }
                    }
                } else {
                    val errorMsg = "Lỗi tải tài khoản: ${accountResponse.code()}"
                    Log.e("HomeViewModelLog", errorMsg)
                    _uiState.update { currentState ->
                        val newError = if (currentState.error != null) "${currentState.error} và tài khoản." else "Không thể tải tài khoản."
                        currentState.copy(error = newError)
                    }
                }

            } catch (e: Exception) {
                Log.e("HomeViewModelLog", "Lỗi ngoại lệ khi fetch: ${e.message}", e)
                _uiState.update { it.copy(error = e.message ?: "Đã xảy ra lỗi.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

private fun vn(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)

@RequiresApi(Build.VERSION_CODES.O)
private fun formatHomeDate(dateTime: LocalDateTime): String {
    val now = LocalDate.now()
    val date = dateTime.toLocalDate()
    val daysBetween = ChronoUnit.DAYS.between(date, now)

    return when (daysBetween) {
        0L -> "Hôm nay"
        1L -> "Hôm qua"
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
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
    val subtitle = formatHomeDate(localDateTime)

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

