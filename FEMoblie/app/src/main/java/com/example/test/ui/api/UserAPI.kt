import com.example.test.ui.api.ImageUploadResp
import com.example.test.ui.api.LoginReq
import com.example.test.ui.api.LoginResp
import com.example.test.ui.api.SignUpRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UsersApi {
    @POST("api/Users/login")
    suspend fun login(@Body body: LoginReq): Response<LoginResp>

    @POST("api/Users/Create")
    suspend fun register(@Body body: SignUpRequest): Response<Void>

    @Multipart
    @POST("api/Image/imageUpload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Query("embedBase64") embedBase64: Boolean = false
    ): Response<ImageUploadResp>
}
