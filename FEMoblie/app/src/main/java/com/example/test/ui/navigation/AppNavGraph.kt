package com.example.test.ui.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.test.ui.models.LoginReq
import com.example.test.ui.models.SignUpRequest
import com.example.test.ui.models.UserDto
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.screens.*
import com.example.test.vm.SettingsViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import com.example.test.ui.mock.MockData as HomeMock
import org.json.JSONArray
import org.json.JSONObject
import com.example.test.ui.api.Api
import com.example.test.ui.api.AuthStore
import com.example.test.ui.scan.UploadResult
import com.example.test.utils.SoundManager
import com.example.test.vm.AllTransactionViewModel

private fun extractServerMessage(raw: String): String? {
    try {
        val obj = JSONObject(raw)

        val title = obj.optString("title").takeIf { it.isNotBlank() }
        if (title != null && title.contains("validation", ignoreCase = true)) {
            if (obj.has("errors")) {
                val errs = obj.getJSONObject("errors")
                val errorMessages = mutableListOf<String>()
                errs.keys().forEach { key ->
                    val arr = errs.optJSONArray(key)
                    if (arr != null && arr.length() > 0) {
                        val errorMsg = arr.getString(0)
                        val translatedMsg = when {
                            errorMsg.contains("required", ignoreCase = true) -> "Trường này bắt buộc"
                            errorMsg.contains("invalid", ignoreCase = true) -> "Giá trị không hợp lệ"
                            errorMsg.contains("email", ignoreCase = true) && errorMsg.contains("format", ignoreCase = true) -> "Email không đúng định dạng"
                            errorMsg.contains("email", ignoreCase = true) -> "Email không hợp lệ"
                            errorMsg.contains("password", ignoreCase = true) && errorMsg.contains("incorrect", ignoreCase = true) -> "Mật khẩu không đúng"
                            errorMsg.contains("password", ignoreCase = true) && errorMsg.contains("length", ignoreCase = true) -> "Mật khẩu phải có ít nhất 6 ký tự"
                            errorMsg.contains("password", ignoreCase = true) -> "Mật khẩu không hợp lệ"
                            errorMsg.contains("user", ignoreCase = true) && errorMsg.contains("not found", ignoreCase = true) -> "Không tìm thấy người dùng"
                            errorMsg.contains("already exists", ignoreCase = true) -> "Thông tin đã tồn tại"
                            errorMsg.contains("unauthorized", ignoreCase = true) -> "Không có quyền truy cập"
                            else -> errorMsg
                        }
                        errorMessages.add(translatedMsg)
                    }
                }
                if (errorMessages.isNotEmpty()) {
                    return errorMessages.joinToString(", ")
                } else {
                    return "Thông tin không hợp lệ"
                }
            }
            return "Thông tin không hợp lệ"
        }

        // Xử lý message thông thường
        return obj.optString("message").takeIf { it.isNotBlank() }
            ?: obj.optString("detail").takeIf { it.isNotBlank() }
            ?: title
            ?: run {
                if (obj.has("errors")) {
                    val errs = obj.get("errors")
                    when (errs) {
                        is JSONArray -> (0 until errs.length()).joinToString("; ") { errs.getString(it) }
                        is JSONObject -> errs.keys().asSequence().joinToString("; ") { key ->
                            val arr = errs.optJSONArray(key)
                            if (arr != null) (0 until arr.length()).joinToString(", ") { arr.getString(it) }
                            else errs.optString(key)
                        }
                        else -> errs.toString()
                    }
                } else null
            }
    } catch (_: Exception) {
        return raw.ifBlank { null }
    }
}

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object EmailLogin : Screen("email_login")
    data object PhoneLogin : Screen("phone_login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object BudgetAll : Screen("budget_all")
    data object TransactionsAll : Screen("transactions_all")
    data object OtpVerification : Screen("otp_verification/{phone}") {
        fun createRoute(phone: String) = "otp_verification/${Uri.encode(phone)}"
        const val ARG = "phone"
    }
    data object BudgetEdit : Screen("budget_edit/{index}") {
        fun create(index: Int) = "budget_edit/$index"
        const val ARG = "index"
    }
    object BudgetCreate {
        const val ARG = "userId"
        const val PATTERN = "budget_create/{$ARG}"
        fun route(userId: String) = "budget_create/$userId"
    }
    data object IncomeCreate : Screen("income_create")
    data object ExpenseCreate : Screen("expense_create")
    data object Notifications : Screen("notifications")
    data object EditIncome : Screen("edit_income/{tx}") {
        const val ARG = "tx"
        @RequiresApi(Build.VERSION_CODES.O)
        fun create(tx: TxUi): String = "edit_income/${Uri.encode(Gson().toJson(TxNav.from(tx)))}"
    }
    data object EditExpense : Screen("edit_expense/{tx}") {
        const val ARG = "tx"
        @RequiresApi(Build.VERSION_CODES.O)
        fun create(tx: TxUi): String = "edit_expense/${Uri.encode(Gson().toJson(TxNav.from(tx)))}"
    }
    data object Report : Screen("report")
    data object Saving : Screen("saving")
    data object SavingDetail : Screen("saving_detail/{index}") {
        const val ARG = "index"
        fun create(index: String) = "saving_detail/$index"
    }
    data object SavingAdd : Screen("saving_add")
    data object Setting : Screen("setting")
    data object PersonalInfo : Screen("personal_info")
    data object ProfilePicture : Screen("profile_picture")
    data object CurrentUnit : Screen("current_unit")
    data object Scan : Screen("scan")
    data object ScanResult : Screen("scan_result")
    data object ChangePassword : Screen("change_password")
}

private data class TxNav(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val type: String,
    val emoji: String?,
    val epochMillis: Long
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun from(tx: TxUi): TxNav =
            TxNav(
                id = tx.id,
                title = tx.title,
                category = tx.category,
                amount = tx.amount,
                type = tx.type.name,
                emoji = tx.emoji,
                epochMillis = tx.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun toTxUi(): TxUi =
        TxUi(
            id = id,
            title = title,
            category = category,
            dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()),
            amount = amount,
            type = TxType.valueOf(type),
            emoji = emoji
        )
}

private fun androidx.navigation.NavController.switchTo(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val settingsVm: SettingsViewModel = viewModel()
    val dark by settingsVm.darkMode.collectAsStateWithLifecycle()
    val soundEnabled by settingsVm.soundEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authRepo = remember { com.example.test.data.AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var startDest by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val userId = authRepo.userIdFlow.first()
            val userName = authRepo.userNameFlow.first()
            val userEmail = authRepo.userEmailFlow.first()
            val userPhone = authRepo.userPhoneFlow.first()
            val userCreationDate = authRepo.userCreationDateFlow.first()
            val token = authRepo.tokenFlow.first()
            val refreshToken = authRepo.refreshTokenFlow.first()

            AuthStore.loadFromDataStore(userId, userName, userEmail, userPhone, userCreationDate, token, refreshToken)

            Api.onTokensRefreshed = { newAccessToken, newRefreshToken ->
                scope.launch {
                    authRepo.updateTokens(newAccessToken, newRefreshToken)
                }
            }

            startDest = if (userId != null) "main" else "auth"
        }
    }

    if (startDest == null) {
        return
    }

    NavHost(navController = navController, startDestination = startDest!!) {
        navigation(startDestination = Screen.Login.route, route = "auth") {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToEmail = { navController.navigate(Screen.EmailLogin.route) },
                    onNavigateToPhone = { navController.navigate(Screen.PhoneLogin.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.EmailLogin.route) {
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val usersApi = remember { Api.usersService }
                val authRepo = remember { com.example.test.data.AuthRepository(ctx) }

                EmailLoginScreen(
                    onBack = { navController.popBackStack() },
                    onLogin = { email, password, onError ->
                        scope.launch {
                            try {
                                val res = usersApi.login(LoginReq(email, password))
                                val body = res.body()
                                if (res.isSuccessful && body != null) {
                                    AuthStore.userId = body.user.userId
                                    AuthStore.userName = body.user.name
                                    AuthStore.userEmail = body.user.email
                                    AuthStore.userPhone = body.user.phoneNumber
                                    AuthStore.userCreationDate = body.user.createdDate
                                    AuthStore.token = body.accessToken
                                    AuthStore.refreshToken = body.refreshToken

                                    authRepo.saveUserInfo(
                                        userId = body.user.userId,
                                        userName = body.user.name,
                                        userEmail = body.user.email,
                                        userPhone = body.user.phoneNumber,
                                        userCreationDate = body.user.createdDate,
                                        token = body.accessToken,
                                        refreshToken = body.refreshToken
                                    )

                                    navController.navigate("main") {
                                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    val msg = res.errorBody()?.string()?.let(::extractServerMessage)
                                        ?: "❌ Đăng nhập thất bại"
                                    onError(msg)
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (_: java.net.UnknownHostException) {
                                onError("❌ Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng")
                            } catch (_: java.net.SocketTimeoutException) {
                                onError("❌ Hết thời gian chờ. Vui lòng thử lại")
                            } catch (_: java.net.ConnectException) {
                                onError("❌ Không thể kết nối đến máy chủ")
                            } catch (_: java.io.IOException) {
                                onError("❌ Lỗi kết nối. Vui lòng thử lại")
                            } catch (_: Exception) {
                                onError("❌ Đăng nhập thất bại. Vui lòng thử lại")
                            }
                        }
                    },
                    onRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.PhoneLogin.route) {
                PhoneLoginScreen(
                    onBack = { navController.popBackStack() },
                    onRequestOtp = { phone, verificationId ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("verificationId", verificationId)
                        navController.navigate(Screen.OtpVerification.createRoute(phone))
                    }
                )
            }

            composable(
                route = Screen.OtpVerification.route,
                arguments = listOf(navArgument(Screen.OtpVerification.ARG) { type = NavType.StringType }),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val phone = Uri.decode(backStackEntry.arguments?.getString(Screen.OtpVerification.ARG).orEmpty())
                val verificationId = navController.previousBackStackEntry?.savedStateHandle?.get<String>("verificationId") ?: ""

                OtpVerificationScreen(
                    phoneNumber = phone,
                    verificationId = verificationId,
                    onBack = { navController.popBackStack() },
                    onVerify = {
                        navController.navigate("main") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val usersApi = remember { Api.usersService }

                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegister = { fullName, email, phone, password, onError ->
                        val now = Instant.now().toString()
                        val id = UUID.randomUUID().toString()
                        scope.launch {
                            try {
                                val res = usersApi.register(
                                    SignUpRequest(
                                        UserDto(
                                            userId = id,
                                            name = fullName.trim(),
                                            phoneNumber = phone.trim(),
                                            email = email.trim(),
                                            password = password,
                                            createdDate = now,
                                            updatedDate = now
                                        ),
                                        confirmPassword = password
                                    )
                                )

                                if (res.isSuccessful) {
                                    Toast.makeText(ctx, "✅ Đăng ký thành công", Toast.LENGTH_LONG).show()
                                    navController.popBackStack(Screen.Login.route, false)
                                } else {
                                    val code = res.code()
                                    val serverMsg = res.errorBody()?.string()?.let(::extractServerMessage)
                                    val msg = serverMsg ?: when (code) {
                                        400 -> "❌ Thông tin không hợp lệ. Kiểm tra họ tên, email, số điện thoại, mật khẩu"
                                        401 -> "❌ Bạn chưa được xác thực"
                                        403 -> "❌ Bạn không có quyền thực hiện thao tác này"
                                        404 -> "❌ Không tìm thấy endpoint đăng ký"
                                        409 -> "❌ Email hoặc số điện thoại đã tồn tại"
                                        422 -> "❌ Dữ liệu không hợp lệ"
                                        429 -> "❌ Quá nhiều yêu cầu. Thử lại sau"
                                        in 500..599 -> "❌ Máy chủ lỗi ($code). Thử lại sau"
                                        else -> "❌ Đăng ký thất bại ($code)"
                                    }
                                    onError(msg)
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (_: java.net.UnknownHostException) {
                                onError("❌ Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng")
                            } catch (_: java.net.SocketTimeoutException) {
                                onError("❌ Hết thời gian chờ. Vui lòng thử lại")
                            } catch (_: java.net.ConnectException) {
                                onError("❌ Không thể kết nối đến máy chủ")
                            } catch (_: java.io.IOException) {
                                onError("❌ Lỗi kết nối. Vui lòng thử lại")
                            } catch (_: Exception) {
                                onError("❌ Đăng ký thất bại. Vui lòng thử lại")
                            }
                        }
                    }
                )
            }
        }

        navigation(startDestination = Screen.Home.route, route = "main") {

            composable(
                route = Screen.Home.route,
                enterTransition = { null },
                exitTransition = { null }
            ) {
                HomeScreen(
                    onOpenBudgetAll = { navController.navigate(Screen.BudgetAll.route) },
                    onAddIncome = { navController.navigate(Screen.IncomeCreate.route) },
                    onAddExpense = { navController.navigate(Screen.ExpenseCreate.route) },
                    onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                    onOpenAllTransactions = { navController.navigate(Screen.TransactionsAll.route) },
                    onReport = { navController.switchTo(Screen.Report.route) },
                    onSaving = { navController.switchTo(Screen.Saving.route) },
                    onSetting = { navController.switchTo(Screen.Setting.route) },
                    onCamera = { navController.navigate(Screen.Scan.route) }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.BudgetAll.route) {
                val ctx = LocalContext.current
                val uid by AuthStore.userIdFlow.collectAsState()

                if (uid.isNullOrBlank()) {
                    LaunchedEffect("nav_to_auth") {
                        Toast.makeText(ctx, "Vui lòng đăng nhập để xem ngân sách", Toast.LENGTH_LONG).show()
                        navController.navigate("auth") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    BudgetAllScreen(
                        onBack = { navController.popBackStack() },
                        onOpenEdit = { idx -> navController.navigate(Screen.BudgetEdit.create(idx)) },
                        onAdd = { navController.navigate(Screen.BudgetCreate.PATTERN) }
                    )
                }
            }


            composable(
                route = Screen.BudgetEdit.route,
                arguments = listOf(navArgument(Screen.BudgetEdit.ARG) { type = NavType.IntType })
            ) { backStack ->
                val idx = backStack.arguments!!.getInt(Screen.BudgetEdit.ARG)
                BudgetEditScreen(index = idx, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.BudgetCreate.PATTERN
            ) {
                val userId by AuthStore.userIdFlow.collectAsStateWithLifecycle()

                if (userId.isNullOrBlank()) {
                    Text("Không thể tạo ngân sách: bạn chưa đăng nhập.")
                } else {
                    AddBudgetScreen(
                        onBack = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() },
                        onCreate = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("budget_created", true)
                            navController.popBackStack()
                        }
                    )
                }
            }




            composable(Screen.IncomeCreate.route) {
                AddIncomeScreen(
                    onBack = { navController.navigate(Screen.Home.route) },
                )
            }

            composable(Screen.ExpenseCreate.route) {
                AddExpenseScreen(
                    onBack = { navController.navigate(Screen.Home.route) },
                )
            }

            composable(Screen.TransactionsAll.route) {
                val vm: AllTransactionViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { vm.fetchTransactions() }

                AllTransactionsScreen(
                    transactions = state.transactions,
                    onEditIncome = { tx -> navController.navigate(Screen.EditIncome.create(tx)) },
                    onEditExpense = { tx -> navController.navigate(Screen.EditExpense.create(tx)) },
                    onBack = { navController.popBackStack() }
                )
            }



            composable(
                route = Screen.EditIncome.route,
                arguments = listOf(navArgument(Screen.EditIncome.ARG) { type = NavType.StringType })
            ) { backStack ->
                val json = Uri.decode(backStack.arguments?.getString(Screen.EditIncome.ARG).orEmpty())
                val nav = Gson().fromJson(json, TxNav::class.java)
                EditIncomeScreen(
                    tx = nav.toTxUi(),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.EditExpense.route,
                arguments = listOf(navArgument(Screen.EditExpense.ARG) { type = NavType.StringType })
            ) { backStack ->
                val json = Uri.decode(backStack.arguments?.getString(Screen.EditExpense.ARG).orEmpty())
                val nav = Gson().fromJson(json, TxNav::class.java)
                EditExpenseScreen(
                    tx = nav.toTxUi(),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.Report.route,
                enterTransition = { null },
                exitTransition = { null }
            ) {
                ReportScreen(
                    onHome = { navController.switchTo(Screen.Home.route) },
                    onSaving = { navController.switchTo(Screen.Saving.route) },
                    onSetting = { navController.switchTo(Screen.Setting.route) },
                    onCamera = { navController.navigate(Screen.Scan.route) }
                )
            }

            composable(Screen.SavingAdd.route) {
                AddSavingGoalScreen(
                    onBack = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    onCreate = { success ->
                        if (success) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("should_refresh_savings", true)
                        }
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Saving.route,
                enterTransition = { null },
                exitTransition = { null }
            ) { backStackEntry ->
                SavingsScreen(
                    onHome = { navController.switchTo(Screen.Home.route) },
                    onReport = { navController.switchTo(Screen.Report.route) },
                    onSettings = { navController.switchTo(Screen.Setting.route) },
                    onCamera = { navController.navigate(Screen.Scan.route) },
                    onSaving = {  },
                    onAddGoal = { navController.navigate(Screen.SavingAdd.route) },
                    onGoalClick = { goalId ->
                        val route = Screen.SavingDetail.create(goalId)
                        Log.d("Navigation", "Navigating to: $route with goalId: $goalId")
                        navController.navigate(route)
                    },
                    backStackEntry = backStackEntry
                )
            }

            composable(
                route = Screen.SavingDetail.route,
                arguments = listOf(navArgument(Screen.SavingDetail.ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getString(Screen.SavingDetail.ARG)

                SavingDetailScreen(
                    goalId = goalId,
                    onNavigateBack = { refreshNeeded ->
                        if (refreshNeeded) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("should_refresh_savings", true)
                            Log.d("Navigation", "Set refresh signal for SavingsScreen on update.")
                        }
                        navController.popBackStack()
                    },
                    onDeleteSuccess = {

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("should_refresh_savings", true)
                        Log.d("Navigation", "Set refresh signal after delete for SavingsScreen.")
                        navController.popBackStack()
                    },
                )
            }
            composable(
                route = Screen.Setting.route,
                enterTransition = { null },
                exitTransition = { null }
            ) {
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val authRepo = remember { com.example.test.data.AuthRepository(ctx) }

                SettingScreen(
                    dark = dark,
                    onToggleDark = settingsVm::setDarkMode,
                    soundEnabled = soundEnabled,
                    onToggleSound = settingsVm::setSoundEnabled,
                    onHome = { navController.switchTo(Screen.Home.route) },
                    onReport = { navController.switchTo(Screen.Report.route) },
                    onSaving = { navController.switchTo(Screen.Saving.route) },
                    onPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                    onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                    onLogout = {
                        scope.launch {
                            AuthStore.clear()
                            authRepo.clearUserInfo()

                            navController.navigate("auth") {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    onSetting = { },
                    onCamera = { navController.navigate(Screen.Scan.route) }
                )
            }

            composable(Screen.PersonalInfo.route) {
                PersonalInfoScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.ChangePassword.route) {
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val usersApi = remember { Api.usersService }

                ChangePasswordScreen(
                    onBack = { navController.popBackStack() },
                    onChangePassword = { oldPassword, newPassword, onError ->
                        scope.launch {
                            val userId = AuthStore.userId
                            if (userId == null) {
                                onError("❌ Không tìm thấy thông tin người dùng")
                                return@launch
                            }
                            try {
                                val request = com.example.test.ui.models.ChangePasswordRequest(
                                    userId = userId,
                                    oldPassword = oldPassword,
                                    newPassword = newPassword,
                                    confirmPassword = newPassword
                                )
                                val response = usersApi.changePassword(request)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(ctx, "✅ Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    val msg = response.body()?.message ?: "Đổi mật khẩu thất bại"
                                    onError("❌ $msg")
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (_: java.net.UnknownHostException) {
                                onError("❌ Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng")
                            } catch (_: java.net.SocketTimeoutException) {
                                onError("❌ Hết thời gian chờ. Vui lòng thử lại")
                            } catch (_: java.net.ConnectException) {
                                onError("❌ Không thể kết nối đến máy chủ")
                            } catch (_: java.io.IOException) {
                                onError("❌ Lỗi kết nối. Vui lòng thử lại")
                            } catch (_: Exception) {
                                onError("❌ Đổi mật khẩu thất bại. Vui lòng thử lại")
                            }
                        }
                    }
                )
            }

            composable(Screen.ProfilePicture.route) {
                ProfilePictureScreen(
                    onSave = { _, _, _, -> },
                    onBack = { navController.switchTo(Screen.Setting.route) },
                )
            }

            composable(Screen.CurrentUnit.route) {
                CurrencyUnitScreen(
                    onBack = { navController.switchTo(Screen.Setting.route) },
                    onSelect = { item ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("currency_code", item.code)
                    }
                )
            }

            composable(Screen.ScanResult.route) {
                ScanResultScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Scan.route) {
                val imageApi = remember { Api.imageService }
                val nav = navController
                val ctx = LocalContext.current

                ReceiptScanScreen(
                    imageApi = imageApi,
                    userId = null,
                    categoryId = null,
                    onBack = { nav.popBackStack() },

                    onUploaded = { body ->
                        val json = Gson().toJson(body)
                        nav.popBackStack()
                        nav.navigate(Screen.ScanResult.route)
                        nav.currentBackStackEntry?.savedStateHandle?.apply {
                            set("upload_result", UploadResult(true, null, body.message ?: "OK"))
                            set("scan_payload", json)
                        }
                    },

                    onUploadError = { raw ->
                        val msg = extractServerMessage(raw) ?: raw
                        Toast.makeText(ctx, msg.ifBlank { "Upload thất bại" }, Toast.LENGTH_LONG).show()
                        nav.popBackStack()
                        nav.navigate(Screen.ScanResult.route)
                        nav.currentBackStackEntry?.savedStateHandle
                            ?.set("upload_result", UploadResult(false, null, msg))
                    },
                    showScanTips = true,
                    showPermissionTexts = false
                )
            }
        }
    }
}
private fun parseAmountVnd(text: String): Long =
    text.filter { it.isDigit() }.toLongOrNull() ?: 0L

@RequiresApi(Build.VERSION_CODES.O)
private fun com.example.test.ui.mock.TransactionMock.toTxUi(id: Int): TxUi =
    TxUi(
        id = id.toString(),
        title = title,
        category = subtitle.substringBefore(" • "),
        dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        amount = amount,
        type = if (isPositive) TxType.INCOME else TxType.EXPENSE,
        emoji = icon
    )

private fun Uri.toImagePart(
    ctx: Context,
    fieldName: String,
    fileName: String
): MultipartBody.Part {
    val bytes = ctx.contentResolver.openInputStream(this)!!.use { it.readBytes() }
    val mime = ctx.contentResolver.getType(this) ?: "image/jpeg"
    val body = bytes.toRequestBody(mime.toMediaType())
    return MultipartBody.Part.createFormData(fieldName, fileName, body)
}
