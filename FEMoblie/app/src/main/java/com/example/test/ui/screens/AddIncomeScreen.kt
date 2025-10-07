@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class IncomeInput(
    val amountVnd: Long,
    val category: String,
    val note: String,
    val dateMillis: Long
)

private data class IncomeCategory(
    val emoji: String,
    val label: String
)

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit,
    onSave: (IncomeInput) -> Unit
) {
    // State
    var amountRaw by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<IncomeCategory?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    val today = LocalDate.now()
    var date by rememberSaveable { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = remember {
        listOf(
            IncomeCategory("💰", "Lương"),
            IncomeCategory("🎁", "Thưởng"),
            IncomeCategory("📈", "Đầu tư"),
            IncomeCategory("💻", "Việc tự do"),
            IncomeCategory("🏪", "Bán hàng"),
            IncomeCategory("💵", "Thu nhập khác")
        )
    }

    fun digitsOnly(s: String) = s.filter { it.isDigit() }
    val amountLong = amountRaw.toLongOrNull() ?: 0L
    val isValid = amountLong > 0 && selectedCategory != null

    val vi = Locale("vi", "VN")
    val sym = DecimalFormatSymbols(vi).apply { groupingSeparator = '.'; decimalSeparator = ',' }
    val df = remember { DecimalFormat("#,##0", sym) }
    val moneyPreview = if (amountLong > 0) df.format(amountLong) + " ₫" else ""

    val dateLabel = remember(date) { date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = 36.dp

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFFD9D9D9).copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().height(headerHeight)
            ) {}
        },
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Surface(color = Color(0xFFF6F6F7)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            onSave
                        },
                        enabled = isValid,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34C759),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF34C759).copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Text("Lưu", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

    ) {
        Column(
            modifier = Modifier
                .padding(top = statusTop + headerHeight)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_back),
                        contentDescription = "Quay lại",
                        tint = Color.Black
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text("Thêm khoản thu", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE3E3E7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Số tiền", color = Color(0xFF7B8090), fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountRaw,
                        onValueChange = { amountRaw = digitsOnly(it) },
                        placeholder = { Text("Nhập số tiền…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFD7D7DB), RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB)),
                        trailingIcon = {
                            Text(
                                "đ",
                                color = Color(0xFF9CA3AF),
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF111827)
                        )
                    )

                    if (moneyPreview.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("≈ $moneyPreview", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Chọn danh mục", fontSize = 14.sp, color = Color(0xFF5F6167))
            Spacer(Modifier.height(10.dp))
            CategoryGrid(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Spacer(Modifier.height(20.dp))

            // Ghi chú
            Text("Ghi chú", fontSize = 14.sp, color = Color(0xFF5F6167))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                placeholder = { Text("Nhập ghi chú…") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F2F4),
                    focusedContainerColor = Color(0xFFF1F2F4),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF4C6FFF)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Ngày giao dịch
            Text("Ngày giao dịch:", fontSize = 14.sp, color = Color(0xFF5F6167))
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFD7D7DB), RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 12.dp)
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateLabel, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color(0xFF111827))
                Icon(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showDatePicker) {
        val initial = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("Chọn") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") } }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = initial)
            DatePicker(state = state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let {
                    date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
            }
        }
    }
}
@Composable
private fun CategoryGrid(
    categories: List<IncomeCategory>,
    selected: IncomeCategory?,
    onSelect: (IncomeCategory) -> Unit
) {
    val rows = categories.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { cat ->
                    val isSelected = selected == cat
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF4C6FFF) else Color(0xFFE3E3E7)),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clickable { onSelect(cat) }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFEEF2FF) else Color(0xFFF5F7FB)),
                                    contentAlignment = Alignment.Center
                                ) { Text(cat.emoji, fontSize = 18.sp, textAlign = TextAlign.Center) }
                                Spacer(Modifier.height(6.dp))
                                Text(cat.label, fontSize = 13.sp, color = Color(0xFF111827))
                            }
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f).height(80.dp)) }
            }
        }
    }
}
