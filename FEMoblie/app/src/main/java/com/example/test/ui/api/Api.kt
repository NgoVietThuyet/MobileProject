package com.example.test.ui.api

import com.example.test.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthStore {
    private val _userId = MutableStateFlow<String?>(null)
    private val _userName = MutableStateFlow<String?>(null)
    private val _userEmail = MutableStateFlow<String?>(null)
    private val _userPhone = MutableStateFlow<String?>(null)
    private val _userCreationDate = MutableStateFlow<String?>(null)
    private val _token = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)

    val userIdFlow: StateFlow<String?> = _userId.asStateFlow()

    var userId: String?
        get() = _userId.value
        set(value) {
            println("AuthStore: userId được cập nhật thành -> $value")
            _userId.value = value
        }

    var userName: String?
        get() = _userName.value
        set(value) {
            println("AuthStore: userName được cập nhật thành -> $value")
            _userName.value = value
        }

    var userEmail: String?
        get() = _userEmail.value
        set(value) {
            println("AuthStore: userEmail được cập nhật thành -> $value")
            _userEmail.value = value
        }

    var userPhone: String?
        get() = _userPhone.value
        set(value) {
            println("AuthStore: userPhone được cập nhật thành -> $value")
            _userPhone.value = value
        }

    var userCreationDate: String?
        get() = _userCreationDate.value
        set(value) {
            println("AuthStore: userCreationDate được cập nhật thành -> $value")
            _userCreationDate.value = value
        }

    var token: String?
        get() = _token.value
        set(value) { _token.value = value }

    var refreshToken: String?
        get() = _refreshToken.value
        set(value) { _refreshToken.value = value }

    fun loadFromDataStore(
        userId: String?,
        userName: String?,
        userEmail: String?,
        userPhone: String?,
        userCreationDate: String?,
        token: String?,
        refreshToken: String?
    ) {
        _userId.value = userId
        _userName.value = userName
        _userEmail.value = userEmail
        _userPhone.value = userPhone
        _userCreationDate.value = userCreationDate
        _token.value = token
        _refreshToken.value = refreshToken
        println("AuthStore: Đã tải dữ liệu từ DataStore")
    }

    fun updateTokens(token: String, refreshToken: String) {
        _token.value = token
        _refreshToken.value = refreshToken
        println("AuthStore: Đã cập nhật tokens")
    }

    fun clear() {
        _userId.value = null
        _userName.value = null
        _userEmail.value = null
        _userPhone.value = null
        _userCreationDate.value = null
        _token.value = null
        _refreshToken.value = null
    }
}

object Api {
    private val BASE_URL = "http://10.0.2.2:5012/"

    var onTokensRefreshed: ((String, String) -> Unit)? = null

    private val refreshRetrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val refreshClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(refreshClient)
            .build()
    }

    private val refreshService: UsersApi by lazy {
        refreshRetrofit.create(UsersApi::class.java)
    }

    private val tokenAuthenticator = okhttp3.Authenticator { _, response ->

        if (response.request.header("Authorization-Retry") != null) {
            return@Authenticator null // Give up, we already retried
        }

        val refreshToken = AuthStore.refreshToken
        if (refreshToken.isNullOrBlank()) {
            return@Authenticator null
        }

        try {
            val refreshResponse = kotlinx.coroutines.runBlocking {
                refreshService.refreshToken(
                    com.example.test.ui.models.RefreshTokenReq(refreshToken)
                )
            }

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newAccessToken = refreshResponse.body()!!.accessToken
                val newRefreshToken = refreshResponse.body()!!.refreshToken

                // Update tokens in AuthStore
                AuthStore.updateTokens(newAccessToken, newRefreshToken)

                // Notify callback to save to DataStore
                onTokensRefreshed?.invoke(newAccessToken, newRefreshToken)

                // Retry the request with new token
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .header("Authorization-Retry", "true")
                    .build()
            } else {
                // Refresh failed, clear auth
                AuthStore.clear()
                return@Authenticator null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@Authenticator null
        }
    }

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = okhttp3.Interceptor { chain ->
            val originalRequest = chain.request()
            val token = AuthStore.token
            val requestBuilder = originalRequest.newBuilder()
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val usersService: UsersApi by lazy {
        retrofit.create(UsersApi::class.java)
    }
    val imageService: ImageApi by lazy {
        retrofit.create(ImageApi::class.java)
    }
    val notificationService: NotificationApi by lazy {
        retrofit.create(NotificationApi::class.java)
    }
    val transactionService: TransactionApi by lazy {
        retrofit.create(TransactionApi::class.java)
    }
    val accountService: AccountApi by lazy {
        retrofit.create(AccountApi::class.java)
    }

    val chatbotService: ChatbotApi by lazy {
        retrofit.create(ChatbotApi::class.java)
    }
    val budgetService: BudgetApi by lazy {
        retrofit.create(BudgetApi::class.java)
    }
    val reportService: ReportApi by lazy {
        retrofit.create(ReportApi::class.java)
    }
    val savinggoalService: SavingGoalApi by lazy {
        retrofit.create(SavingGoalApi::class.java)
    }
}
