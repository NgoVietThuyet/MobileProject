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
import com.example.test.ui.mock.MockData
import com.example.test.ui.theme.AppGradient
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToLong
import com.example.test.ui.util.parseUsedMFromMock
import com.example.test.ui.util.parseTotalMFromMock


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetEditScreen(index: Int, onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val item = MockData.budgetCategories.getOrNull(index)
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = 36.dp

    LaunchedEffect(item) { if (item == null) onBack() }

    fun parseUsedM(amount: String): Double {
        val first = amount.split('/').firstOrNull().orEmpty()
        val cleaned = first.lowercase().replace(",", ".").replace("[^0-9\\.]".toRegex(), "")
        return cleaned.toDoubleOrNull() ?: 0.0
    }
    fun parseTotalM(amount: String): Double? {
        val second = amount.split('/').getOrNull(1)?.trim().orEmpty()
        if (second.isEmpty()) return null
        val cleaned = second.lowercase().replace(",", ".").replace("[^0-9\\.]".toRegex(), "")
        return cleaned.toDoubleOrNull()
    }

    val usedVnd: Long = item?.let { (parseUsedMFromMock(it.amount) * 1_000_000).roundToLong() } ?: 0L

    val totalVndInitial: Long? =
        MockData.getBudgetTotalVnd(index) ?: item?.let {
            val usedM  = parseUsedMFromMock(it.amount)
            val totalM = parseTotalMFromMock(it.amount)
                ?: if (it.progress > 0f && usedM > 0.0) usedM / it.progress else null
            totalM?.let { m -> (m * 1_000_000).roundToLong() }
        }

    var totalVndRaw by remember {
        mutableStateOf(totalVndInitial?.toString() ?: "")
    }

    fun sanitizeDigits(s: String) = s.filter { it.isDigit() }

    val totalVndLong: Long? = totalVndRaw.toLongOrNull()
    val isUpdateEnabled = totalVndLong != null

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFFD9D9D9).copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().height(headerHeight)
            ) {}
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusTop + headerHeight)
                .padding(horizontal = 20.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Quay lại", tint = Color.Black)
                }
                Spacer(Modifier.width(6.dp))
                Text("Chỉnh sửa ngân sách", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(brush = AppGradient.BlueGreen, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                item?.let { cat ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(cat.color, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Text(cat.icon) }
                                Spacer(Modifier.width(12.dp))
                                Text(cat.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }

                            val displayTotalVnd = totalVndInitial ?: 0L
                            Text(
                                text = "${usedVnd} / ${displayTotalVnd}",
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                                    .background(Color.White.copy(0.35f), RoundedCornerShape(999.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(cat.progress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(cat.color, RoundedCornerShape(999.dp))
                                )
                            }
                        }
                    }
                } ?: Text("Không tìm thấy danh mục", color = Color.White, modifier = Modifier.align(Alignment.CenterStart))
            }

            Spacer(Modifier.height(16.dp))

            // Card nhập số tiền
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Cập nhật tổng ngân sách (đơn vị: VND)", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = totalVndRaw,
                        onValueChange = { totalVndRaw = sanitizeDigits(it) },
                        label = { Text("Tổng ngân sách (₫)") },
                        placeholder = { Text("VD: 3000000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = totalVndRaw.isNotEmpty() && !isUpdateEnabled,
                        supportingText = {
                            if (totalVndRaw.isNotEmpty() && !isUpdateEnabled) Text("Giá trị không hợp lệ")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val vnd = totalVndLong ?: 0L
                            val newTotalM = vnd / 1_000_000.0 // convert VND -> M
                            MockData.updateBudgetTotalM(index, newTotalM)
                            scope.launch { snackbarHostState.showSnackbar("Đã cập nhật ngân sách") }
                            onBack()
                        },
                        enabled = isUpdateEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF268233))
                    ) {
                        Icon(painter = painterResource(R.drawable.increase), contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Cập nhật")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD80000)),
                border = BorderStroke(1.dp, Color(0xFFD80000))
            ) {
                Icon(painter = painterResource(R.drawable.ic_trash), contentDescription = "Xoá ngân sách", tint = Color(0xFFD80000))
                Spacer(Modifier.width(8.dp))
                Text("Xoá ngân sách")
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Xoá ngân sách?") },
                    text = { Text("Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        TextButton(onClick = {
                            MockData.deleteBudget(index)
                            showDeleteConfirm = false
                            scope.launch { snackbarHostState.showSnackbar("Đã xoá ngân sách") }
                            onBack()
                        }) { Text("Xoá", color = Color(0xFFD80000)) }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Huỷ") } }
                )
            }
        }
    }
}
