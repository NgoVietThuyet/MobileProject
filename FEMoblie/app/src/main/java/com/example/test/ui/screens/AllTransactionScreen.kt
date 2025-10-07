@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.mock.MockData
import com.example.test.ui.mock.TransactionMock
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import java.text.NumberFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllTransactionsScreen(
    onBack: () -> Unit = {},
    onEditIncome: (TxUi) -> Unit = {},
    onEditExpense: (TxUi) -> Unit = {}
) {
    AllTransactionsScreen(
        transactions = mockTxFromRecent(),
        onBack = onBack,
        onEditIncome = onEditIncome,
        onEditExpense = onEditExpense
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AllTransactionsScreen(
    transactions: List<TxUi>,
    onBack: () -> Unit = {},
    onEditIncome: (TxUi) -> Unit = {},
    onEditExpense: (TxUi) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val appBarHeight = 36.dp

    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) }

    val filtered = remember(transactions, query, tab) {
        transactions.asSequence()
            .filter {
                when (tab) {
                    1 -> it.type == TxType.INCOME
                    2 -> it.type == TxType.EXPENSE
                    else -> true
                }
            }
            .filter { query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) }
            .sortedByDescending { it.dateTime }
            .toList()
    }
    val groups = remember(filtered) { filtered.groupBy { it.dateTime.toLocalDate() } }
    val sumIncome = remember(filtered) { filtered.filter { it.type == TxType.INCOME }.sumOf { it.amount } }
    val sumExpense = remember(filtered) { filtered.filter { it.type == TxType.EXPENSE }.sumOf { it.amount } }

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

            item {
                TitleRow(title = "Tất cả giao dịch", onBack = onBack)
                Spacer(Modifier.height(12.dp))
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("tìm kiếm giao dịch...") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegmentTab("Tất cả", selected = tab == 0) { tab = 0 }
                    SegmentTab("Thu nhập", selected = tab == 1) { tab = 1 }
                    SegmentTab("Chi tiêu", selected = tab == 2) { tab = 2 }
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                SummaryCard(sumIncome, sumExpense)
                Spacer(Modifier.height(8.dp))
            }

            groups.forEach { (date, list) ->
                item { DateHeaderRow(date) }
                item {
                    DayGroupCard(
                        txs = list,
                        onEditIncome = onEditIncome,
                        onEditExpense = onEditExpense
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayGroupCard(
    txs: List<TxUi>,
    onEditIncome: (TxUi) -> Unit,
    onEditExpense: (TxUi) -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFD9D9D9)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            txs.forEachIndexed { i, tx ->
                TxRowLine(
                    tx = tx,
                    onClick = { if (tx.type == TxType.INCOME) onEditIncome(tx) else onEditExpense(tx) }
                )
                if (i != txs.lastIndex) Divider()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TxRowLine(
    tx: TxUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F5F5), shape = MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            if (tx.emoji != null) Text(tx.emoji, fontSize = 18.sp)
            else tx.iconRes?.let { Icon(painter = painterResource(it), contentDescription = null) }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(
                "${tx.category} • ${tx.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
        Text(
            text = (if (tx.type == TxType.INCOME) "+" else "-") + vn(tx.amount),
            color = if (tx.type == TxType.INCOME) Color(0xFF16A34A) else Color(0xFFDC2626),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SegmentTab(text: String, selected: Boolean, onClick: () -> Unit) {
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
private fun TitleRow(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Quay lại",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
private fun SummaryCard(allIncome: Long, allExpense: Long) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.inc), null, tint = Color(0xFF16A34A))
                    Spacer(Modifier.width(6.dp))
                    Text("Thu nhập", fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.dec), null, tint = Color(0xFFDC2626))
                    Spacer(Modifier.width(6.dp))
                    Text("Chi tiêu", fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "+${vn(allIncome)}",
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
                Text(
                    "-${vn(allExpense)}",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateHeaderRow(date: LocalDate) {
    val label = when (date) {
        LocalDate.now() -> "Hôm nay"
        LocalDate.now().minusDays(1) -> "Hôm qua"
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF6B7280))
        Divider(Modifier.weight(1f).padding(start = 12.dp))
    }
}

private fun vn(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value).replace(" ", "")

@RequiresApi(Build.VERSION_CODES.O)
private fun mockTxFromRecent(): List<TxUi> {
    val zone = ZoneId.systemDefault()
    return MockData.recentTransactions.mapIndexed { idx, m ->
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(m.createdAt), zone)
        val category = m.subtitle.substringBefore(" • ").trim()
        val amountAbs = m.amount.filter { it.isDigit() }.toLongOrNull() ?: 0L
        TxUi(
            id = "mock-$idx",
            title = m.title,
            category = category,
            dateTime = dt,
            amount = amountAbs,
            type = if (m.isPositive) TxType.INCOME else TxType.EXPENSE,
            emoji = m.icon
        )
    }.sortedByDescending { it.dateTime }
}

@RequiresApi(Build.VERSION_CODES.O)
fun TransactionMock.toTxUi(i: Int): TxUi {
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault())
    val category = subtitle.substringBefore(" • ").trim()
    val amountAbs = amount.filter { it.isDigit() }.toLongOrNull() ?: 0L
    return TxUi(
        id = "mock-$i",
        title = title,
        category = category,
        dateTime = dt,
        amount = amountAbs,
        type = if (isPositive) TxType.INCOME else TxType.EXPENSE,
        emoji = icon
    )
}
