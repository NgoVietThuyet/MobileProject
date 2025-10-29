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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
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
import com.example.test.vm.AllTransactionViewModel

private fun extractServerMessage(raw: String): String? = try {
    val obj = JSONObject(raw)
    obj.optString("message").takeIf { it.isNotBlank() }
        ?: obj.optString("detail").takeIf { it.isNotBlank() }
        ?: obj.optString("title").takeIf { it.isNotBlank() }
        ?: run {
            if (obj.has("errors")) {
                val errs = obj.get("errors")
                when (errs) {
                    is JSONArray -> (0 until errs.length()).joinToString("; ") { errs.getString(it) }
                    is JSONObject -> errs.keys().asSequence().joinToString("; ") { key ->
                        val arr = errs.optJSONArray(key)
                        if (arr != null) "$key: " + (0 until arr.length()).joinToString(", ") { arr.getString(it) }
                        else "$key: ${errs.optString(key)}"
                    }
                    else -> errs.toString()
                }
            } else null
        }
} catch (_: Exception) {
    raw.ifBlank { null }
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

    NavHost(navController = navController, startDestination = "auth") {
        navigation(startDestination = Screen.Login.route, route = "auth") {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToEmail = { navController.navigate(Screen.EmailLogin.route) },
                    onNavigateToPhone = { navController.navigate(Screen.PhoneLogin.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onFb = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.EmailLogin.route) {
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val usersApi = remember { Api.usersService }

                EmailLoginScreen(
                    onBack = { navController.popBackStack() },
                    onLogin = { email, password ->
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


                                    navController.navigate("main") {
                                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    val msg = res.errorBody()?.string()?.let(::extractServerMessage)
                                        ?: "Đăng nhập thất bại"
                                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                val isConnErr = e is java.net.UnknownHostException ||
                                        e is java.net.ConnectException || e is java.net.SocketTimeoutException
                                val msg = if (isConnErr) "Mất kết nối máy chủ" else (e.localizedMessage ?: "Đăng nhập thất bại")
                                Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.PhoneLogin.route) {
                PhoneLoginScreen(
                    onBack = { navController.popBackStack() },
                    onRequestOtp = { phone ->
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
                OtpVerificationScreen(
                    phoneNumber = phone,
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
                    onRegister = { fullName, email, phone, password ->
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
                                        )
                                    )
                                )

                                if (res.isSuccessful) {
                                    Toast.makeText(ctx, "Đăng ký thành công", Toast.LENGTH_LONG).show()
                                    navController.popBackStack(Screen.Login.route, false)
                                } else {
                                    val code = res.code()
                                    val serverMsg = res.errorBody()?.string()?.let(::extractServerMessage)
                                    val msg = serverMsg ?: when (code) {
                                        400 -> "Thông tin không hợp lệ. Kiểm tra họ tên, email, số điện thoại, mật khẩu."
                                        401 -> "Bạn chưa được xác thực."
                                        403 -> "Bạn không có quyền thực hiện thao tác này."
                                        404 -> "Không tìm thấy endpoint đăng ký."
                                        409 -> "Email hoặc số điện thoại đã tồn tại."
                                        422 -> "Dữ liệu không hợp lệ."
                                        429 -> "Quá nhiều yêu cầu. Thử lại sau."
                                        in 500..599 -> "Máy chủ lỗi ($code). Thử lại sau."
                                        else -> "Đăng ký thất bại ($code)."
                                    }
                                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (_: java.net.UnknownHostException) {
                                Toast.makeText(ctx, "Không có internet hoặc tên miền máy chủ không hợp lệ", Toast.LENGTH_LONG).show()
                            } catch (_: java.net.SocketTimeoutException) {
                                Toast.makeText(ctx, "Hết thời gian chờ. Kiểm tra kết nối mạng", Toast.LENGTH_LONG).show()
                            } catch (_: java.net.ConnectException) {
                                Toast.makeText(ctx, "Không thể kết nối máy chủ", Toast.LENGTH_LONG).show()
                            } catch (_: java.io.IOException) {
                                Toast.makeText(ctx, "Lỗi mạng. Vui lòng thử lại", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "Lỗi không xác định: ${e.localizedMessage ?: e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
        }

        navigation(startDestination = Screen.Home.route, route = "main") {

            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenBudgetAll = { navController.navigate(Screen.BudgetAll.route) },
                    onAddIncome = { navController.navigate(Screen.IncomeCreate.route) },
                    onAddExpense = { navController.navigate(Screen.ExpenseCreate.route) },
                    onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                    onOpenAllTransactions = { navController.navigate(Screen.TransactionsAll.route) },
                    onReport = { navController.navigate(Screen.Report.route) },
                    onSaving = { navController.navigate(Screen.Saving.route) },
                    onSetting = { navController.navigate(Screen.Setting.route) },
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

            composable(Screen.Report.route) {
                ReportScreen(
                    onHome = { navController.navigate(Screen.Home.route) },
                    onSaving = { navController.navigate(Screen.Saving.route) },
                    onSetting = { navController.navigate(Screen.Setting.route) },
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

            composable(Screen.Saving.route) { backStackEntry ->
                SavingsScreen(
                    onHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                    onReport = { navController.navigate(Screen.Report.route) { launchSingleTop = true } },
                    onSettings = { navController.navigate(Screen.Setting.route) { launchSingleTop = true } },
                    onCamera = { navController.navigate(Screen.Scan.route) { launchSingleTop = true } },
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
            composable(Screen.Setting.route) {
                SettingScreen(
                    dark = dark,
                    onToggleDark = settingsVm::setDarkMode,
                    onHome = { navController.navigate(Screen.Home.route) },
                    onReport = { navController.navigate(Screen.Report.route) },
                    onSaving = { navController.navigate(Screen.Saving.route) },
                    onPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                    onProfilePicture = { navController.navigate(Screen.ProfilePicture.route) },
                    onLanguages = { },
                    onCurrency = { navController.navigate(Screen.CurrentUnit.route) },
                    onLogout = {
                        AuthStore.clear()
                        navController.navigate("auth") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onSetting = { },
                    onCamera = { navController.navigate(Screen.Scan.route) }
                )
            }

            composable(Screen.PersonalInfo.route) {
                PersonalInfoScreen(
                    onBack = { navController.switchTo(Screen.Setting.route) },
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
