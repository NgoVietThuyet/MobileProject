package com.example.test.ui.navigation

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
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
import com.example.test.ui.api.SignUpResponse
import com.example.test.ui.api.UserDto
import com.example.test.ui.screens.*
import kotlinx.coroutines.launch
import com.google.gson.Gson
import java.time.Instant
import java.util.UUID
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object EmailLogin : Screen("email_login")
    data object PhoneLogin : Screen("phone_login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object BudgetAll : Screen("budget_all")

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
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "auth",
    ) {
        // ============== AUTH GRAPH ==============
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
                                        e is java.net.ConnectException ||
                                        e is java.net.SocketTimeoutException
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
                exitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
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
                            val id  = UUID.randomUUID().toString()

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

        // ============== MAIN GRAPH ==============
        navigation(startDestination = Screen.Home.route, route = "main") {

            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenBudgetAll = { navController.navigate(Screen.BudgetAll.route) },
                    onAddIncome = { navController.navigate(Screen.IncomeCreate.route) },
                    onAddExpense = { navController.navigate(Screen.ExpenseCreate.route) }
                )
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
                BudgetEditScreen(
                    index = idx,
                    onBack = { navController.popBackStack() }
                )
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
                    onSave = { input ->
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ExpenseCreate.route) {
                AddExpenseScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { input ->
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
