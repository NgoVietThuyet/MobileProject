@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.test.R
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.mock.BudgetCategoryMock
import com.example.test.ui.mock.MockData as HomeMock
import com.example.test.ui.mock.TransactionMock
import com.example.test.ui.theme.AppGradient

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenBudgetAll: () -> Unit = {},
    onAddIncome: () -> Unit = {},
    onAddExpense: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenAllTransactions: () -> Unit = {},
    onReport: () -> Unit = {},
    onSaving: () -> Unit = {},
    onSetting: () -> Unit = {},
    onCamera: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var chatOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            MainBottomBar(
                selected = BottomTab.HOME,
                onHome = { },
                onReport = onReport,
                onCamera = onCamera,
                onSaving = onSaving,
                onSetting = onSetting,
            )
        },
        floatingActionButton = {
            if (!chatOpen) {
                ChatAssistButton(
                    unread = HomeMock.unreadChats,
                    onClick = { chatOpen = true }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        val scheme = MaterialTheme.colorScheme

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = padding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                HeaderSection(
                    onOpenNotifications = onOpenNotifications,
                    onAddIncome = onAddIncome,
                    onAddExpense = onAddExpense
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
            item { MonthlySummaryCard() }
            item { Spacer(Modifier.height(24.dp)) }
            item { BudgetCategoriesCard(onSeeAll = onOpenBudgetAll) }
            item { Spacer(Modifier.height(24.dp)) }
            item { RecentTransactionsCard(onSeeAll = onOpenAllTransactions) }
        }

        ChatOverlay(open = chatOpen, onDismiss = { chatOpen = false })
    }
}

/* ===================== Header ===================== */

@Composable
private fun HeaderSection(
    onOpenNotifications: () -> Unit = {},
    onAddIncome: () -> Unit = {},
    onAddExpense: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(brush = AppGradient.BluePurple)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xin chào\n${HomeMock.greetingName}",
                    color = scheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                )

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onOpenNotifications),
                    shape = CircleShape,
                    color = scheme.onPrimary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, scheme.onPrimary.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bell),
                            contentDescription = "Notifications",
                            tint = scheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Số dư hiện tại",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        HomeMock.balance,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            QuickActionButtons(
                onAddIncome = onAddIncome,
                onAddExpense = onAddExpense,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ===================== Quick Actions ===================== */

@Composable
private fun QuickActionButtons(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {

    val incomeBg = Color(0xFFE0FCE6)
    val incomeFg = Color(0xFF268233)
    val expenseBg = Color(0xFFFBE2E2)
    val expenseFg = Color(0xFFB40000)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onAddIncome,
            modifier = Modifier
                .weight(1f)
                .height(75.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = incomeBg,
                contentColor = incomeFg,
                disabledContainerColor = incomeBg.copy(alpha = 0.6f),
                disabledContentColor = incomeFg.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Thu nhập", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .weight(1f)
                .height(75.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = expenseBg,
                contentColor = expenseFg,
                disabledContainerColor = expenseBg.copy(alpha = 0.6f),
                disabledContentColor = expenseFg.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Chi tiêu", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* ===================== Monthly Summary ===================== */

@Composable
private fun MonthlySummaryCard() {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tháng này", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                Icon(painter = painterResource(R.drawable.increase), contentDescription = null, tint = scheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryCol("Thu nhập", HomeMock.monthlyIncome, MaterialTheme.colorScheme.tertiary)
                SummaryCol("Chi tiêu", HomeMock.monthlyExpense, MaterialTheme.colorScheme.error)
                SummaryCol("Tiết kiệm", HomeMock.monthlySaving, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SummaryCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/* ===================== Budget Card ===================== */

@Composable
private fun BudgetCategoriesCard(onSeeAll: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ngân sách theo danh mục", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                TextButton(
                    onClick = onSeeAll,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)
                ) {
                    Text("Xem tất cả", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HomeMock.budgetCategories.forEachIndexed { index, item ->
                BudgetCategoryItem(item)
                if (index < HomeMock.budgetCategories.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun BudgetCategoryItem(data: BudgetCategoryMock) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(data.color),
            contentAlignment = Alignment.Center
        ) { Text(data.icon, fontSize = 18.sp) }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(data.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                Text(data.amount, fontSize = 14.sp, color = scheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(scheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(data.progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(data.color)
                )
            }
        }
    }
}

/* ===================== Transactions ===================== */

@Composable
private fun RecentTransactionsCard(onSeeAll: () -> Unit = {}) {
    val items = remember(HomeMock.recentTransactions) {
        HomeMock.recentTransactions
            .sortedByDescending { it.createdAt }
            .take(4)
    }
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Giao dịch gần đây", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                TextButton(onClick = onSeeAll, colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)) {
                    Text("Xem tất cả", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            items.forEach { tx -> TransactionItem(tx) }
        }
    }
}

@Composable
private fun TransactionItem(tx: TransactionMock) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(scheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Text(tx.icon, fontSize = 18.sp) }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(tx.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
            Text(tx.subtitle, fontSize = 12.sp, color = scheme.onSurfaceVariant)
        }

        Text(
            text = tx.amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (tx.isPositive) scheme.tertiary else scheme.error
        )
    }
}

/* ===================== Chat Overlay ===================== */

@Composable
private fun ChatAssistButton(
    unread: Int,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier.size(56.dp)
        ) { Icon(painter = painterResource(R.drawable.message), contentDescription = "Chat trợ lý") }

        if (unread > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(18.dp)
                    .background(scheme.error, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unread.coerceAtMost(9).toString(),
                    color = scheme.onError,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChatOverlay(
    open: Boolean,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(scheme.scrim.copy(alpha = 0.35f))
                    .noRippleClickable { onDismiss() }
                    .zIndex(1f)
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(scheme.surface)
                    .zIndex(2f)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(scheme.primary)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(R.drawable.message), contentDescription = null, tint = scheme.onPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("Chat bot", color = scheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        items(8) { i ->
                            if (i % 2 == 0) BotBubble("Chào ${HomeMock.greetingName} 👋")
                            else MeBubble("Cho mình xem báo cáo tháng này.")
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(scheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nhập tin nhắn...", color = scheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { }) { Text("Gửi", color = scheme.primary, fontSize = 14.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotBubble(text: String) {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.primaryContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) { Text(text, color = scheme.onPrimaryContainer, fontSize = 14.sp) }
    }
}

@Composable
private fun MeBubble(text: String) {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.tertiaryContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) { Text(text, color = scheme.onTertiaryContainer, fontSize = 14.sp) }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    composed { pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) } }

@Preview(widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}
