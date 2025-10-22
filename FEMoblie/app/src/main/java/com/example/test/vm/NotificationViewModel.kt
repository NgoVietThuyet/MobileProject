package com.example.test.vm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.NotificationRepo
import com.example.test.ui.mock.NotificationItem
import com.example.test.ui.models.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val error: String? = null
)

private val notificationDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = repository.getAllNotifications()

                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!.notifications
                    val items = dtos.map { it.toNotificationItem() }
                    _uiState.update {
                        it.copy(isLoading = false, notifications = items, error = null)
                    }
                } else {
                    val errorMsg = "Lỗi ${response.code()}: ${response.message()}"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                val errorMsg = "Lỗi: ${e.message}"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(notifications = state.notifications.map {
                    if (it.id == id) it.copy(unread = false) else it
                })
            }
            try {
                repository.markAsRead(id)
            } catch (e: Exception) { /* Xử lý lỗi nếu cần */ }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(notifications = state.notifications.filterNot { it.id == id })
            }
            try {
                repository.deleteNotification(id)
            } catch (e: Exception) { /* Xử lý lỗi nếu cần */ }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun NotificationDto.toNotificationItem(): NotificationItem {
    return NotificationItem(
        id = this.id,
        unread = !this.isRead,
        message = this.content,
        title = "Thông báo chung",
        time = this.createdAt.formatTime(),
        iconEmoji = "🔔",
        iconBg = Color(0xFFE0F7FA)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun String.formatTime(): String {
    return try {
        val parsedDateTime = LocalDateTime.parse(this, notificationDateFormatter)
        val nowDate = LocalDate.now()
        val notificationDate = parsedDateTime.toLocalDate()

        val daysBetween = ChronoUnit.DAYS.between(notificationDate, nowDate)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())

        when (daysBetween) {
            0L -> "Hôm nay ${parsedDateTime.format(timeFormatter)}"
            1L -> "Hôm qua ${parsedDateTime.format(timeFormatter)}"
            else -> parsedDateTime.format(dateTimeFormatter)
        }
    } catch (e: Exception) {
        this
    }
}

