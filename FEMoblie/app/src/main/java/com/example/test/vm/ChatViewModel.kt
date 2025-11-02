package com.example.test.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.ChatbotRepo
import com.example.test.data.LocalCategoryDataSource
import com.example.test.ui.api.Api
import com.example.test.ui.api.AuthStore
import com.example.test.ui.models.CreateTransactionRequest
import com.example.test.ui.models.TransactionDto
import com.example.test.ui.screens.ChatMessage
import com.example.test.ui.screens.ChatSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatbotRepo
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _createTransactionStatus = MutableStateFlow<CreateTransactionStatus>(CreateTransactionStatus.Idle)
    val createTransactionStatus: StateFlow<CreateTransactionStatus> = _createTransactionStatus.asStateFlow()

    init {
        viewModelScope.launch {
            AuthStore.userIdFlow.collectLatest { uid ->
                if (uid == null) _messages.value = emptyList()
            }
        }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text = text.trim(), sender = ChatSender.USER)
        _messages.update { it + userMsg }
        viewModelScope.launch {
            val typingId = UUID.randomUUID().toString()
            _messages.update { it + ChatMessage(id = typingId, text = "Đang soạn trả lời...", sender = ChatSender.BOT) }
            val result = repo.extract(text)
            val bot = result.getOrElse { ex -> "Lỗi gọi API: ${ex.message ?: "không xác định"}" }
            _messages.update { cur ->
                cur.filterNot { it.id == typingId } + ChatMessage(text = bot, sender = ChatSender.BOT)
            }
        }
    }

    fun createTransaction(
        amount: String,
        categoryId: String?,
        categoryName: String?,
        note: String?,
        type: String
    ) {
        viewModelScope.launch {
            _createTransactionStatus.value = CreateTransactionStatus.Loading
            try {
                val userId = AuthStore.userId
                if (userId.isNullOrBlank()) {
                    _createTransactionStatus.value = CreateTransactionStatus.Error("Vui lòng đăng nhập")
                    return@launch
                }

                // Resolve categoryId: use provided ID, or lookup by name, or use default
                val resolvedCategoryId = when {
                    !categoryId.isNullOrBlank() -> categoryId
                    !categoryName.isNullOrBlank() -> {
                        // Try to find category by name
                        val foundCategory = LocalCategoryDataSource.findByName(categoryName)
                        foundCategory?.categoryId ?: LocalCategoryDataSource.getDefaultCategory().categoryId
                    }
                    else -> LocalCategoryDataSource.getDefaultCategory().categoryId
                }

                android.util.Log.d("ChatBot", "Creating transaction with categoryId: $resolvedCategoryId (from name: $categoryName)")

                val transactionDto = TransactionDto(
                    transactionId = "",
                    userId = userId,
                    categoryId = resolvedCategoryId,
                    type = type,
                    amount = amount,
                    note = note,
                    createdDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    updatedDate = null
                )

                val request = CreateTransactionRequest(transaction = transactionDto)
                val response = Api.transactionService.createTransaction(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _createTransactionStatus.value = CreateTransactionStatus.Success
                } else {
                    val errorMsg = response.body()?.message ?: "Lỗi tạo giao dịch"
                    _createTransactionStatus.value = CreateTransactionStatus.Error(errorMsg)
                }
            } catch (e: Exception) {
                _createTransactionStatus.value = CreateTransactionStatus.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun resetCreateTransactionStatus() {
        _createTransactionStatus.value = CreateTransactionStatus.Idle
    }

    fun resetMessages() {
        _messages.value = emptyList()
    }
}

sealed class CreateTransactionStatus {
    object Idle : CreateTransactionStatus()
    object Loading : CreateTransactionStatus()
    object Success : CreateTransactionStatus()
    data class Error(val message: String) : CreateTransactionStatus()
}
