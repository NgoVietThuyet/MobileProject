package com.example.test.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.api.Api
import com.example.test.ui.api.AuthStore
import com.example.test.ui.models.BudgetCreateResponse
import com.example.test.ui.models.CreateBudgetReq
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

data class AddBudgetUiState(
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddBudgetViewModel @Inject constructor() : ViewModel() {

    private val api = Api.budgetService

    private val _ui = MutableStateFlow(AddBudgetUiState())
    val ui: StateFlow<AddBudgetUiState> = _ui

    fun submit(
        categoryId: String,
        amountVnd: String,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            _ui.value = AddBudgetUiState(isSubmitting = true)

            val userId = AuthStore.userId.orEmpty()
            if (userId.isBlank()) {
                _ui.value = AddBudgetUiState(isSubmitting = false, error = "Bạn chưa đăng nhập.")
                return@launch
            }

            try {
                val req = CreateBudgetReq(
                    userId = userId,
                    categoryId = categoryId,
                    initialAmount = amountVnd,
                    currentAmount = "0",
                    startDate = startDate,
                    endDate = endDate
                )

                val res: Response<BudgetCreateResponse> = api.createBudget(req)

                _ui.value = if (res.isSuccessful) {
                    AddBudgetUiState(isSubmitting = false, success = true)
                } else {
                    AddBudgetUiState(isSubmitting = false, error = "HTTP ${res.code()}")
                }
            } catch (e: Exception) {
                _ui.value = AddBudgetUiState(isSubmitting = false, error = e.message)
            }
        }
    }
}
