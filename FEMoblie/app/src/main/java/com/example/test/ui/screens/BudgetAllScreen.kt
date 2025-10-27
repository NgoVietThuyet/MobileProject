@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.test.ui.api.AuthStore
import com.example.test.ui.components.AppHeader
import com.example.test.vm.BudgetAllViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetAllScreen(
    onBack: () -> Unit = {},
    onOpenEdit: (Int) -> Unit = {},
    onAdd: () -> Unit = {},
    viewModel: BudgetAllViewModel = hiltViewModel()
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val cs = MaterialTheme.colorScheme

    val userId by AuthStore.userIdFlow.collectAsState()

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) viewModel.load(userId)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, userId) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME && !userId.isNullOrBlank()) {
                viewModel.load(userId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val state by viewModel.uiState.collectAsState()

    val totalUsedM = state.items.sumOf { parseUsedM(it.amountLabel) }
    val totalBudgetM = state.items.sumOf { parseTotalM(it.amountLabel) }
    val progress = if (totalBudgetM > 0.0) (totalUsedM / totalBudgetM).toFloat().coerceIn(0f, 1f) else 0f

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = { AppHeader(title = "Ngân sách theo danh mục", showBack = true, onBack = onBack) },
        containerColor = cs.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(cs.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            item { Spacer(Modifier.height(appBarHeight + 12.dp)) }

            item {
                TotalBudgetCard(
                    usedLabel = "${formatM(totalUsedM)} ₫",
                    totalLabel = "${formatM(totalBudgetM)} ₫",
                    percent = progress
                )
                Spacer(Modifier.height(16.dp))
            }

            if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }

            state.error?.let { err ->
                item {
                    Text("Lỗi: $err", color = Color.Red)
                    Spacer(Modifier.height(8.dp))
                }
            }

            itemsIndexed(
                items = state.items,
                key = { index, item -> "${item.id}#$index" }
            ) { index, cat ->
                BudgetRow(
                    icon = cat.icon,
                    title = cat.title,
                    amount = cat.amountLabel,
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
                        .border(1.dp, cs.outline, RoundedCornerShape(16.dp))
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cs.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text("+", fontSize = 18.sp, color = cs.onSurfaceVariant) }
                        Spacer(Modifier.height(8.dp))
                        Text("Thêm danh mục mới", fontSize = 16.sp, color = cs.onSurface)
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
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(1.dp, cs.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tổng chi tiêu tháng này", color = cs.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text(usedLabel, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = cs.primary)
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(cs.surfaceVariant)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(percent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(cs.primary)
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Đã chi: ${(percent * 100).toInt()}%", color = cs.onSurfaceVariant, fontSize = 12.sp)
                Text("Ngân sách: $totalLabel", color = cs.onSurfaceVariant, fontSize = 12.sp)
            }
        }
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
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(1.dp, cs.outline),
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
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = cs.onSurface)
                }
                Text(amount, color = cs.onSurfaceVariant, fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(cs.surfaceVariant)
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
            Text("${(progress * 100).toInt()}% đã sử dụng", color = cs.onSurfaceVariant, fontSize = 12.sp)
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
private fun parseM(text: String): Double {
    if (text.isEmpty()) return 0.0
    val cleaned = text.lowercase()
        .replace(",", ".")
        .replace("[^0-9\\.]".toRegex(), "")
    return cleaned.toDoubleOrNull() ?: 0.0
}
private fun formatM(v: Double): String {
    val r = kotlin.math.round(v * 10.0) / 10.0
    return if (r % 1.0 == 0.0) "${r.toInt()}M" else String.format(java.util.Locale.US, "%.1fM", r)
}
