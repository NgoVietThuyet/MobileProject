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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.theme.AppGradient
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
    onCreate: (SavingGoalDraft) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val gap = 24.dp
    val zone = ZoneId.systemDefault()

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
    var selectedColorArgb by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedColor = selectedColorArgb?.let { Color(it) }

    val suggestions = remember {
        listOf(
            "🏠" to ("mua nhà" to "2–5 tỷ"),
            "🚗" to ("mua xe" to "500M–2 tỷ"),
            "✈️" to ("du lịch" to "10–50M"),
            "🆘" to ("khẩn cấp" to "3–6 tháng lương"),
            "💍" to ("đám cưới" to ""),
            "🎓" to ("học tập" to "")
        )
    }
    val suggestionRows = remember { suggestions.chunked(2) }

    val icons = remember { listOf("🏠","🚗","✈️","🍜","📅","💻","💍","😊","🆘","🌮","☕","💰","🎮","🐶","🍕","📷","🎵","🎯") }
    val iconRows = remember { icons.chunked(6) }

    val colors = remember {
        listOf(
            Color(0xFFE8F0FE), Color(0xFFF3E8FF), Color(0xFFFFEDD5),
            Color(0xFFFEE2E2), Color(0xFFFEF3C7), Color(0xFFD1FAE5)
        )
    }
    val colorRows = remember { colors.chunked(3) }

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
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.onSurface)
                            ) { Text("Hủy") }

                            Button(
                                onClick = {
                                    val money = amount.toLongOrNull() ?: 0L
                                    onCreate(
                                        SavingGoalDraft(
                                            title = name.trim(),
                                            targetVnd = money,
                                            targetDateMillis = dateState.selectedDateMillis,
                                            emoji = selectedIcon,
                                            color = selectedColor ?: scheme.primary
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(AppGradient.BluePurple, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) { Text("Tạo mục tiêu", color = scheme.onPrimary) }
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
                    row.forEach { (emoji, pair) ->
                        SuggestCard(
                            emoji = emoji,
                            title = pair.first,
                            sub = pair.second,
                            modifier = Modifier.weight(1f),
                            scheme = scheme
                        ) { name = pair.first }
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
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
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
                                    }
                                }
                            },
                            placeholder = { Text("dd/MM/yyyy") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_calendar),
                                        contentDescription = "Chọn ngày",
                                        tint = scheme.onSurface
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                            onClick = { selectedIcon = e },
                            scheme = scheme
                        )
                    }
                }
            }

            item { SectionLabel("Chọn màu sắc", scheme) }
            items(colorRows) { row ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { c ->
                        SelectableColor(
                            color = c,
                            selected = selectedColor == c,
                            onClick = { selectedColorArgb = c.value.toInt() },
                            scheme = scheme
                        )
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
    sub: String,
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
            if (sub.isNotBlank()) Text(sub, fontSize = 12.sp, color = scheme.onSurfaceVariant)
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

@Composable
private fun SelectableColor(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    scheme: ColorScheme
) {
    Surface(
        shape = CircleShape,
        color = scheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
        modifier = Modifier.size(56.dp).clickable { onClick() }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(color))
        }
    }
}

data class SavingGoalDraft(
    val title: String,
    val targetVnd: Long,
    val targetDateMillis: Long?,
    val emoji: String?,
    val color: Color
)
