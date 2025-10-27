@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.ui.components.AppHeader
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.vm.ReportViewModel
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private data class ReportSlice(val label: String, val value: Float, val color: Color)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportScreen(
    onExportPdf: () -> Unit = {},
    onExportExcel: () -> Unit = {},
    onHome: () -> Unit = {},
    onReport: () -> Unit = {},
    onSaving: () -> Unit = {},
    onSetting: () -> Unit = {},
    onCamera: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var tab by remember { mutableStateOf(0) }

    val scheme = MaterialTheme.colorScheme

    val pieSlices: List<ReportSlice> = remember(uiState.budgetPieData, scheme) {
        val palette = listOf(
            scheme.primary, scheme.secondary, scheme.tertiary,
            scheme.inversePrimary, scheme.error, scheme.outline
        )
        uiState.budgetPieData.mapIndexed { i, data ->
            ReportSlice(
                label = data.label,
                value = data.value,
                color = palette[i % palette.size]
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Báo cáo",
                showBack = false,
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = BottomTab.REPORT,
                onHome = onHome,
                onReport = onReport,
                onCamera = onCamera,
                onSaving = onSaving,
                onSetting = onSetting
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegTab(
                        "Tuần",
                        uiState.currentPeriod == 0,
                        selectedColor = scheme.secondary,
                        selectedLabel = scheme.onSecondary
                    ) { viewModel.setPeriod(0) }
                    SegTab(
                        "Tháng",
                        uiState.currentPeriod == 1,
                        selectedColor = scheme.secondary,
                        selectedLabel = scheme.onSecondary
                    ) { viewModel.setPeriod(1) }
                    SegTab(
                        "Năm",
                        uiState.currentPeriod == 2,
                        selectedColor = scheme.secondary,
                        selectedLabel = scheme.onSecondary
                    ) { viewModel.setPeriod(2) }
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val incomeM = uiState.kpiIncome / 1_000_000f
                    val expenseM = uiState.kpiExpense / 1_000_000f
                    val savingM = (uiState.kpiIncome - uiState.kpiExpense) / 1_000_000f

                    KpiCard(
                        title = "Thu nhập",
                        value = "+${"%.1f".format(incomeM)}M",
                        valueColor = scheme.tertiary,
                        icon = Icons.Outlined.TrendingUp,
                        iconTint = scheme.tertiary,
                        iconBg = scheme.tertiary.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Chi tiêu",
                        value = "-${"%.1f".format(expenseM)}M",
                        valueColor = scheme.error,
                        icon = Icons.Outlined.TrendingDown,
                        iconTint = scheme.error,
                        iconBg = scheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Tiết kiệm",
                        value = "${if (savingM > 0) "+" else ""}${"%.1f".format(savingM)}M",
                        valueColor = scheme.primary,
                        icon = Icons.Outlined.Savings,
                        iconTint = scheme.primary,
                        iconBg = scheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegTab("Tổng quan", tab == 0) { tab = 0 }
                    SegTab("Danh mục", tab == 1) { tab = 1 }
                    SegTab("Xu hướng", tab == 2) { tab = 2 }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Lỗi: ${uiState.error}",
                            color = scheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder(),
                        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        when (tab) {
                            0 -> {
                                val maxV = max(
                                    uiState.overviewIncome.maxOrNull() ?: 0f,
                                    uiState.overviewExpense.maxOrNull() ?: 0f
                                )
                                val ticks = yTicks(maxV)
                                ChartWithAxes(
                                    yTicks = ticks,
                                    xLabels = uiState.overviewLabels,
                                    chart = {
                                        BarCompareChart(
                                            income = uiState.overviewIncome,
                                            expense = uiState.overviewExpense,
                                            yMax = ticks.last()
                                        )
                                    }
                                )
                            }
                            1 -> {
                                Column(Modifier.padding(12.dp)) {
                                    PieChartDynamic(pieSlices)
                                }
                            }
                            else -> {
                                val maxV = uiState.trendExpense.maxOrNull() ?: 0f
                                val ticks = yTicks(maxV)
                                ChartWithAxes(
                                    yTicks = ticks,
                                    xLabels = uiState.trendLabels,
                                    chart = {
                                        LineChart(
                                            pointsM = uiState.trendExpense,
                                            yMax = ticks.last()
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoChip(
                            Icons.Outlined.TrendingUp,
                            "Bạn đã tiết kiệm được 18% so với tháng trước",
                            scheme.primaryContainer,
                            scheme.primary
                        )
                        InfoChip(
                            Icons.Outlined.Event,
                            "Chi tiêu nhiều nhất vào thứ 6 và chủ nhật",
                            scheme.secondaryContainer,
                            scheme.secondary
                        )
                        InfoChip(
                            Icons.Outlined.Paid,
                            "Danh mục “Ăn uống” chiếm 35% tổng chi tiêu",
                            scheme.tertiaryContainer,
                            scheme.tertiary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Xuất báo cáo", fontWeight = FontWeight.Medium, color = scheme.onSurface)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExportPdf,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, scheme.outlineVariant)
                            ) {
                                Icon(Icons.Outlined.FileDownload, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Xuất PDF")
                            }
                            OutlinedButton(
                                onClick = onExportExcel,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, scheme.outlineVariant)
                            ) {
                                Icon(Icons.Outlined.FileDownload, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Xuất Excel")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun yTicks(maxValue: Float): List<Float> {
    val m = if (maxValue <= 0f) 1f else maxValue
    val step = max(0.5f, ceil(m / 4f * 2f) / 2f)
    return (0..4).map { it * step }
}

@Composable
private fun ChartWithAxes(
    yTicks: List<Float>,
    xLabels: List<String>,
    chart: @Composable BoxScope.() -> Unit
) {
    val yAxisWidth = 44.dp
    val h = 200.dp

    Column(Modifier.fillMaxWidth().padding(12.dp)) {

        Row(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .height(h),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yTicks.reversed().forEach { v ->
                    Text("${v}M", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(Modifier.height(h).weight(1f)) { chart() }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(yAxisWidth))
            Row(Modifier.weight(1f)) {
                if (xLabels.isNotEmpty()) {
                    xLabels.forEach {
                        Text(
                            it,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun BarCompareChart(income: List<Float>, expense: List<Float>, yMax: Float) {
    val scheme = MaterialTheme.colorScheme
    val pos = scheme.tertiary
    val neg = scheme.error
    val grid = scheme.outlineVariant
    Canvas(Modifier.fillMaxSize()) {
        val n = max(income.size, expense.size).coerceAtLeast(1)
        val w = size.width
        val h = size.height
        val pad = 8f
        val plotW = w - pad * 2
        val plotH = h - pad * 2
        val groupW = plotW / n
        val barW = (groupW * 0.8f) / 2f

        for (i in 1 until 4) {
            val y = pad + plotH * i / 4f
            drawLine(grid, Offset(pad, y), Offset(pad + plotW, y), 1f)
        }

        for (i in 0 until n) {
            val inc = income.getOrNull(i) ?: 0f
            val exp = expense.getOrNull(i) ?: 0f
            val h1 = if (yMax > 0) (inc / yMax) * plotH else 0f
            val h2 = if (yMax > 0) (exp / yMax) * plotH else 0f
            val gx = pad + groupW * i + (groupW * 0.1f)
            drawRect(pos, topLeft = Offset(gx, pad + plotH - h1), size = Size(barW, h1))
            drawRect(neg, topLeft = Offset(gx + barW, pad + plotH - h2), size = Size(barW, h2))
        }
    }
}

@Composable
private fun LineChart(pointsM: List<Float>, yMax: Float) {
    val scheme = MaterialTheme.colorScheme
    val line = scheme.secondary
    val grid = scheme.outlineVariant
    val inner = scheme.surface
    Canvas(Modifier.fillMaxSize()) {
        if (pointsM.isEmpty()) return@Canvas
        val pad = 8f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val stepX = if (pointsM.size > 1) w / (pointsM.size - 1) else w

        for (i in 1 until 4) {
            val y = pad + h * i / 4f
            drawLine(grid, Offset(pad, y), Offset(pad + w, y), 1f)
        }

        val path = Path()
        pointsM.forEachIndexed { i, v ->
            val x = pad + stepX * i
            val y = pad + h - if (yMax > 0) (v / yMax) * h else 0f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, line, style = Stroke(width = 4f))
        pointsM.forEachIndexed { i, _ ->
            val x = pad + stepX * i
            val y = pad + h - if (yMax > 0) (pointsM[i] / yMax) * h else 0f
            drawCircle(line, radius = 6f, center = Offset(x, y))
            drawCircle(inner, radius = 3f, center = Offset(x, y))
        }
    }
}

@Composable
private fun PieChartDynamic(data: List<ReportSlice>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(140.dp)) {
            val total = data.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.001f)
            val sizePx = min(size.width, size.height)
            val radius = sizePx
            val topLeft = Offset((size.width - radius) / 2f, (size.height - radius) / 2f)
            var start = -90f
            data.forEach { s ->
                val sweep = 360f * (s.value / total)
                drawArc(color = s.color, startAngle = start, sweepAngle = sweep, useCenter = true, topLeft = topLeft, size = Size(radius, radius))
                start += sweep
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (data.isEmpty()) {
                Text("Không có dữ liệu ngân sách", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            data.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(it.color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        it.label,
                        Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text("${"%.1f".format(it.value)}M", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SegTab(
    text: String,
    selected: Boolean,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    selectedLabel: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.Medium) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = selectedColor,
            selectedLabelColor = selectedLabel
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
            borderWidth = if (selected) 0.dp else 1.dp
        )
    )
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = iconBg, modifier = Modifier.size(34.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(icon, null, tint = iconTint)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, bg: Color, tint: Color) {
    Surface(color = bg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint)
            Spacer(Modifier.width(8.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}