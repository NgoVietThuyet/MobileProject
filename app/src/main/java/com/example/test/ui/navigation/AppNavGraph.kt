package com.example.test.ui.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.test.ui.screens.*

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
                EmailLoginScreen(
                    onBack = { navController.popBackStack() },
                    onLogin = {
                        navController.navigate("main") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                            restoreState = true
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
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onLoginNow = { navController.popBackStack(Screen.Login.route, false) }
                )
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

