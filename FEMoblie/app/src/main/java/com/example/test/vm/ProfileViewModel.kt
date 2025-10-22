package com.example.test.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.TransactionRepo
import com.example.test.data.UserRepo
import com.example.test.ui.api.AuthStore
import com.example.test.ui.models.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class SaveStatus { IDLE, LOADING, SUCCESS, ERROR }
data class ProfileUiState(
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val errorMessage: String? = null,
    val currentName: String = AuthStore.userName ?: "",
    val currentEmail: String = AuthStore.userEmail ?: "",
    val currentPhone: String = AuthStore.userPhone ?: "",
    val transactionCount: Int = 0,
    val activeDays: Long = 0
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val transactionRepo: TransactionRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val creationDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    init {
        loadProfileStats()
    }

    private fun loadProfileStats() {
        viewModelScope.launch {
            try {
                val response = transactionRepo.getAllFromAuth()
                if (response.isSuccessful) {
                    val count = response.body()?.transactions?.size ?: 0
                    _uiState.update { it.copy(transactionCount = count) }
                }
            } catch (e: Exception) {
            }

            AuthStore.userCreationDate?.let { dateString ->
                try {
                    val creationDate = LocalDate.parse(dateString, creationDateFormatter)
                    val days = ChronoUnit.DAYS.between(creationDate, LocalDate.now())
                    _uiState.update { it.copy(activeDays = days) }
                } catch (e: Exception) {
                }
            }
        }
    }

    fun updateUser(newName: String, newPhone: String) {
        viewModelScope.launch {
            val uid = AuthStore.userId
            val email = _uiState.value.currentEmail
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = "User not logged in") }
                return@launch
            }

            _uiState.update { it.copy(saveStatus = SaveStatus.LOADING) }

            val userDto = UserDto(
                userId = uid,
                name = newName,
                phoneNumber = newPhone,
                email = email
            )

            try {
                val response = userRepo.updateUser(userDto)
                if (response.isSuccessful) {
                    AuthStore.userName = newName
                    AuthStore.userPhone = newPhone
                    _uiState.update { it.copy(saveStatus = SaveStatus.SUCCESS) }
                } else {
                    val msg = response.errorBody()?.string() ?: "Cập nhật thất bại"
                    _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = msg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveStatus = SaveStatus.ERROR, errorMessage = e.message ?: "Lỗi không xác định") }
            }
        }
    }

    fun resetStatus() {
        _uiState.update { it.copy(saveStatus = SaveStatus.IDLE, errorMessage = null) }
    }
}

