package com.example.test.ui.navigation

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.test.ui.api.LoginReq
import com.example.test.ui.api.SignUpRequest
import com.example.test.ui.api.UserDto
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.screens.*
import kotlinx.coroutines.launch
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import com.example.test.ui.mock.MockData as HomeMock

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
    data object BudgetCreate : Screen("budget_create")
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
        fun create(index: Int) = "saving_detail/$index"
    }
    data object SavingAdd : Screen("saving_add")
    data object Setting : Screen("setting")

    data object PersonalInfo : Screen("personal_info")

    data object ProfilePicture : Screen("profile_picture")

    data object CurrentUnit : Screen("current_unit")

    data object Scan : Screen("scan")
}

private data class TxNav(
    val id: String,
    val title: String,
    val category: String,
    val amount: Long,
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

private fun NavController.switchTo(route: String) {
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
    NavHost(navController = navController, startDestination = "auth") {
        navigation(startDestination = Screen.Login.route, route = "auth") {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToEmail = { navController.navigate(Screen.EmailLogin.route) },
                    onNavigateToPhone = { navController.navigate(Screen.PhoneLogin.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onFb = {navController.navigate(Screen.Home.route)}
                )
            }

            composable(Screen.EmailLogin.route) {
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val api = remember { Api.service }

                EmailLoginScreen(
                    onBack = { navController.popBackStack() },
                    onLogin = { email, password ->
                        scope.launch {
                            try {
                                val res = api.login(LoginReq(email, password))
                                val body = res.body()
                                if (res.isSuccessful && body != null) {
                                    AuthStore.token = body.token
                                    navController.navigate("main") {
                                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    Toast.makeText(ctx, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                val isConnErr = e is java.net.UnknownHostException ||
                                        e is java.net.ConnectException || e is java.net.SocketTimeoutException
                                val msg = if (isConnErr) "Mất kết nối máy chủ" else "Đăng nhập thất bại"
                                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
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
                val api = remember { Api.service }
                val snackbar = remember { SnackbarHostState() }

                Scaffold(snackbarHost = { SnackbarHost(hostState = snackbar) }) {
                    RegisterScreen(
                        onBack = { navController.popBackStack() },
                        onRegister = { fullName, email, phone, password ->
                            val now = Instant.now().toString()
                            val id = UUID.randomUUID().toString()
                            scope.launch {
                                try {
                                    val res = api.register(
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
                                        Toast.makeText(ctx, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack(Screen.Login.route, false)
                                    } else {
                                        snackbar.showSnackbar("Mất kết nối máy chủ")
                                    }
                                } catch (e: kotlinx.coroutines.CancellationException) {
                                    throw e
                                } catch (_: Exception) {
                                    snackbar.showSnackbar("Mất kết nối máy chủ")
                                }
                            }
                        }
                    )
                }
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
                BudgetAllScreen(
                    onBack = { navController.popBackStack() },
                    onOpenEdit = { idx -> navController.navigate(Screen.BudgetEdit.create(idx)) },
                    onAdd = { navController.navigate(Screen.BudgetCreate.route) }
                )
            }

            composable(
                route = Screen.BudgetEdit.route,
                arguments = listOf(navArgument(Screen.BudgetEdit.ARG) { type = NavType.IntType })
            ) { backStack ->
                val idx = backStack.arguments!!.getInt(Screen.BudgetEdit.ARG)
                BudgetEditScreen(index = idx, onBack = { navController.popBackStack() })
            }

            composable(Screen.BudgetCreate.route) {
                AddBudgetScreen(
                    onBack = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    onCreate = { _, _, _, _ -> navController.popBackStack() }
                )
            }

            composable(Screen.IncomeCreate.route) {
                AddIncomeScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { _ -> navController.popBackStack() }
                )
            }

            composable(Screen.ExpenseCreate.route) {
                AddExpenseScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { _ -> navController.popBackStack() }
                )
            }

            composable(Screen.TransactionsAll.route) {
                val list = remember {
                    HomeMock.recentTransactions.sortedByDescending { it.createdAt }
                        .mapIndexed { i, m -> m.toTxUi(i) }
                }
                AllTransactionsScreen(
                    transactions = list,
                    onBack = { navController.popBackStack() },
                    onEditIncome = { tx -> navController.navigate(Screen.EditIncome.create(tx)) },
                    onEditExpense = { tx -> navController.navigate(Screen.EditExpense.create(tx)) }
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
                    onSave = { _ -> navController.popBackStack() },
                    onDelete = { _ -> navController.popBackStack() }
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
                    onSave = { _ -> navController.popBackStack() },
                    onDelete = { _ -> navController.popBackStack() }
                )
            }

            composable(Screen.Report.route) {
                ReportScreen(
                    onHome = { navController.navigate(Screen.Home.route) },
                    onSaving = { navController.navigate(Screen.Saving.route) },
                    onSetting = { navController.navigate(Screen.Setting.route) },
                    onCamera = {navController.navigate(Screen.Scan.route)}
                )
            }

            composable(Screen.Saving.route) {
                SavingsScreen(
                    onHome = { navController.navigate(Screen.Home.route) },
                    onReport = { navController.navigate(Screen.Report.route) },
                    onSettings = { navController.navigate(Screen.Setting.route) },
                    onCamera = { navController.navigate(Screen.Scan.route) },
                    onSaving = { },
                    onAddGoal = { navController.navigate(Screen.SavingAdd.route) },
                    onGoalClick = { index -> navController.navigate(Screen.SavingDetail.create(index)) }
                )
            }

            composable(Screen.SavingAdd.route) {
                AddSavingGoalScreen(
                    onBack = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    onCreate = { _ -> navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SavingDetail.route,
                arguments = listOf(navArgument(Screen.SavingDetail.ARG) { type = NavType.IntType })
            ) { backStack ->
                val idx = backStack.arguments!!.getInt(Screen.SavingDetail.ARG)
                val goal = HomeMock.savingGoals.getOrNull(idx) ?: HomeMock.savingGoals.first()
                SavingDetailScreen(
                    goal = goal,
                    onBack = { navController.popBackStack() },
                    onDelete = { navController.popBackStack() }
                )
            }

            composable(Screen.Setting.route) {
                SettingScreen(
                    onHome = { navController.navigate(Screen.Home.route) },
                    onReport = { navController.navigate(Screen.Report.route) },
                    onSaving = { navController.navigate(Screen.Saving.route) },
                    onPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
                    onProfilePicture = { navController.navigate(Screen.ProfilePicture.route) },
                    onLanguages = { navController.navigate(Screen.CurrentUnit.route) },
                    onLogout = { /* TODO logout */ },
                    onSetting = { },
                    onCamera = {navController.navigate(Screen.Scan.route)}
                )
            }

            composable(Screen.PersonalInfo.route) {
                PersonalInfoScreen(
                    onBack = {navController.switchTo(Screen.Setting.route)},
                    onSave = {},
                )
            }

            composable(Screen.ProfilePicture.route) {
                ProfilePictureScreen(
                    onSave = {_,_,_,->},
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

            composable ( Screen.Scan.route ) {
                ReceiptScanScreen (
                    onBack = {navController.popBackStack()},
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
        amount = parseAmountVnd(amount),
        type = if (isPositive) TxType.INCOME else TxType.EXPENSE,
        emoji = icon
    )
