// network/AuthApi.kt
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @Multipart
    @POST("auth/login-file") // đổi path theo BE của bạn
    suspend fun loginJsonFile(
        @Part file: MultipartBody.Part // tên field "file" — đổi nếu BE yêu cầu tên khác
    ): retrofit2.Response<Unit> // hoặc LoginResponse nếu BE trả user/token
}
