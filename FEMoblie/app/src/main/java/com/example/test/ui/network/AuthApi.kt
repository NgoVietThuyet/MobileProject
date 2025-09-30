// network/AuthApi.kt
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @Multipart
    @POST("auth/login-file")
    suspend fun loginJsonFile(
        @Part file: MultipartBody.Part
    ): retrofit2.Response<Unit>
}
