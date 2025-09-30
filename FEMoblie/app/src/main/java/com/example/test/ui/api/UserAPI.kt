// com.example.test.ui.api.UsersApi.kt
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {
    @POST("api/Users/login")
    suspend fun login(@Body body: LoginReq): Response<LoginResp>
}
