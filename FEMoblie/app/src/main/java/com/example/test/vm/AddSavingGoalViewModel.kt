package com.example.test.vm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.SavingGoalApi
import com.example.test.ui.models.SavingGoalCreatePayload
import com.example.test.ui.models.SavingGoalCreateReq
import com.example.test.ui.screens.SavingGoalDraft
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
import com.google.gson.Gson
import com.example.test.data.SavingGoalCategories

private const val TAG = "AddSavingGoalVM"

data class AddSavingGoalUiState(
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AddSavingGoalViewModel @Inject constructor(
    private val savingGoalApi: SavingGoalApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSavingGoalUiState())
    val uiState: StateFlow<AddSavingGoalUiState> = _uiState.asStateFlow()

    private val apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val zone = ZoneId.systemDefault()
    private val gson = Gson()

    fun submitGoal(draft: SavingGoalDraft) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, success = false, error = null) }

            val userId = AuthStore.userId.orEmpty()
            if (userId.isBlank()) {
                _uiState.update { it.copy(isSubmitting = false, error = "Bạn chưa đăng nhập.") }
                return@launch
            }

            val selectedEmoji = draft.emoji
            val categoryId = SavingGoalCategories.categories.find { it.icon == selectedEmoji }?.categoryId
            if (categoryId.isNullOrBlank()) {
                Log.e(TAG, "Could not find categoryId for emoji: $selectedEmoji")
                _uiState.update { it.copy(isSubmitting = false, error = "Biểu tượng không hợp lệ.") }
                return@launch
            }

            val deadlineString = draft.targetDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(zone).toLocalDate().format(apiDateFormatter)
            } ?: ""

            val newGoalId = UUID.randomUUID().toString()


            val innerRequest = SavingGoalCreateReq(
                savingGoalId = newGoalId,
                userId = userId,
                categoryId = categoryId,
                title = draft.title,
                targetAmount = draft.targetVnd.toString(),
                currentAmount = "0",
                deadline = deadlineString,
            )

            val payload = SavingGoalCreatePayload(savingGoalDto = innerRequest)

            try {
                val jsonPayload = gson.toJson(payload)
                Log.d(TAG, "Sending JSON Payload to /Create: $jsonPayload")

                val response = savingGoalApi.createSavingGoal(payload)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, success = true) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error ${response.code()}: $errorBody")
                    _uiState.update { it.copy(isSubmitting = false, error = "Lỗi ${response.code()}: ${errorBody ?: "Không thể tạo mục tiêu."}") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during createSavingGoal", e)
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Lỗi không xác định") }
            }
        }
    }
}