package com.example.test.ui.api

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

    val userIdFlow: StateFlow<String?> = _userId.asStateFlow()
    val userNameFlow: StateFlow<String?> = _userName.asStateFlow()
    val userEmailFlow: StateFlow<String?> = _userEmail.asStateFlow()
    val userPhoneFlow: StateFlow<String?> = _userPhone.asStateFlow()
    val userCreationDateFlow: StateFlow<String?> = _userCreationDate.asStateFlow()
    val tokenFlow: StateFlow<String?> = _token.asStateFlow()

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

    fun clear() {
        _userId.value = null
        _userName.value = null
        _userEmail.value = null
        _userPhone.value = null
        _userCreationDate.value = null
        _token.value = null
    }
}

object Api {
    private const val BASE_URL = "http://10.0.2.2:5012/"

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
}
