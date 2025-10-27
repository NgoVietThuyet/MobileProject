@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.data.LocalCategoryDataSource
import com.example.test.ui.api.Api
import com.example.test.ui.api.AuthStore
import com.example.test.ui.components.AppHeader
import com.example.test.ui.models.BudgetDto
import com.example.test.ui.models.UpdateBudgetAmountReq
import com.example.test.ui.theme.AppGradient
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetEditScreen(index: Int, onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    val userId = remember { AuthStore.userId }
    val budgetApi = remember { Api.budgetService }

    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    var budget: BudgetDto? by remember { mutableStateOf(null) }
    var budgetId by remember { mutableStateOf<String?>(null) }
    var usedVnd by remember { mutableStateOf(0L) }
    var totalVndInitial by remember { mutableStateOf<Long?>(null) }

    var totalVndRaw by remember { mutableStateOf("") }
    fun sanitizeDigits(s: String) = s.filter { it.isDigit() }
    val totalVndLong: Long? = totalVndRaw.toLongOrNull()
    val isUpdateEnabled = !isSubmitting && totalVndLong != null && budgetId != null

    var showDeleteConfirm by remember { mutableStateOf(false) }

    var categoryName by remember { mutableStateOf<String?>(null) }
    var categoryIcon by remember { mutableStateOf<String?>(null) }
    var categoryColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(index, userId) {
        if (userId.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Vui lòng đăng nhập") }
            onBack(); return@LaunchedEffect
        }
        isLoading = true
        try {
            val res = budgetApi.getAllBudgets(userId)
            if (!res.isSuccessful) {
                scope.launch { snackbarHostState.showSnackbar("Tải ngân sách lỗi: HTTP ${res.code()}") }
                onBack(); return@LaunchedEffect
            }
            val list = res.body().orEmpty()
            val item = list.getOrNull(index)
            if (item == null) {
                scope.launch { snackbarHostState.showSnackbar("Không tìm thấy ngân sách") }
                onBack(); return@LaunchedEffect
            }

            budget = item
            budgetId = item.budgetId
            usedVnd = item.currentAmount?.onlyDigits()?.toLongOrNull() ?: 0L
            val initialVnd = item.initialAmount?.onlyDigits()?.toLongOrNull()
            totalVndInitial = initialVnd
            totalVndRaw = initialVnd?.toString() ?: ""

            val cat = LocalCategoryDataSource.find(item.categoryId)
            categoryName = cat?.name ?: "Danh mục ${item.categoryId.take(6)}"
            categoryIcon = cat?.icon ?: pickEmoji(abs(item.categoryId.hashCode()))
            categoryColor = pickColor(abs(item.categoryId.hashCode()))

        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar(e.message ?: "Lỗi tải ngân sách") }
            onBack()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "Chỉnh sửa ngân sách", showBack = true, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
        containerColor = cs.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cs.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(brush = AppGradient.BlueGreen, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                        .background(cs.surface.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = categoryColor ?: cs.secondary, shape = RoundedCornerShape(8.dp)) {
                                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                    Text(categoryIcon ?: "💰")
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = categoryName ?: "Danh mục",
                                color = cs.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        val displayTotalVnd = totalVndInitial ?: 0L
                        Text(
                            text = "${usedVnd} / ${displayTotalVnd} ₫",
                            color = cs.onPrimary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(cs.onPrimary.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                        ) {
                            val progress = if (displayTotalVnd > 0)
                                (usedVnd.toFloat() / displayTotalVnd).coerceIn(0f, 1f)
                            else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(categoryColor ?: cs.primary, RoundedCornerShape(999.dp))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                border = BorderStroke(1.dp, cs.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Cập nhật tổng ngân sách (đơn vị: VND)", fontWeight = FontWeight.Medium, color = cs.onSurface)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = totalVndRaw,
                        onValueChange = { totalVndRaw = sanitizeDigits(it) },
                        label = { Text("Tổng ngân sách (₫)") },
                        placeholder = { Text("VD: 3000000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = totalVndRaw.isNotEmpty() && totalVndLong == null,
                        supportingText = {
                            if (totalVndRaw.isNotEmpty() && totalVndLong == null) Text("Giá trị không hợp lệ")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val id = budgetId
                            val vnd = totalVndLong
                            if (id == null || vnd == null) return@Button
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val req = UpdateBudgetAmountReq(
                                        budgetId = id,
                                        updateAmount = vnd.toString(),
                                        isAddAmount = false
                                    )
                                    val res = budgetApi.updateBudget(req)
                                    if (res.isSuccessful) {
                                        snackbar(scope, snackbarHostState, "Đã cập nhật ngân sách")
                                        onBack()
                                    } else {
                                        val msg = res.errorBody()?.string().orEmpty()
                                        snackbar(scope, snackbarHostState, msg.ifBlank { "Cập nhật thất bại (${res.code()})" })
                                    }
                                } catch (e: Exception) {
                                    snackbar(scope, snackbarHostState, e.message ?: "Lỗi cập nhật")
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = isUpdateEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(painter = painterResource(R.drawable.increase), contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Cập nhật")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && budgetId != null,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error),
                border = BorderStroke(1.dp, cs.error)
            ) {
                Icon(painter = painterResource(R.drawable.ic_trash), contentDescription = "Xoá ngân sách")
                Spacer(Modifier.width(8.dp))
                Text("Xoá ngân sách")
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Xoá ngân sách?") },
                    text = { Text("Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = budgetId ?: return@TextButton
                                isSubmitting = true
                                showDeleteConfirm = false
                                scope.launch {
                                    try {
                                        val res = budgetApi.deleteBudgetById(id)
                                        if (res.isSuccessful) {
                                            snackbar(scope, snackbarHostState, "Đã xoá ngân sách")
                                            onBack()
                                        } else {
                                            val msg = res.errorBody()?.string().orEmpty()
                                            snackbar(scope, snackbarHostState, msg.ifBlank { "Xoá thất bại (${res.code()})" })
                                        }
                                    } catch (e: Exception) {
                                        snackbar(scope, snackbarHostState, e.message ?: "Lỗi xoá ngân sách")
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = cs.error)
                        ) { Text("Xoá") }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Huỷ") } }
                )
            }
        }
    }
}

private fun String.onlyDigits(): String = this.filter { it.isDigit() }

private fun snackbar(scope: kotlinx.coroutines.CoroutineScope, host: SnackbarHostState, msg: String) {
    scope.launch { host.showSnackbar(msg) }
}

private fun pickEmoji(i: Int): String {
    val em = listOf("🍔", "🚗", "🏠", "🎓", "🛒", "💡", "🏥", "🎉", "🧾", "🛠")
    return em[i % em.size]
}

private fun pickColor(i: Int): Color {
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3),
        Color(0xFFFF9800), Color(0xFFE91E63),
        Color(0xFF9C27B0)
    )
    return colors[i % colors.size]
}
