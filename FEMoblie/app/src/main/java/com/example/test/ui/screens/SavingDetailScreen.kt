@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.mock.SavingGoalMock
import com.example.test.ui.theme.AppGradient
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun SavingDetailScreen(
    goal: SavingGoalMock,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSavedChange: (Long) -> Unit = {}
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val initSaved = (goal.savedM * 1_000_000).toLong()
    val total     = (goal.totalM * 1_000_000).toLong()
    var currentSaved by rememberSaveable { mutableStateOf(initSaved) }
    var input by rememberSaveable { mutableStateOf("") }
    val progress = if (total > 0) (currentSaved.toDouble() / total).coerceIn(0.0, 1.0) else 0.0
    val pct = (progress * 100).roundToInt()

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
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = AppGradient.BluePurple)
                .padding(top = pad.calculateTopPadding())
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Chi tiết mục tiêu",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(goal.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) { Text(goal.emoji, fontSize = 22.sp, color = Color.White) }

                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Mục tiêu của bạn", fontWeight = FontWeight.Medium, color = Color.White)
                                Text(
                                    "${fmtVndExact(fromMToVnd(goal.savedM))} / ${fmtVndExact(fromMToVnd(goal.totalM))}",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        ContinuousLinearProgress(
                            progress = progress.toFloat(),
                            color = Color.Black,
                            trackColor = Color(0xFFBFD4FF),
                            height = 16.dp,
                            corner = 12.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$pct% hoàn thành", fontSize = 14.sp, color = Color.White)
                            Spacer(Modifier.weight(1f))
                            Icon(painterResource(R.drawable.ic_calendar), null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("${goal.daysRemain} ngày", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = Color.White
            ) {
                Column(Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        color = Color.White
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Cập nhật số tiền", fontWeight = FontWeight.Medium, fontSize = 22.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Số tiền", fontSize = 15.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = input,
                                onValueChange = { s -> input = s.filter { ch -> ch.isDigit() || ch == '-' } },
                                placeholder = { Text("VD: 1050000") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val delta = parseVnd(input)
                                        currentSaved = (currentSaved + delta).coerceAtMost(total)
                                        input = ""
                                        onSavedChange(currentSaved)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                                ) {
                                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(22.dp)) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("+", color = Color(0xFF22C55E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text("Thêm tiền", color = Color.White, fontSize = 16.sp)
                                }
                                Button(
                                    onClick = {
                                        val delta = parseVnd(input)
                                        currentSaved = (currentSaved - delta).coerceAtLeast(0L)
                                        input = ""
                                        onSavedChange(currentSaved)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                                ) {
                                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(22.dp)) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("—", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text("Rút ra", color = Color.White, fontSize = 16.sp)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Số dư hiện tại: " + fmtVndExact(currentSaved),
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(2.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444))
                        Spacer(Modifier.width(8.dp))
                        Text("Xóa mục tiêu")
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ContinuousLinearProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    height: Dp,
    corner: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(trackColor)
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(color)
        )
    }
}

private fun fmtM(v: Float): String {
    val r = (kotlin.math.round(v * 10f) / 10f)
    val s = if (r % 1f == 0f) r.toInt().toString() else String.format(java.util.Locale.US, "%.1f", r)
    return "${s}M"
}

private fun fromMToVnd(m: Float): Long = (m * 1_000_000f).toLong() // không làm tròn

private fun fmtVndExact(value: Long): String {
    val df = DecimalFormat("#,###")
    df.decimalFormatSymbols = df.decimalFormatSymbols.apply { groupingSeparator = '.' }
    val sign = if (value < 0) "-" else ""
    val abs = kotlin.math.abs(value)
    return "$sign${df.format(abs)}đ"
}

private fun parseVnd(s: String): Long {
    val cleaned = s.trim().replace("[^0-9-]".toRegex(), "")
    return cleaned.toLongOrNull() ?: 0L
}
