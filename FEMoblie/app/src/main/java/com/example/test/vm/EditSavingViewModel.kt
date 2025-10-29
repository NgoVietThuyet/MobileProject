package com.example.test.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.api.SavingGoalApi
import com.example.test.ui.models.SavingGoalDeleteReq
import com.example.test.ui.models.SavingGoalUpdateAmountReq
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SavingDetailVM"

data class SavingDetailUiState(
    val isUpdatingAmount: Boolean = false,
    val updateAmountError: String? = null,
    val updateAmountSuccess: Boolean = false,

    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class SavingDetailViewModel @Inject constructor(
    private val savingGoalApi: SavingGoalApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingDetailUiState())
    val uiState: StateFlow<SavingDetailUiState> = _uiState.asStateFlow()

    fun updateAmount(goalId: String?, amount: Long) {
        if (goalId.isNullOrBlank() || amount == 0L) {
            Log.w(TAG, "updateAmount called with invalid goalId or zero amount.")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingAmount = true,
                    updateAmountError = null,
                    updateAmountSuccess = false
                )
            }

            val isAdding = amount > 0
            val absAmountStr = kotlin.math.abs(amount).toString()

            val request = SavingGoalUpdateAmountReq(
                goalId = goalId,
                updateAmount = absAmountStr,
                isAddAmount = isAdding
            )

            try {
                Log.d(TAG, "Updating amount for goal $goalId: amount=$amount, isAdding=$isAdding")
                val response = savingGoalApi.updateSavingGoalAmount(request)

                if (response.isSuccessful) {
                    Log.i(TAG, "Amount updated successfully for goal $goalId")
                    _uiState.update {
                        it.copy(
                            isUpdatingAmount = false,
                            updateAmountSuccess = true
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error updating amount ${response.code()}: $errorBody")
                    _uiState.update {
                        it.copy(
                            isUpdatingAmount = false,
                            updateAmountError = "Lỗi ${response.code()}: ${errorBody ?: "Cập nhật thất bại."}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during updateAmount", e)
                _uiState.update {
                    it.copy(
                        isUpdatingAmount = false,
                        updateAmountError = e.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }

    fun deleteGoal(goalId: String?) {
        if (goalId.isNullOrBlank()){
            Log.w(TAG, "deleteGoal called with invalid goalId.")
            _uiState.update { it.copy(deleteError = "ID mục tiêu không hợp lệ.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null, deleteSuccess = false) }
            try {
                val request = SavingGoalDeleteReq(id = goalId)
                Log.d(TAG, "Deleting goal $goalId")
                val response = savingGoalApi.deleteSavingGoal(request)

                if (response.isSuccessful) {
                    Log.i(TAG, "Goal $goalId deleted successfully")
                    _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error deleting goal ${response.code()}: $errorBody")
                    _uiState.update { it.copy(isDeleting = false, deleteError = "Lỗi ${response.code()}: ${errorBody ?: "Xóa thất bại."}") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during deleteGoal", e)
                _uiState.update { it.copy(isDeleting = false, deleteError = e.message ?: "Lỗi không xác định") }
            }
        }
    }

    fun clearStatusFlags() {
        _uiState.update {
            it.copy(
                updateAmountSuccess = false,
                updateAmountError = null,
                deleteSuccess = false,
                deleteError = null
            )
        }
    }
}