@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.example.test.ui.theme.AppGradient
import com.example.test.vm.AddSavingGoalViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddSavingGoalScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onCreate: (Boolean) -> Unit,
    viewModel: AddSavingGoalViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val gap = 24.dp
    val zone = ZoneId.systemDefault()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var dateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    var dateText by rememberSaveable { mutableStateOf("") }
    val dateFmt = remember { DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT) }

    fun millisToText(ms: Long?): String =
        ms?.let {
            val d = Instant.ofEpochMilli(it).atZone(zone).toLocalDate()
            "${d.dayOfMonth}/${d.monthValue}/${d.year}"
        } ?: ""

    var selectedIcon by rememberSaveable { mutableStateOf<String?>(null) }

    val suggestions = remember {
        mapOf(
            "🏠" to "mua nhà",
            "🚗" to "mua xe",
            "✈️" to "du lịch",
            "🆘" to "khẩn cấp",
            "💍" to "đám cưới",
            "🎓" to "học tập"
        )
    }
    val suggestionEntries = remember { suggestions.entries.toList() }
    val suggestionRows = remember { suggestionEntries.chunked(2) }

    val icons = remember { listOf("🏠","🚗","✈️","🍜","📅","💻","💍","🎓","🆘","🌮","☕","💰","🎮","🐶","🍕","📷","🎵","🎯") }
    val iconRows = remember { icons.chunked(6) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onCreate(true)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppHeader(
                title = "Thêm mục tiêu tiết kiệm",
                showBack = true,
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(color = scheme.surface) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
                    Divider(color = scheme.outlineVariant, thickness = 1.dp)
                    if (uiState.error != null) {
                        Text(
                            text = "Lỗi: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(64.dp)) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .widthIn(max = 420.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.onSurface),
                                enabled = !uiState.isSubmitting
                            ) { Text("Hủy") }

                            Button(
                                onClick = {
                                    val money = amount.toLongOrNull() ?: 0L
                                    val draft = SavingGoalDraft(
                                        title = name.trim(),
                                        targetVnd = money,
                                        targetDateMillis = dateState.selectedDateMillis,
                                        emoji = selectedIcon
                                    )
                                    viewModel.submitGoal(draft)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(AppGradient.BluePurple, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                enabled = name.isNotBlank() && amount.isNotBlank() && selectedIcon != null && !uiState.isSubmitting
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = scheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Tạo mục tiêu", color = scheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(top = gap, bottom = gap)
        ) {

            item { SectionLabel("Mục tiêu phổ biến", scheme) }
            items(suggestionRows) { row ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { entry ->
                        SuggestCard(
                            emoji = entry.key,
                            title = entry.value,
                            modifier = Modifier.weight(1f),
                            scheme = scheme
                        ) {
                            name = entry.value
                            selectedIcon = entry.key
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            item {
                Column {
                    SectionLabel("Tên mục tiêu", scheme)
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Nhập mục tiêu của bạn...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                    }

                    SectionLabel("Số tiền mục tiêu", scheme)
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { s -> amount = s.filter { it.isDigit() } },
                            placeholder = { Text("Nhập số tiền...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting
                        )
                    }

                    SectionLabel("Chọn ngày hoàn thành", scheme)
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = { t ->
                                dateText = t
                                runCatching {
                                    if (t.isNotBlank()) {
                                        val d = LocalDate.parse(t.trim().replace('-', '/'), dateFmt)
                                        val ms = d.atStartOfDay(zone).toInstant().toEpochMilli()
                                        dateMillis = ms
                                        dateState.selectedDateMillis = ms
                                    } else {
                                        dateMillis = null
                                        dateState.selectedDateMillis = null
                                    }
                                }
                            },
                            placeholder = { Text("dd/MM/yyyy (tuỳ chọn)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                IconButton(onClick = { showDatePicker = true }, enabled = !uiState.isSubmitting) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_calendar),
                                        contentDescription = "Chọn ngày",
                                        tint = scheme.onSurface
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !uiState.isSubmitting
                        )
                    }
                }
            }

            item { SectionLabel("Chọn biểu tượng", scheme) }
            items(iconRows) { row ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { e ->
                        SelectableIcon(
                            emoji = e,
                            selected = selectedIcon == e,
                            onClick = {
                                if (!uiState.isSubmitting) {
                                    selectedIcon = e
                                    if (suggestions.containsKey(e)) {
                                        name = suggestions[e] ?: ""
                                    } else {
                                        name = ""
                                    }
                                }
                            },
                            scheme = scheme
                        )
                    }
                    repeat(6 - row.size) {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateMillis = dateState.selectedDateMillis
                    dateText = millisToText(dateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = dateState) }
    }
}

@Composable
private fun SectionLabel(text: String, scheme: ColorScheme) {
    Text(text, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 10.dp), color = scheme.onSurfaceVariant)
}

@Composable
private fun SuggestCard(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
    scheme: ColorScheme,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.height(96.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface)
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, color = scheme.onSurface)
        }
    }
}

@Composable
private fun SelectableIcon(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    scheme: ColorScheme
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) scheme.primaryContainer else scheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.size(48.dp).clickable { onClick() }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 18.sp) }
    }
}

data class SavingGoalDraft(
    val title: String,
    val targetVnd: Long,
    val targetDateMillis: Long?,
    val emoji: String?
)