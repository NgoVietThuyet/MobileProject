@file:OptIn(ExperimentalFoundationApi::class)

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
private val IncomeBg   = Color(0xFFDFF3E6)
private val IncomeMain = Color(0xFF2E7D32)
private val ExpenseBg  = Color(0xFFFFE4E6)
private val ExpenseMain= Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
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
    val appBarHeight = 36.dp

    var chatOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = padding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item { HeaderSection(onOpenNotifications = onOpenNotifications) }
            item { Spacer(Modifier.height(16.dp)) }
            item { QuickActionButtons(onAddIncome = onAddIncome, onAddExpense = onAddExpense) }
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
    onOpenNotifications: () -> Unit = {}
) {
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
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                )

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onOpenNotifications),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bell),
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Số dư hiện tại",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = HomeMock.balance,
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    bg = IncomeBg,
                    main = IncomeMain,
                    iconRes = R.drawable.inc,
                    title = "Thu nhập",
                    value = HomeMock.monthlyIncome
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    bg = ExpenseBg,
                    main = ExpenseMain,
                    iconRes = R.drawable.dec,
                    title = "Chi tiêu",
                    value = HomeMock.monthlyExpense
                )
            }
        }
    }
}

/* =============== StatCard =============== */

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    bg: Color,
    main: Color,
    iconRes: Int,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, main.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(main, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, color = main, fontSize = 12.sp)
                    Text(value, color = main, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ===================== Quick Actions ===================== */

@Composable
private fun QuickActionButtons(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onAddIncome,
            modifier = Modifier
                .weight(1f)
                .height(75.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xffe0fce6),
                contentColor = Color(0xff268233)
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
                containerColor = Color(0xfffbe2e2),
                contentColor = Color(0xffb40000)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xffe5e7eb))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tháng này", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Icon(painter = painterResource(R.drawable.increase), contentDescription = null, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryCol("Thu nhập", HomeMock.monthlyIncome, Color(0xff2ba63a))
                SummaryCol("Chi tiêu", HomeMock.monthlyExpense, Color(0xffd80000))
                SummaryCol("Tiết kiệm", HomeMock.monthlySaving, Color(0xff5372fe))
            }
        }
    }
}

@Composable
private fun SummaryCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xff7b8090), fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/* ===================== Budget Card ===================== */

@Composable
private fun BudgetCategoriesCard(onSeeAll: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xffe5e7eb))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ngân sách theo danh mục", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                TextButton(
                    onClick = onSeeAll,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
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
        ) {
            Text(data.icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(data.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Text(data.amount, fontSize = 14.sp, color = Color(0xff7b8090))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xffe5e7eb))
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xffe5e7eb))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Giao dịch gần đây", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                TextButton(onClick = onSeeAll) {
                    Text("Xem tất cả", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(Modifier.height(20.dp))

            items.forEach { tx ->
                TransactionItem(tx)
            }
        }
    }
}

@Composable
private fun TransactionItem(tx: TransactionMock) {
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
                .background(Color(0xfff5f5f5)),
            contentAlignment = Alignment.Center
        ) {
            Text(tx.icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(tx.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(tx.subtitle, fontSize = 12.sp, color = Color(0xff7b8090))
        }

        Text(
            text = tx.amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (tx.isPositive) Color(0xff39c94c) else Color(0xffd80000)
        )
    }
}

/* ===================== Chat Overlay ===================== */

@Composable
private fun ChatAssistButton(
    unread: Int,
    onClick: () -> Unit
) {
    Box {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = Color(0xFF5372FE),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(painter = painterResource(R.drawable.message), contentDescription = "Chat trợ lý")
        }

        if (unread > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(18.dp)
                    .background(Color(0xFFFF3B30), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unread.coerceAtMost(9).toString(),
                    color = Color.White,
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
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
                    .background(Color.White)
                    .zIndex(2f)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF3D73F5))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.message),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Chat bot", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Nhập tin nhắn...",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { }) {
                            Text("Gửi", color = Color(0xFF3D73F5), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotBubble(text: String) {
    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEEF2FF))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text, color = Color(0xFF111827), fontSize = 14.sp)
        }
    }
}

@Composable
private fun MeBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFDCFCE7))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text, color = Color(0xFF064E3B), fontSize = 14.sp)
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    composed {
        pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        }
    }

@Preview(widthDp = 412, heightDp = 900, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}
