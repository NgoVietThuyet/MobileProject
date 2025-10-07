@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.mock.TxUi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditIncomeScreen(
    tx: TxUi,
    onBack: () -> Unit = {},
    onSave: (TxUi) -> Unit,
    onDelete: (TxUi) -> Unit
) {
    EditIncomeContent(tx, onBack, onSave, onDelete)
}
private data class IncCategoryOption(val key: String, val label: String, val emoji: String)

private val incomeOptions = listOf(
    IncCategoryOption("salary", "Lương", "💼"),
    IncCategoryOption("bonus", "Thưởng", "🎁"),
    IncCategoryOption("invest", "Đầu tư", "📈"),
    IncCategoryOption("freelance", "Việc tự do", "🧑‍💻"),
    IncCategoryOption("sales", "Bán hàng", "🛒"),
    IncCategoryOption("other_inc", "Thu nhập khác", "💡"),
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EditIncomeContent(
    tx: TxUi,
    onBack: () -> Unit,
    onSave: (TxUi) -> Unit,
    onDelete: (TxUi) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val appBarHeight = 36.dp

    var amountText by remember(tx.id) { mutableStateOf(tx.amount.toString()) }
    var note by remember(tx.id) { mutableStateOf("") }
    var date by remember(tx.id) { mutableStateOf(tx.dateTime.toLocalDate()) }
    var selected by remember(tx.id) {
        mutableStateOf(incomeOptions.firstOrNull { it.label.equals(tx.category, true) } ?: incomeOptions.first())
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9D9D9).copy(alpha = 0.6f),
                    scrolledContainerColor = Color(0xFFD9D9D9).copy(alpha = 0.6f)
                ),
                windowInsets = WindowInsets(0),
                modifier = Modifier.height(appBarHeight)
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(appBarHeight + 12.dp))

            TitleRow(title = "Chỉnh sửa", onBack = onBack)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) amountText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền") },
                placeholder = { Text("Nhập lại số tiền...") },
                suffix = { Text("đ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text("Thay đổi nguồn thu nhập", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF6B7280))
            Spacer(Modifier.height(10.dp))

            CategoryGridIncome(
                options = incomeOptions,
                selected = selected,
                onSelect = { selected = it }
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú") },
                placeholder = { Text("Thay đổi nội dung giao dịch...") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                label = { Text("Ngày giao dịch") },
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Chọn ngày",
                            tint = Color.Black
                        )
                    }
                }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton({ showDatePicker = false }) { Text("Huỷ") } }
                ) { DatePicker(state = datePickerState) }
            }

            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.weight(1f))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val newAmount = amountText.toLongOrNull() ?: tx.amount
                        val newDateTime = tx.dateTime
                            .withYear(date.year)
                            .withMonth(date.monthValue)
                            .withDayOfMonth(date.dayOfMonth)
                        onSave(
                            tx.copy(
                                amount = newAmount,
                                category = selected.label,
                                dateTime = newDateTime
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E),
                        contentColor = Color.White
                    )
                ) {
                    Text("Lưu", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { onDelete(tx) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = "Xoá",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Xoá", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TitleRow(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Quay lại",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun CategoryGridIncome(
    options: List<IncCategoryOption>,
    selected: IncCategoryOption,
    onSelect: (IncCategoryOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { opt ->
                    val isSel = opt == selected
                    OutlinedCard(
                        onClick = { onSelect(opt) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSel) Color(0xFFEFF6FF) else Color.White
                        ),
                        border = BorderStroke(1.dp, if (isSel) Color(0xFF2563EB) else Color(0xFFE5E7EB)),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(opt.emoji, fontSize = 18.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(opt.label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
