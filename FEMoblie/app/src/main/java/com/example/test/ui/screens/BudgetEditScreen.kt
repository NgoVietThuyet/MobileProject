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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.mock.MockData
import com.example.test.ui.theme.AppGradient
import com.example.test.ui.util.parseTotalMFromMock
import com.example.test.ui.util.parseUsedMFromMock
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetEditScreen(index: Int, onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    val item = MockData.budgetCategories.getOrNull(index)
    LaunchedEffect(item) { if (item == null) onBack() }

    val usedVnd: Long = item?.let { (parseUsedMFromMock(it.amount) * 1_000_000).roundToLong() } ?: 0L
    val totalVndInitial: Long? =
        MockData.getBudgetTotalVnd(index) ?: item?.let {
            val usedM = parseUsedMFromMock(it.amount)
            val totalM = parseTotalMFromMock(it.amount) ?: if (it.progress > 0f && usedM > 0.0) usedM / it.progress else null
            totalM?.let { m -> (m * 1_000_000).roundToLong() }
        }

    var totalVndRaw by remember { mutableStateOf(totalVndInitial?.toString() ?: "") }
    fun sanitizeDigits(s: String) = s.filter { it.isDigit() }
    val totalVndLong: Long? = totalVndRaw.toLongOrNull()
    val isUpdateEnabled = totalVndLong != null
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Chỉnh sửa ngân sách",
                showBack = true,
                onBack = onBack
            )
        },
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

            // Header card
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
                            .background(cs.surface.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = cat.color,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                        Text(cat.icon)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(cat.title, color = cs.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }

                            val displayTotalVnd = totalVndInitial ?: 0L
                            Text(
                                text = "${usedVnd} / ${displayTotalVnd}",
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((item.progress).coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(cat.color, RoundedCornerShape(999.dp))
                                )
                            }
                        }
                    }
                } ?: Text("Không tìm thấy danh mục", color = cs.onPrimary, modifier = Modifier.align(Alignment.CenterStart))
            }

            Spacer(Modifier.height(16.dp))

            // Edit card
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
                        isError = totalVndRaw.isNotEmpty() && !isUpdateEnabled,
                        supportingText = {
                            if (totalVndRaw.isNotEmpty() && !isUpdateEnabled) Text("Giá trị không hợp lệ")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val vnd = totalVndLong ?: 0L
                            val newTotalM = vnd / 1_000_000.0
                            MockData.updateBudgetTotalM(index, newTotalM)
                            scope.launch { snackbarHostState.showSnackbar("Đã cập nhật ngân sách") }
                            onBack()
                        },
                        enabled = isUpdateEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
                    ) {
                        Icon(painter = painterResource(R.drawable.increase), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cập nhật")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
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
                                MockData.deleteBudget(index)
                                showDeleteConfirm = false
                                scope.launch { snackbarHostState.showSnackbar("Đã xoá ngân sách") }
                                onBack()
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
