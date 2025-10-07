import com.example.test.ui.api.LoginReq
import com.example.test.ui.api.LoginResp
import com.example.test.ui.api.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {
    @POST("api/Users/login")
    suspend fun login(@Body body: LoginReq): Response<LoginResp>

    @POST("api/Users/Create")
    suspend fun register(@Body body: SignUpRequest): Response<Void>
}
