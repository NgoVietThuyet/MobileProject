package com.example.test.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.SavingGoalApi
import com.example.test.ui.models.SavingGoalDto
import com.example.test.ui.models.SavingGoalDeleteReq
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import retrofit2.Response

private const val TAG = "SavingsViewModel"

data class SavingsUiState(
    val isLoading: Boolean = true,
    val savingGoals: List<SavingGoalDto> = emptyList(),
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val savingGoalApi: SavingGoalApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null as String?) }
            try {
                val userId = AuthStore.userId.orEmpty()
                if (userId.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
                    return@launch
                }

                val response: Response<List<SavingGoalDto>> = savingGoalApi.getAllSavingGoals(userId)

                if (response.isSuccessful) {
                    val goalsList: List<SavingGoalDto>? = response.body()
                    Log.d(TAG, "Goals received from API: ${goalsList.toString()}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            savingGoals = goalsList.orEmpty(),
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error fetching goals: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load saving goals", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null, deleteSuccess = false) }
            try {
                val request = SavingGoalDeleteReq(id = goalId)
                val response = savingGoalApi.deleteSavingGoal(request)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
                    loadGoals()
                } else {
                    _uiState.update { it.copy(isDeleting = false, deleteError = "Error deleting: ${response.code()}") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete saving goal", e)
                _uiState.update { it.copy(isDeleting = false, deleteError = e.message ?: "Unknown error") }
            }
        }
    }


    fun clearDeleteStatus() {
        _uiState.update { it.copy(deleteSuccess = false, deleteError = null) }
    }
}