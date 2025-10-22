package com.example.test.ui.scan

import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import com.example.test.data.ImageRepo

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val imageRepo: ImageRepo
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadUIState>(UploadUIState.Idle)
    val uploadState: StateFlow<UploadUIState> = _uploadState

    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadUIState.Loading
            try {
                val result = imageRepo.upload(imageUri)
                _uploadState.value = if (result.success) {
                    UploadUIState.Success(result.url)
                } else {
                    UploadUIState.Error(result.message ?: "Lỗi không xác định")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uploadState.value = UploadUIState.Error(e.message ?: "Đã xảy ra lỗi khi upload")
            }
        }
    }

    fun resetState() {
        _uploadState.value = UploadUIState.Idle
    }
}

sealed class UploadUIState {
    data object Idle : UploadUIState()
    data object Loading : UploadUIState()
    data class Success(val imageUrl: String?) : UploadUIState()
    data class Error(val message: String) : UploadUIState()
}

@Parcelize
data class UploadResult(
    val success: Boolean,
    val url: String?,
    val message: String?
) : Parcelable
