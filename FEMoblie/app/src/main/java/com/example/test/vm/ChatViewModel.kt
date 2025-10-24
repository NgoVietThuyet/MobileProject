package com.example.test.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.ChatbotRepo
import com.example.test.ui.api.AuthStore
import com.example.test.ui.screens.ChatMessage
import com.example.test.ui.screens.ChatSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatbotRepo
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

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

    fun resetMessages() {
        _messages.value = emptyList()
    }
}
