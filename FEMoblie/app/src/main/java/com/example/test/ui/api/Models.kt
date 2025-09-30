// com.example.test.ui.api.Models.kt
data class LoginReq(val email: String, val password: String)

data class LoginResp(
    val token: String,
    val expires: String? = null
)
