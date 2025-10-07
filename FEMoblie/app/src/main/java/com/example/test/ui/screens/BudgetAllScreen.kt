package com.example.test.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.mock.MockData

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAllScreen(
    onBack: () -> Unit = {},
    onOpenEdit: (Int) -> Unit = {},
    onAdd: () -> Unit = {}
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val budgets = MockData.budgetCategories
    val totalUsedM = budgets.sumOf { parseUsedM(it.amount) }
    val totalBudgetM = budgets.sumOf { parseTotalM(it.amount) }
    val progress = if (totalBudgetM > 0.0) (totalUsedM / totalBudgetM).toFloat().coerceIn(0f, 1f) else 0f

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
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = padding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            item { Spacer(Modifier.height(appBarHeight + 12.dp)) }

            // Header
            item {
                TitleRow(
                    title = "Ngân sách theo danh mục",
                    onBack = onBack
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                TotalBudgetCard(
                    usedLabel = "${formatM(totalUsedM)} ₫",
                    totalLabel = "${formatM(totalBudgetM)} ₫",
                    percent = progress
                )
                Spacer(Modifier.height(16.dp))
            }

            itemsIndexed(
                items = budgets,
                key = { index, item -> "${item.title}#$index" }
            ) { index, cat ->
                BudgetRow(
                    icon = cat.icon,
                    title = cat.title,
                    amount = cat.amount,
                    progress = cat.progress,
                    color = cat.color,
                    onClick = { onOpenEdit(index) }
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF111111), RoundedCornerShape(16.dp))
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) { Text("+", fontSize = 18.sp) }
                        Spacer(Modifier.height(8.dp))
                        Text("Thêm danh mục mới", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TotalBudgetCard(
    usedLabel: String,
    totalLabel: String,
    percent: Float
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tổng chi tiêu tháng này", color = Color(0xFF7B8090), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text(usedLabel, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D73F5))
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(percent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF3D73F5))
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Đã chi: ${(percent * 100).toInt()}%", color = Color(0xFF7B8090), fontSize = 12.sp)
                Text("Ngân sách: $totalLabel", color = Color(0xFF7B8090), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TitleRow(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Quay lại", tint = Color.Black)
        }
        Spacer(Modifier.width(6.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}


@Composable
private fun BudgetRow(
    icon: String,
    title: String,
    amount: String,
    progress: Float,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) { Text(icon, fontSize = 16.sp) }
                    Spacer(Modifier.width(12.dp))
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Text(amount, color = Color(0xFF7B8090), fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}% đã sử dụng", color = Color(0xFF7B8090), fontSize = 12.sp)
        }
    }
}

private fun parseUsedM(amount: String): Double {
    val first = amount.split('/').firstOrNull().orEmpty()
    return parseM(first)
}
private fun parseTotalM(amount: String): Double {
    val parts = amount.split('/')
    val second = parts.getOrNull(1)?.trim().orEmpty()
    return parseM(second)
}

// "2.1M" hoặc "2,1M" -> 2.1
private fun parseM(text: String): Double {
    if (text.isEmpty()) return 0.0
    val cleaned = text.lowercase()
        .replace(",", ".")
        .replace("[^0-9\\.]".toRegex(), "")
    return cleaned.toDoubleOrNull() ?: 0.0
}

// 7.0 -> "7.0M"
private fun formatM(v: Double): String {
    val r = kotlin.math.round(v * 10.0) / 10.0
    return if (r % 1.0 == 0.0) "${r.toInt()}M" else String.format(java.util.Locale.US, "%.1fM", r)
}
