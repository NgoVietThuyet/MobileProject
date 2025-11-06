@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.R
import com.example.test.data.LocalCategoryDataSource
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.theme.AppGradient
import com.example.test.ui.util.MoneyUiConfig
import com.example.test.ui.util.NumberFmt
import com.example.test.vm.ChatViewModel
import com.example.test.vm.CreateTransactionStatus
import com.example.test.vm.HomeBudgetViewModel
import com.example.test.vm.HomeViewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

enum class ChatSender { BOT, USER }
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: ChatSender
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = true,
    unreadChats: Int = 0,
    messages: List<ChatMessage>? = null,
    onSendMessage: ((String) -> Unit)? = null,
    onOpenBudgetAll: () -> Unit = {},
    onAddIncome: () -> Unit = {},
    onAddExpense: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenAllTransactions: () -> Unit = {},
    onReport: () -> Unit = {},
    onSaving: () -> Unit = {},
    onSetting: () -> Unit = {},
    onCamera: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    budgetViewModel: HomeBudgetViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var chatOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isLoggedIn) { if (!isLoggedIn) chatOpen = false }

    val homeState by viewModel.uiState.collectAsStateWithLifecycle()
    val vmMessages by chatViewModel.messages.collectAsStateWithLifecycle()
    val budgetState by budgetViewModel.ui.collectAsStateWithLifecycle()

    val effectiveMessages = messages ?: vmMessages
    val effectiveOnSend: (String) -> Unit = onSendMessage ?: chatViewModel::send

    LaunchedEffect(Unit) {
        viewModel.fetchInitialData()
        budgetViewModel.loadBudgets()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            MainBottomBar(
                selected = BottomTab.HOME,
                onHome = {},
                onReport = onReport,
                onCamera = onCamera,
                onSaving = onSaving,
                onSetting = onSetting
            )
        },
        floatingActionButton = {
            if (isLoggedIn && !chatOpen) {
                ChatAssistButton(unread = unreadChats, onClick = { chatOpen = true })
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
                    bottom = padding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Header
            item {
                HeaderSection(
                    userName = homeState.userName,
                    balance = homeState.balance,
                    onOpenNotifications = onOpenNotifications,
                    onAddIncome = onAddIncome,
                    onAddExpense = onAddExpense
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            // Monthly summary
            item {
                MonthlySummaryCard(
                    monthlyIncome = homeState.monthlyIncome,
                    monthlyExpense = homeState.monthlyExpense
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            // Budgets
            item {
                BudgetCategoriesCard(
                    budgets = budgetState.items,
                    isLoading = budgetState.isLoading,
                    onSeeAll = onOpenBudgetAll
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            // Recent transactions
            item {
                RecentTransactionsCard(
                    transactions = homeState.recentTransactions,
                    isLoading = homeState.isLoading,
                    onSeeAll = onOpenAllTransactions
                )
            }
        }

        // Chat overlay
        ChatOverlay(
            open = isLoggedIn && chatOpen,
            onDismiss = { chatOpen = false },
            bottomInset = padding.calculateBottomPadding(),
            messages = effectiveMessages,
            onSend = effectiveOnSend,
            chatViewModel = chatViewModel
        )
    }
}

@Composable
private fun HeaderSection(
    userName: String?,
    balance: String?,
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
            Spacer(Modifier.height(50.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xin chào\n${userName ?: "..."}",
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
            Spacer(Modifier.height(30.dp))
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
                    Spacer(Modifier.height(12.dp))
                    Text(
                        balance ?: "...",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            QuickActionButtons(
                onAddIncome = onAddIncome,
                onAddExpense = onAddExpense,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

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

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onAddIncome,
            modifier = Modifier
                .weight(1f)
                .height(75.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = incomeBg,
                contentColor = incomeFg
            )
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
                contentColor = expenseFg
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Chi tiêu", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    monthlyIncome: Long,
    monthlyExpense: Long
) {
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
                Text("Tháng này", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Icon(
                    painter = painterResource(R.drawable.increase),
                    contentDescription = null,
                    tint = scheme.primary
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val decimals = MoneyUiConfig.ROUND_DECIMALS_FOR_M
                SummaryCol(
                    "Thu nhập",
                    "+${NumberFmt.millionLabel(NumberFmt.toM(monthlyIncome), decimals)}",
                    scheme.tertiary
                )
                SummaryCol(
                    "Chi tiêu",
                    "-${NumberFmt.millionLabel(NumberFmt.toM(monthlyExpense), decimals)}",
                    scheme.error
                )
                SummaryCol(
                    "Tiết kiệm",
                    NumberFmt.millionLabel(
                        NumberFmt.toM(monthlyIncome - monthlyExpense),
                        decimals
                    ),
                    scheme.primary
                )
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

private fun parseColorOrNull(hex: String?): Color? = try {
    hex?.let { Color(android.graphics.Color.parseColor(it)) }
} catch (_: Exception) { null }

@Composable
private fun BudgetCategoriesCard(
    budgets: List<com.example.test.vm.HomeBudgetItem>,
    isLoading: Boolean,
    onSeeAll: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ngân sách theo danh mục",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )
                TextButton(
                    onClick = onSeeAll,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)
                ) {
                    Text(
                        text = "Xem tất cả",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            when {
                isLoading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                budgets.isEmpty() -> Text(
                    "Chưa có ngân sách nào",
                    color = scheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                else -> Column {
                    budgets.take(4).forEachIndexed { index, item ->
                        BudgetCategoryItem(item)
                        if (index != budgets.take(4).lastIndex) {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetCategoryItem(
    item: com.example.test.vm.HomeBudgetItem
) {
    val scheme = MaterialTheme.colorScheme

    val color = parseColorOrNull(item.categoryColor) ?: scheme.primary
    val decimals = MoneyUiConfig.ROUND_DECIMALS_FOR_M
    val total = item.initialAmount.filter { it.isDigit() }.toFloatOrNull() ?: 0f
    val used = item.currentAmount.filter { it.isDigit() }.toFloatOrNull() ?: 0f
    val progress = if (total > 0f) used / total else 0f

    val amountLabel = "${NumberFmt.millionLabel(NumberFmt.toM(used.toLong()), decimals)} / " +
            "${NumberFmt.millionLabel(NumberFmt.toM(total.toLong()), decimals)}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                item.categoryIcon ?: "💰",
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.categoryName ?: "Danh mục",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )
                Text(
                    amountLabel,
                    fontSize = 14.sp,
                    color = scheme.onSurfaceVariant
                )
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
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RecentTransactionsCard(
    transactions: List<TxUi>,
    isLoading: Boolean,
    onSeeAll: () -> Unit
) {
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
                Text("Giao dịch gần đây", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                TextButton(
                    onClick = onSeeAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)
                ) { Text("Xem tất cả", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(20.dp))
            when {
                isLoading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                transactions.isEmpty() -> {
                    Text("Không có giao dịch gần đây.", color = scheme.onSurfaceVariant)
                }

                else -> transactions.forEach { tx -> TransactionItem(tx) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TransactionItem(tx: TxUi) {
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
        ) { Text(tx.emoji ?: "💰", fontSize = 18.sp) }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(tx.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface)
            Text(tx.category, fontSize = 12.sp, color = scheme.onSurfaceVariant)
        }

        val isPositive = tx.type == TxType.INCOME
        val formattedAmount =
            (if (isPositive) "+" else "-") + vn(tx.amount.toLongOrNull() ?: 0L)

        Text(
            text = formattedAmount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) scheme.tertiary else scheme.error
        )
    }
}

private fun vn(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        .format(value)
        .replace(" ", "")

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
    onDismiss: () -> Unit,
    bottomInset: Dp,
    messages: List<ChatMessage>,
    onSend: (String) -> Unit,
    chatViewModel: ChatViewModel
) {
    val scheme = MaterialTheme.colorScheme
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val contentBottomPadding = remember(imeBottom, bottomInset, navBottom) {
        if (imeBottom > (bottomInset + navBottom)) imeBottom else (bottomInset + navBottom)
    }
    var input by rememberSaveable(open) { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(scheme.scrim.copy(alpha = 0.35f))
                    .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
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
                    .fillMaxHeight(0.96f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(scheme.surface)
                    .zIndex(2f)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = contentBottomPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(scheme.primary)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.message),
                            contentDescription = null,
                            tint = scheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Chat bot",
                            color = scheme.onPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng chat",
                                tint = scheme.onPrimary
                            )
                        }
                    }

                    val listState = rememberLazyListState()
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                WelcomeMessage()
                            }
                        } else {
                            items(messages, key = { it.id }) { m ->
                                if (m.sender == ChatSender.BOT) BotBubble(m.text, chatViewModel) else MeBubble(m.text)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(scheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("Nhập tin nhắn...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            )
                        )
                        TextButton(
                            onClick = {
                                val text = input.trim()
                                if (text.isNotEmpty()) {
                                    onSend(text)
                                    input = ""
                                }
                            },
                            enabled = input.isNotBlank()
                        ) { Text("Gửi", color = scheme.primary, fontSize = 14.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeMessage() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.message),
                contentDescription = null,
                tint = scheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Xin chào! 👋",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tôi có thể giúp gì cho bạn?",
            fontSize = 14.sp,
            color = scheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Hãy nhập giao dịch của bạn, ví dụ:\n\"Mua cà phê 50000\"",
            fontSize = 12.sp,
            color = scheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun TypingIndicator() {
    val scheme = MaterialTheme.colorScheme
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dotCount = (dotCount % 3) + 1
        }
    }

    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < dotCount) scheme.onPrimaryContainer
                        else scheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
private fun BotBubble(
    text: String,
    chatViewModel: ChatViewModel? = null
) {
    val scheme = MaterialTheme.colorScheme

    // Check if this is a typing indicator
    if (text == "Đang soạn trả lời..." || text.contains("...")) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                TypingIndicator()
            }
        }
        return
    }

    // Try to parse as transaction JSON
    val transactionData = remember(text) {
        android.util.Log.d("ChatBot", "Attempting to parse: [$text]")
        parseTransactionJson(text)
    }

    if (transactionData != null && chatViewModel != null) {
        TransactionChatCard(transactionData, chatViewModel)
    } else {
        // Display as normal text bubble
        Row(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = text,
                    color = scheme.onPrimaryContainer,
                    fontSize = 14.sp
                )
            }
        }
    }
}

data class TransactionData(
    val amount: String,
    val category: String,
    val categoryId: String?,
    val note: String?,
    val type: String,
    val emoji: String?
)

private fun parseTransactionJson(text: String): TransactionData? {
    return try {
        var trimmed = text.trim()

        // Handle escaped JSON (e.g., "{\"amount\":\"20000\"}")
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            // Remove outer quotes and unescape
            trimmed = trimmed.substring(1, trimmed.length - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        }

        // Try to extract JSON from text that might contain additional info
        val jsonStart = trimmed.indexOf("{")
        val jsonEnd = trimmed.lastIndexOf("}")

        if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) return null

        val jsonText = trimmed.substring(jsonStart, jsonEnd + 1)
        val json = org.json.JSONObject(jsonText)

        // Validate required fields
        if (!json.has("amount") && !json.has("category")) {
            android.util.Log.d("ChatBot", "JSON missing required fields: $jsonText")
            return null
        }

        val amount = json.optString("amount", "")
        val category = json.optString("category", "")

        // Make sure at least one of amount or category is present
        if (amount.isBlank() && category.isBlank()) {
            android.util.Log.d("ChatBot", "JSON has empty required fields")
            return null
        }

        android.util.Log.d("ChatBot", "Successfully parsed transaction: amount=$amount, category=$category")

        TransactionData(
            amount = amount,
            category = category,
            categoryId = json.optString("categoryId").takeIf { it.isNotBlank() },
            note = json.optString("note").takeIf { it.isNotBlank() },
            type = json.optString("type", "expense"),
            emoji = json.optString("emoji", "💰").takeIf { it.isNotBlank() }
        )
    } catch (e: Exception) {
        android.util.Log.e("ChatBot", "Failed to parse transaction JSON from text: [$text]", e)
        null
    }
}

@Composable
private fun TransactionChatCard(
    data: TransactionData,
    chatViewModel: ChatViewModel
) {
    val scheme = MaterialTheme.colorScheme
    val createStatus by chatViewModel.createTransactionStatus.collectAsStateWithLifecycle()

    // ===== Resolve danh mục từ ID/Name -> icon + tên hiển thị =====
    val catDto = remember(data.category, data.categoryId) {
        // Ưu tiên dùng categoryId; nếu backend chỉ trả "category" là ID (vd: "8") thì lấy luôn
        val idGuess = data.categoryId?.takeIf { it.isNotBlank() } ?: data.category
        LocalCategoryDataSource.find(idGuess)
            ?: LocalCategoryDataSource.findByName(data.category)
            ?: LocalCategoryDataSource.getDefaultCategory()
    }
    val displayName = catDto.name           // ví dụ: "Ăn uống"
    val displayIcon = catDto.icon           // ví dụ: "🍜"
    val resolvedCategoryId = catDto.categoryId // ví dụ: "8"

    val isIncome = data.type.equals("income", ignoreCase = true) ||
            data.type.equals("INCOME", ignoreCase = true)

    // ===== State cục bộ cho từng thẻ =====
    var isAdded by remember(data) { mutableStateOf(false) }
    var submitted by remember(data) { mutableStateOf(false) }
    var isLoading by remember(data) { mutableStateOf(false) }

    // Chỉ nhận kết quả cho đúng thẻ đã bấm
    LaunchedEffect(createStatus) {
        if (!submitted) return@LaunchedEffect
        when (createStatus) {
            is CreateTransactionStatus.Success -> {
                isAdded = true
                isLoading = false
                submitted = false
                chatViewModel.resetCreateTransactionStatus()
            }
            is CreateTransactionStatus.Error -> {
                isLoading = false
                submitted = false
                chatViewModel.resetCreateTransactionStatus()
            }
            else -> Unit
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.message),
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Giao dịch được đề xuất",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onPrimaryContainer
                )
            }

            // Nội dung giao dịch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = scheme.surfaceVariant,
                    modifier = Modifier.size(40.dp),
                    contentColor = scheme.onSurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(displayIcon, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        displayName, // ← tên danh mục đã resolve (vd: Ăn uống)
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = scheme.onSurface
                    )
                    if (!data.note.isNullOrBlank()) {
                        Text(
                            text = data.note,
                            fontSize = 12.sp,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                val amtColor = if (isIncome) scheme.tertiary else scheme.error
                val amountLong = data.amount.toLongOrNull() ?: 0L
                Text(
                    text = (if (isIncome) "+" else "-") + vn(amountLong),
                    color = amtColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nút thêm / trạng thái đã thêm
            if (!isAdded) {
                Button(
                    onClick = {
                        submitted = true
                        isLoading = true
                        chatViewModel.createTransaction(
                            amount = data.amount,
                            categoryId = resolvedCategoryId, // ← ID đã resolve (vd: "8")
                            categoryName = displayName,      // ← Tên đã resolve (vd: "Ăn uống")
                            note = data.note,
                            type = data.type
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = scheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Đang thêm...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Thêm giao dịch", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.tertiaryContainer)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = scheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Đã thêm vào giao dịch",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onTertiaryContainer
                    )
                }
            }
        }
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
