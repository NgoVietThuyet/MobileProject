@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.models.CategoryDto
import com.example.test.vm.AddTransactionViewModel
import com.example.test.vm.SaveStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var amountRaw by rememberSaveable { mutableStateOf("") }

    // Sửa: Lấy danh sách category thật từ ViewModel
    val categories = uiState.incomeCategories

    var selectedCategoryIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedCategory = selectedCategoryIndex?.let { categories.getOrNull(it) }

    var note by rememberSaveable { mutableStateOf("") }

    val zone = ZoneId.systemDefault()
    val todayMillis = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    var dateMillis by rememberSaveable { mutableStateOf(todayMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amountString = amountRaw.filter { it.isDigit() }
    val isValid = amountString.toLongOrNull()?.let { it > 0 } == true &&
            selectedCategory != null &&
            uiState.saveStatus != SaveStatus.LOADING

    val vi = Locale("vi", "VN")
    val sym = DecimalFormatSymbols(vi).apply { groupingSeparator = '.'; decimalSeparator = ',' }
    val df = remember { DecimalFormat("#,##0", sym) }

    val moneyPreview = amountString.toLongOrNull()?.let {
        if (it > 0) df.format(it) + " ₫" else ""
    } ?: ""

    val dateLabel = remember(dateMillis) {
        Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    LaunchedEffect(uiState.saveStatus) {
        when (uiState.saveStatus) {
            SaveStatus.SUCCESS -> {
                Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
                onBack()
            }
            SaveStatus.ERROR -> {
                Toast.makeText(context, "Lỗi: ${uiState.errorMessage}", Toast.LENGTH_LONG).show()
                viewModel.resetStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Thêm khoản thu",
                showBack = true,
                onBack = onBack
            )
        },
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Surface(color = scheme.surface) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.saveTransaction(
                                amount = amountString,
                                categoryId = selectedCategory!!.categoryId,
                                note = note.trim(),
                                dateMillis = dateMillis,
                                type = "Income"
                            )
                        },
                        enabled = isValid,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.tertiary,
                            contentColor = scheme.onTertiary,
                            disabledContainerColor = scheme.tertiary.copy(alpha = 0.4f),
                            disabledContentColor = scheme.onTertiary.copy(alpha = 0.9f)
                        )
                    ) {
                        if (uiState.saveStatus == SaveStatus.LOADING) {
                            CircularProgressIndicator(
                                color = scheme.onTertiary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Lưu", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = scheme.surface,
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Số tiền", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = amountRaw,
                            onValueChange = { amountRaw = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Nhập số tiền…") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Text("đ", color = scheme.onSurfaceVariant, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = scheme.surfaceVariant,
                                focusedContainerColor = scheme.surfaceVariant,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                cursorColor = scheme.onSurface
                            )
                        )

                        if (moneyPreview.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("≈ $moneyPreview", color = scheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Chọn danh mục", fontSize = 14.sp, color = scheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                CategoryGrid( // Sửa: Dùng chung CategoryGrid
                    categories = categories,
                    selectedIndex = selectedCategoryIndex,
                    onSelect = { selectedCategoryIndex = it },
                    scheme = scheme
                )

                Spacer(Modifier.height(20.dp))

                Text("Ghi chú", fontSize = 14.sp, color = scheme.onSurfaceVariant)
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
                        unfocusedContainerColor = scheme.surfaceVariant,
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = scheme.primary
                    )
                )

                Spacer(Modifier.height(16.dp))

                Text("Ngày giao dịch:", fontSize = 14.sp, color = scheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
                        .background(scheme.surfaceVariant)
                        .padding(horizontal = 12.dp)
                        .clickable { showDatePicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateLabel, modifier = Modifier.weight(1f), fontSize = 14.sp, color = scheme.onSurface)
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@Composable
private fun CategoryGrid(
    categories: List<CategoryDto>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    scheme: ColorScheme
) {
    val rows = categories.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEachIndexed { colIdx, cat ->
                    val idx = rowIdx * 3 + colIdx
                    val isSelected = selectedIndex == idx
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = scheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) scheme.primary else scheme.outlineVariant),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clickable { onSelect(idx) }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) scheme.primaryContainer else scheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) { Text(cat.icon, fontSize = 18.sp, textAlign = TextAlign.Center) }
                                Spacer(Modifier.height(6.dp))
                                Text(cat.name, fontSize = 13.sp, color = scheme.onSurface)
                            }
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f).height(80.dp)) }
            }
        }
    }
}
