// ui/auth/AuthViewModel.kt
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class AuthViewModel(private val api: AuthApi, private val appContext: Context): ViewModel() {

    fun sendLoginAsJsonFile(email: String, password: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = runCatching {
                // Tạo JSON an toàn (quote) để tránh ký tự đặc biệt
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                // File tạm trong cache, không để lại rác
                val file = withContext(Dispatchers.IO) {
                    File.createTempFile("credentials_", ".json", appContext.cacheDir).apply {
                        writeText(json)
                    }
                }

                try {
                    val part = MultipartBody.Part.createFormData(
                        name = "file",                           // đổi nếu BE yêu cầu
                        filename = "credentials.json",
                        body = file.asRequestBody("application/json".toMediaType())
                    )
                    val res = api.loginJsonFile(part)
                    res.isSuccessful
                } finally {
                    // Xóa file tạm sau khi upload
                    withContext(Dispatchers.IO) { file.delete() }
                }
            }.getOrElse { false }

            onDone(ok)
        }
    }
}
