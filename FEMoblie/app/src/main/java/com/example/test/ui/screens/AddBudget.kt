package com.example.test.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AmountStyleChips
import com.example.test.ui.mock.MockData
import com.example.test.ui.util.AmountStyle
import com.example.test.ui.util.MoneyUiConfig
import com.example.test.ui.util.NumberFmt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddBudgetScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onCreate: (name: String, amountVnd: Long, icon: String, color: Color) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var amountRaw by rememberSaveable { mutableStateOf("") }
    var selectedIcon by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedColor by rememberSaveable { mutableStateOf<Color?>(null) }
    var style by rememberSaveable { mutableStateOf(MoneyUiConfig.DEFAULT_STYLE) }

    val icons = remember {
        listOf("🥗","🎮","❄️","🍜","📱","💍","⚠️","🧾","💰","🛒","🍗","🍕","🥤","🎵","🚗")
    }
    val colors = remember {
        listOf(
            Color(0xFF4C6FFF),
            Color(0xFF7C4DFF),
            Color(0xFFFF8A00),
            Color(0xFFFF2D2D),
            Color(0xFFFFC107),
            Color(0xFF34C759),
        )
    }

    fun sanitizeDigits(input: String) = input.filter { it.isDigit() }

    val amountLong = amountRaw.toLongOrNull() ?: 0L
    val isValid = name.isNotBlank() && amountLong > 0 && selectedIcon != null && selectedColor != null

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerHeight = 36.dp

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFFD9D9D9).copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {}
        },
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Surface(color = Color(0xFFF6F6F7)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFE3E3E7))
                    ) { Text("Huỷ") }

                    Button(
                        onClick = {
                            MockData.addBudgetVnd(
                                name = name.trim(),
                                totalVnd = amountLong,
                                icon = selectedIcon!!,
                                color = selectedColor!!
                            )
                            onCreate(name.trim(), amountLong, selectedIcon!!, selectedColor!!)
                            onBack()
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF34C759), Color(0xFF4C6FFF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tạo ngân sách", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
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
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Quay lại",
                        tint = Color.Black
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text("Thêm ngân sách", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Text("Tên ngân sách", fontSize = 14.sp, color = Color(0xFF5F6167))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 14.dp),
                placeholder = { Text("Nhập ngân sách...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F2F4),
                    focusedContainerColor = Color(0xFFF1F2F4),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF4C6FFF)
                )
            )

            Text("Số tiền cho ngân sách", fontSize = 14.sp, color = Color(0xFF5F6167))
            OutlinedTextField(
                value = amountRaw,
                onValueChange = { input -> amountRaw = sanitizeDigits(input) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                placeholder = { Text("Nhập số tiền VND...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF1F2F4),
                    focusedContainerColor = Color(0xFFF1F2F4),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF4C6FFF)
                )
            )

            if (amountLong > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Xem trước:", color = Color(0xFF84868B), fontSize = 12.sp)
                    AmountStyleChips(style = style, onChange = { style = it })
                }
                Spacer(Modifier.height(6.dp))

                val preview = when (style) {
                    AmountStyle.VND_PLAIN   -> NumberFmt.vndPlain(amountLong) + " ₫"
                    AmountStyle.VND_PADDED  -> NumberFmt.vndPadded(amountLong) + " ₫"
                    AmountStyle.MILLION_1DP -> NumberFmt.millionLabel(NumberFmt.toM(amountLong), 1)
                    AmountStyle.MILLION_0DP -> NumberFmt.millionLabel(NumberFmt.toM(amountLong), 0)
                }
                Text(
                    text = "≈ $preview",
                    color = Color(0xFF84868B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            } else {
                Spacer(Modifier.height(14.dp))
            }

            Text("Chọn biểu tượng", fontSize = 14.sp, color = Color(0xFF5F6167))
            Spacer(Modifier.height(8.dp))
            IconGrid(
                icons = icons,
                selected = selectedIcon,
                onSelect = { selectedIcon = it }
            )

            Spacer(Modifier.height(16.dp))

            Text("Chọn màu sắc", fontSize = 14.sp, color = Color(0xFF5F6167))
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE3E3E7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    ColorGrid(
                        colors = colors,
                        selected = selectedColor,
                        onSelect = { selectedColor = it }
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun IconGrid(
    icons: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    val chunked = icons.chunked(5)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunked.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { icon ->
                    val isSelected = selected == icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF4C6FFF) else Color(0xFFE3E3E7)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onSelect(icon) }
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(icon, fontSize = 18.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                repeat(5 - row.size) {
                    Spacer(Modifier.weight(1f).height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun ColorGrid(
    colors: List<Color>,
    selected: Color?,
    onSelect: (Color) -> Unit
) {
    val rows = colors.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { line ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                line.forEach { c ->
                    val isSelected = selected == c
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFFF7F7F9))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { onSelect(c) }
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(28.dp))
                                    .border(BorderStroke(2.dp, Color(0xFF4C6FFF)), RoundedCornerShape(28.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
