@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.test.ui.components.AppHeader
import com.example.test.ui.theme.AppGradient

data class CurrencyItem(
    val countryCode: String,
    val name: String,
    val code: String,
    val symbol: String
)

@Composable
fun CurrencyUnitScreen(
    onBack: () -> Unit = {},
    current: CurrencyItem = vn,
    onSelect: (CurrencyItem) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var selected by remember { mutableStateOf(current) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Chọn đơn vị tiền tệ",
                showBack = true,
                onBack = onBack
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            )
        ) {

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Đơn vị tiền tệ hiện tại",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = scheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                CurrentCurrencyCard(selected)
                Spacer(Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = "Phổ biến",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Phổ biến", color = scheme.onSurface)
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                CurrencyListCard(
                    items = popular,
                    selectedCode = selected.code,
                    onSelect = { it -> selected = it; onSelect(it) }
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text("Khác", modifier = Modifier.padding(horizontal = 16.dp), color = scheme.onSurface)
            }
            item {
                Spacer(Modifier.height(8.dp))
                CurrencyListCard(
                    items = others,
                    selectedCode = selected.code,
                    onSelect = { it -> selected = it; onSelect(it) }
                )
            }
        }
    }
}

@Composable
private fun CurrentCurrencyCard(item: CurrencyItem) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Crossfade(targetState = item, label = "currency-card") { c ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AppGradient.BluePurple),
                        contentAlignment = Alignment.Center
                    ) { Text("đ", color = Color.White, fontSize = 22.sp) }

                    Spacer(Modifier.height(12.dp))
                    Text(c.name, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text("Ví dụ: 42.000 ${c.code}", fontSize = 16.sp, color = scheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CurrencyListCard(
    items: List<CurrencyItem>,
    selectedCode: String,
    onSelect: (CurrencyItem) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column {
            items.forEachIndexed { i, it ->
                CurrencyRow(
                    item = it,
                    selected = it.code == selectedCode,
                    onClick = { onSelect(it) }
                )
                if (i != items.lastIndex) Divider(color = scheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    item: CurrencyItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.width(44.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selected) { Text("•", fontSize = 18.sp, color = scheme.primary); Spacer(Modifier.width(6.dp)) }
            Text(item.countryCode, fontSize = 16.sp, color = scheme.onSurface)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontSize = 16.sp, color = scheme.onSurface)
            Text(item.code, color = scheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Text(item.symbol, fontSize = 20.sp, color = scheme.onSurface)
    }
}

private val vn = CurrencyItem("VN", "Đồng Việt Nam", "VND", "₫")
private val us = CurrencyItem("US", "Đô la Mỹ", "USD", "$")
private val eu = CurrencyItem("EU", "Euro", "EUR", "€")
private val jp = CurrencyItem("JP", "Yên Nhật", "JPY", "¥")
private val kr = CurrencyItem("KR", "Won Hàn Quốc", "KRW", "₩")
private val cn = CurrencyItem("CN", "Nhân dân tệ", "CNY", "¥")
private val gb = CurrencyItem("GB", "Bảng Anh", "GBP", "£")
private val th = CurrencyItem("TH", "Bath Thái", "THB", "฿")
private val popular = listOf(vn, us, eu, jp)
private val others = listOf(kr, cn, gb, th)
