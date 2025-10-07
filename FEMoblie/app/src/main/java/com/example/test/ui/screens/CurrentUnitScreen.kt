@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val appBarHeight = 36.dp
    var selected by remember { mutableStateOf(current) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {},
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
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(brush = AppGradient.BluePurple)
                ) {
                    Column(
                        Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(appBarHeight + 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    painter = painterResource(com.example.test.R.drawable.ic_back),
                                    contentDescription = "Quay lại",
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Đơn vị tiền tệ",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Public, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Tiền tệ hiện tại",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    selected.name,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                CurrentCurrencyCard(selected)
                Spacer(Modifier.height(16.dp))
            }
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(com.example.test.R.drawable.ic_star),
                        contentDescription = "Phổ biến",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Phổ biến")
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

            item { Text("Khác", modifier = Modifier.padding(horizontal = 16.dp)) }
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
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
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
                    Text(c.name, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Ví dụ: 42.000 ${c.code}", fontSize = 16.sp, color = Color.Black.copy(alpha = 0.75f))
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
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
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
                if (i != items.lastIndex) Divider(color = Color(0xFFE5E7EB))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.width(44.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selected) { Text("•", fontSize = 18.sp); Spacer(Modifier.width(6.dp)) }
            Text(item.countryCode, fontSize = 16.sp)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontSize = 16.sp)
            Text(item.code, color = Color(0xFF6B7280), fontSize = 12.sp)
        }
        Text(item.symbol, fontSize = 20.sp)
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
