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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.mock.MockData
import java.time.*
import java.time.format.DateTimeFormatter
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
    onCamera: () -> Unit = {}
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var period by remember { mutableStateOf(1) }
    var tab by remember { mutableStateOf(0) }

    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }

    val dayLabels: List<String>
    val barsIncome: List<Float>
    val barsExpense: List<Float>
    run {
        val days = (5 downTo 0).map { today.minusDays(it.toLong()) }
        val fmt = DateTimeFormatter.ofPattern("dd/MM")
        dayLabels = days.map { it.format(fmt) }
        val grouped = MockData.recentTransactions.groupBy {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate()
        }
        fun sumFor(d: LocalDate, positive: Boolean): Float =
            grouped[d]
                ?.filter { it.isPositive == positive }
                ?.sumOf { it.amount.filter(Char::isDigit).toLongOrNull() ?: 0L }?.toFloat()
                ?.div(1_000_000f) ?: 0f

        barsIncome = days.map { sumFor(it, true) }
        barsExpense = days.map { sumFor(it, false) }
    }

    val pie: List<ReportSlice> = remember {
        val byCat = MockData.recentTransactions
            .filter { !it.isPositive }
            .groupBy { it.subtitle.substringBefore(" • ").trim() }
            .mapValues { e -> e.value.sumOf { it.amount.filter(Char::isDigit).toLongOrNull() ?: 0L } }
        val colors = listOf(
            Color(0xFF22C55E), Color(0xFF60A5FA), Color(0xFFF59E0B),
            Color(0xFFFB7185), Color(0xFFA78BFA), Color(0xFF94A3B8)
        )
        byCat.entries.sortedByDescending { it.value }.mapIndexed { i, (k, v) ->
            ReportSlice(k, v / 1_000_000f, colors[i % colors.size])
        }
    }

    val weekLabels: List<String>
    val lineExpense: List<Float>
    run {
        val start = today.with(DayOfWeek.MONDAY)
        val weekDays = (0..6).map { start.plusDays(it.toLong()) }
        weekLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        val grouped = MockData.recentTransactions
            .filter { !it.isPositive }
            .groupBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }
        lineExpense = weekDays.map { d ->
            grouped[d]?.sumOf { it.amount.filter(Char::isDigit).toLongOrNull() ?: 0L }?.toFloat()
                ?.div(1_000_000f) ?: 0f
        }
    }

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
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)
        ) {
            item { Spacer(Modifier.height(appBarHeight + 12.dp)) }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Báo cáo tài chính", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { /* filter */ }) { Icon(Icons.Outlined.FilterList, null) }
                    IconButton(onClick = { /* export */ }) { Icon(Icons.Outlined.IosShare, null) }
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegTab("Tuần", period == 0) { period = 0 }
                    SegTab("Tháng", period == 1) { period = 1 }
                    SegTab("Năm", period == 2) { period = 2 }
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(
                        title = "Thu nhập",
                        value = "+30M đ",
                        valueColor = Color(0xFF16A34A),
                        icon = Icons.Outlined.TrendingUp,
                        iconTint = Color(0xFF16A34A),
                        iconBg = Color(0xFF16A34A).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Chi tiêu",
                        value = "-25M đ",
                        valueColor = Color(0xFFDC2626),
                        icon = Icons.Outlined.TrendingDown,
                        iconTint = Color(0xFFDC2626),
                        iconBg = Color(0xFFDC2626).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Tiết kiệm",
                        value = "+5M đ",
                        valueColor = Color(0xFF2563EB),
                        icon = Icons.Outlined.Savings,
                        iconTint = Color(0xFF2563EB),
                        iconBg = Color(0xFF2563EB).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegTab("Tổng quan", tab == 0) { tab = 0 }
                    SegTab("Danh mục", tab == 1) { tab = 1 }
                    SegTab("Xu hướng", tab == 2) { tab = 2 }
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (tab) {
                        0 -> {
                            val maxV = max(barsIncome.maxOrNull() ?: 0f, barsExpense.maxOrNull() ?: 0f)
                            val ticks = yTicks(maxV)
                            ChartWithAxes(
                                yTicks = ticks,
                                xLabels = dayLabels,
                                chart = {
                                    BarCompareChart(
                                        income = barsIncome,
                                        expense = barsExpense,
                                        yMax = ticks.last()
                                    )
                                }
                            )
                        }
                        1 -> {
                            Column(Modifier.padding(12.dp)) { PieChartDynamic(pie) }
                        }
                        else -> {
                            val maxV = lineExpense.maxOrNull() ?: 0f
                            val ticks = yTicks(maxV)
                            ChartWithAxes(
                                yTicks = ticks,
                                xLabels = weekLabels,
                                chart = { LineChart(pointsM = lineExpense, yMax = ticks.last()) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoChip(Icons.Outlined.TrendingUp, "Bạn đã tiết kiệm được 18% so với tháng trước", Color(0xFFEFFDF4), Color(0xFF16A34A))
                        InfoChip(Icons.Outlined.Event, "Chi tiêu nhiều nhất vào thứ 6 và chủ nhật", Color(0xFFF2F6FF), Color(0xFF2563EB))
                        InfoChip(Icons.Outlined.Paid, "Danh mục “Ăn uống” chiếm 35% tổng chi tiêu", Color(0xFFFFF7ED), Color(0xFFF59E0B))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Xuất báo cáo", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onExportPdf,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.FileDownload, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Xuất PDF")
                            }
                            OutlinedButton(
                                onClick = onExportExcel,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
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

// helpers //

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
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        val h = 200.dp
        Row(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.width(44.dp).height(h),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yTicks.reversed().forEach { v ->
                    Text("${v}M", fontSize = 10.sp, color = Color(0xFF6B7280))
                }
            }
            Box(Modifier.height(h).weight(1f)) { chart() }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            xLabels.forEach { Text(it, fontSize = 10.sp, color = Color(0xFF6B7280)) }
        }
    }
}

@Composable
private fun BarCompareChart(income: List<Float>, expense: List<Float>, yMax: Float) {
    val green = Color(0xFF16A34A)
    val red = Color(0xFFDC2626)
    Canvas(Modifier.fillMaxSize()) {
        val n = max(income.size, expense.size).coerceAtLeast(1)
        val w = size.width
        val h = size.height
        val pad = 8f
        val plotW = w - pad * 2
        val plotH = h - pad * 2
        val groupW = plotW / n
        val barW = groupW / 3

        for (i in 1 until 4) {
            val y = pad + plotH * i / 4f
            drawLine(Color(0xFFE5E7EB), Offset(pad, y), Offset(pad + plotW, y), 1f)
        }

        for (i in 0 until n) {
            val inc = income.getOrNull(i) ?: 0f
            val exp = expense.getOrNull(i) ?: 0f
            val h1 = if (yMax > 0) (inc / yMax) * plotH else 0f
            val h2 = if (yMax > 0) (exp / yMax) * plotH else 0f
            val gx = pad + groupW * i
            drawRect(green, topLeft = Offset(gx + barW * 0.8f, pad + plotH - h1), size = Size(barW, h1))
            drawRect(red,   topLeft = Offset(gx + barW * 1.9f, pad + plotH - h2), size = Size(barW, h2))
        }
    }
}

@Composable
private fun LineChart(pointsM: List<Float>, yMax: Float) {
    Canvas(Modifier.fillMaxSize()) {
        if (pointsM.isEmpty()) return@Canvas
        val pad = 8f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val stepX = if (pointsM.size > 1) w / (pointsM.size - 1) else 0f

        for (i in 1 until 4) {
            val y = pad + h * i / 4f
            drawLine(Color(0xFFE5E7EB), Offset(pad, y), Offset(pad + w, y), 1f)
        }

        val path = Path()
        pointsM.forEachIndexed { i, v ->
            val x = pad + stepX * i
            val y = pad + h - if (yMax > 0) (v / yMax) * h else 0f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF2563EB), style = Stroke(width = 4f))
        pointsM.forEachIndexed { i, v ->
            val x = pad + stepX * i
            val y = pad + h - if (yMax > 0) (v / yMax) * h else 0f
            drawCircle(Color(0xFF2563EB), radius = 6f, center = Offset(x, y))
            drawCircle(Color.White, radius = 3f, center = Offset(x, y))
        }
    }
}

@Composable
private fun PieChartDynamic(data: List<ReportSlice>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.weight(1f).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(it.color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(it.label, Modifier.weight(1f))
                    Text("${"%.1f".format(it.value)}M", color = Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
private fun SegTab(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.Medium) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = Color(0xFF111827),
            selectedContainerColor = Color.Black,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color.Transparent else Color(0xFF9CA3AF),
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
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFD9D9D9))
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
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 2)
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
            Text(text, color = Color(0xFF111827))
        }
    }
}
