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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.theme.extendedColors
import java.text.NumberFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

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

    val cs = MaterialTheme.colorScheme
    val ex = MaterialTheme.extendedColors

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Tất cả giao dịch",
                showBack = true,
                onBack = onBack
            )
        },
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
    val cs = MaterialTheme.colorScheme
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cs.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = cs.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            txs.forEachIndexed { i, tx ->
                TxRowLine(
                    tx = tx,
                    onClick = { if (tx.type == TxType.INCOME) onEditIncome(tx) else onEditExpense(tx) }
                )
                if (i != txs.lastIndex) Divider(color = cs.outlineVariant)
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
    val cs = MaterialTheme.colorScheme
    val ex = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = cs.surfaceVariant,
            modifier = Modifier.size(40.dp),
            contentColor = cs.onSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    tx.emoji != null -> Text(tx.emoji, fontSize = 18.sp)
                    tx.iconRes != null -> Icon(painter = painterResource(tx.iconRes), contentDescription = null)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = cs.onSurface)
            Text(
                "${tx.category} • ${tx.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                fontSize = 12.sp,
                color = cs.onSurfaceVariant
            )
        }
        val amtColor = if (tx.type == TxType.INCOME) ex.success else cs.error
        Text(
            text = (if (tx.type == TxType.INCOME) "+" else "-") + vn(tx.amount),
            color = amtColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SegmentTab(text: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontWeight = FontWeight.Medium) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = cs.surface,
            labelColor = cs.onSurface,
            selectedContainerColor = cs.primary,
            selectedLabelColor = cs.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color.Transparent else cs.outline,
            borderWidth = if (selected) 0.dp else 1.dp
        )
    )
}

@Composable
private fun SummaryCard(allIncome: Long, allExpense: Long) {
    val cs = MaterialTheme.colorScheme
    val ex = MaterialTheme.extendedColors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        border = BorderStroke(1.dp, cs.outline),
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
                    Icon(painterResource(R.drawable.inc), null, tint = ex.success)
                    Spacer(Modifier.width(6.dp))
                    Text("Thu nhập", fontWeight = FontWeight.Medium, color = cs.onSurface)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.dec), null, tint = cs.error)
                    Spacer(Modifier.width(6.dp))
                    Text("Chi tiêu", fontWeight = FontWeight.Medium, color = cs.onSurface)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "+${vn(allIncome)}",
                    color = ex.success,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
                Text(
                    "-${vn(allExpense)}",
                    color = cs.error,
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
    val cs = MaterialTheme.colorScheme
    val label = when (date) {
        LocalDate.now() -> "Hôm nay"
        LocalDate.now().minusDays(1) -> "Hôm qua"
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.background)
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = null, tint = cs.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = cs.onSurfaceVariant)
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            color = cs.outlineVariant
        )
    }
}

private fun vn(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value).replace(" ", "")
