import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.ImageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repo: ImageRepo
) : ViewModel() {

    fun upload(uri: Uri, onDone: (UploadResult) -> Unit) {
        viewModelScope.launch {
            onDone(repo.upload(uri))
        }
    }
}
@Parcelize
data class UploadResult(
    val success: Boolean,
    val url: String?,
    val message: String?
) : Parcelable

