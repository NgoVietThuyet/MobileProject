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
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.theme.AppGradient
import com.example.test.ui.util.MoneyUiConfig
import com.example.test.ui.util.NumberFmt
import com.example.test.vm.ChatViewModel
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
            onSend = effectiveOnSend
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
    onSend: (String) -> Unit
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
                        Text("Chat bot", color = scheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
                                Text(
                                    "Chưa có tin nhắn.",
                                    color = scheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            items(messages, key = { it.id }) { m ->
                                if (m.sender == ChatSender.BOT) BotBubble(m.text) else MeBubble(m.text)
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
