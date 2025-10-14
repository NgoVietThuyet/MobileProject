@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.mock.TxUi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditExpenseScreen(
    tx: TxUi,
    onBack: () -> Unit = {},
    onSave: (TxUi) -> Unit,
    onDelete: (TxUi) -> Unit
) {
    EditExpenseContent(tx, onBack, onSave, onDelete)
}

private data class ExpCategoryOption(val key: String, val label: String, val emoji: String)

private val expenseOptions = listOf(
    ExpCategoryOption("food", "Ăn uống", "🍽️"),
    ExpCategoryOption("transport", "Đi lại", "🚌"),
    ExpCategoryOption("shopping", "Mua sắm", "🛍️"),
    ExpCategoryOption("entertain", "Giải trí", "🎮"),
    ExpCategoryOption("education", "Giáo dục", "📚"),
    ExpCategoryOption("health", "Y tế", "🩺"),
    ExpCategoryOption("housing", "Nhà ở", "🏠"),
    ExpCategoryOption("utilities", "Điện nước", "💧"),
    ExpCategoryOption("other_exp", "Khác", "📦"),
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EditExpenseContent(
    tx: TxUi,
    onBack: () -> Unit,
    onSave: (TxUi) -> Unit,
    onDelete: (TxUi) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var amountText by remember(tx.id) { mutableStateOf(tx.amount.toString()) }
    var note by remember(tx.id) { mutableStateOf("") }
    var date by remember(tx.id) { mutableStateOf(tx.dateTime.toLocalDate()) }
    var selected by remember(tx.id) {
        mutableStateOf(expenseOptions.firstOrNull { it.label.equals(tx.category, true) } ?: expenseOptions.first())
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Chỉnh sửa",
                showBack = true,
                onBack = onBack
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(scheme.surface)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
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
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary
                    )
                ) { Text("Lưu", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }

                OutlinedButton(
                    onClick = { onDelete(tx) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, scheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = "Xoá",
                        modifier = Modifier.size(18.dp),
                        tint = scheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Xoá", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(inner)
                .consumeWindowInsets(inner)
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) amountText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền") },
                placeholder = { Text("Nhập lại số tiền...") },
                suffix = { Text("đ", color = scheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = scheme.surface,
                    focusedContainerColor = scheme.surface,
                    unfocusedBorderColor = scheme.outlineVariant,
                    focusedBorderColor = scheme.primary,
                    cursorColor = scheme.primary
                )
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Thay đổi danh mục chi tiêu",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = scheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            CategoryGridExpense(
                options = expenseOptions,
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
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = scheme.surface,
                    focusedContainerColor = scheme.surface,
                    unfocusedBorderColor = scheme.outlineVariant,
                    focusedBorderColor = scheme.primary,
                    cursorColor = scheme.primary
                )
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
                            tint = scheme.onSurfaceVariant
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = scheme.surface,
                    focusedContainerColor = scheme.surface,
                    unfocusedBorderColor = scheme.outlineVariant,
                    focusedBorderColor = scheme.primary,
                    cursorColor = scheme.primary
                )
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
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") } }
                ) { DatePicker(state = datePickerState) }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryGridExpense(
    options: List<ExpCategoryOption>,
    selected: ExpCategoryOption,
    onSelect: (ExpCategoryOption) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { opt ->
                    val isSel = opt == selected
                    OutlinedCard(
                        onClick = { onSelect(opt) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSel) scheme.primaryContainer else scheme.surface
                        ),
                        border = BorderStroke(1.dp, if (isSel) scheme.primary else scheme.outlineVariant),
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
                            Text(
                                opt.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSel) scheme.onPrimaryContainer else scheme.onSurface
                            )
                        }
                    }
                }
                if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
