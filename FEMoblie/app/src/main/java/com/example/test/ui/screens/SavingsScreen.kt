@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.example.test.R
import com.example.test.data.SavingGoalCategories
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.models.SavingGoalDto
import com.example.test.ui.theme.AppGradient
import com.example.test.vm.SavingsViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

private fun parseMoneyStringToLong(amountStr: String?): Long {
    if (amountStr.isNullOrBlank()) return 0L
    return amountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseDeadlineString(dateStr: String?): LocalDate? {
    if (dateStr.isNullOrBlank()) return null
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val slashFormatter = try { DateTimeFormatter.ofPattern("yyyy/MM/dd") } catch (e: Exception) { null }

    try {
        return LocalDate.parse(dateStr, isoFormatter)
    } catch (e: DateTimeParseException) {
        if (slashFormatter != null) {
            try {
                return LocalDate.parse(dateStr, slashFormatter)
            } catch (e2: DateTimeParseException) {
                Log.e("SavingsScreen", "Failed to parse deadline: $dateStr", e2)
            }
        }
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateDaysRemaining(deadline: LocalDate?): Long {
    if (deadline == null) return 0L
    val today = LocalDate.now()
    return ChronoUnit.DAYS.between(today, deadline).coerceAtLeast(0L)
}

private fun formatVnd(value: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(value).replace(" ₫", "đ").replace("₫", "đ")
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SavingsScreen(
    onHome: () -> Unit = {},
    onReport: () -> Unit = {},
    onAddGoal: () -> Unit = {},
    onSettings: () -> Unit = {},
    onCamera: () -> Unit = {},
    onSaving: () -> Unit = {},
    onGoalClick: (String) -> Unit = {},
    viewModel: SavingsViewModel = hiltViewModel(),
    backStackEntry: NavBackStackEntry
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(backStackEntry.savedStateHandle) {
        val shouldRefresh = backStackEntry.savedStateHandle
            .get<Boolean>("should_refresh_savings")
        if (shouldRefresh == true) {
            Log.d("SavingsScreen", "Refreshing goals...")
            viewModel.loadGoals()
            backStackEntry.savedStateHandle.remove<Boolean>("should_refresh_savings")
        }
    }

    val createdDateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss") }
    val goalGroups = remember(uiState.savingGoals) {
        uiState.savingGoals
            .mapNotNull { goal ->
                val date = try {
                    LocalDateTime.parse(goal.createdDate, createdDateFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("SavingsScreen", "Failed to parse createdDate: ${goal.createdDate}", e)
                    null
                }
                date?.let { Pair(it, goal) }
            }
            .groupBy({ it.first }, { it.second })
            .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            MainBottomBar(
                selected = BottomTab.SAVING,
                onHome = onHome,
                onReport = onReport,
                onCamera = onCamera,
                onSaving = onSaving,
                onSetting = onSettings
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(brush = AppGradient.BluePurple)
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(appBarHeight + 12.dp))
                        Text("Tiết kiệm", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Xây dựng tương lai tài chính 👋", color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tổng tiết kiệm", color = Color.White, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Outlined.ArrowOutward, null, tint = Color.White)
                                }
                                Spacer(Modifier.height(10.dp))
                                val totalSaved = remember(uiState.savingGoals) {
                                    uiState.savingGoals.sumOf { parseMoneyStringToLong(it.currentAmount) }
                                }
                                Text(formatVnd(totalSaved), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mục tiêu tiết kiệm", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = scheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = onAddGoal,
                        colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Thêm mục tiêu")
                        Spacer(Modifier.width(6.dp))
                        Text("Thêm")
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                uiState.error != null -> {
                    item {
                        Text(
                            text = "Lỗi: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                goalGroups.isEmpty() -> {
                    item {
                        Text(
                            text = "Chưa có mục tiêu tiết kiệm nào.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    goalGroups.forEach { (date, goalsInDay) ->
                        item { DateHeaderRow(date = date) }
                        items(
                            items = goalsInDay,
                            key = { goal -> goal.savingGoalId ?: goal.hashCode() }
                        ) { goalDto ->
                            SavingGoalItem(
                                g = goalDto,
                                onClick = {
                                    Log.d("SavingsScreen", "Goal clicked: ${goalDto.title}, ID: ${goalDto.savingGoalId}")
                                    goalDto.savingGoalId?.let {
                                        onGoalClick(it)
                                    } ?: Log.e("SavingsScreen", "savingGoalId is NULL!")
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(18.dp)) }
            item {
                Text(
                    "Mục tiêu phổ biến",
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Medium,
                    color = scheme.onBackground
                )
            }
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(12.dp)) {
                            SuggestTileView("🏠", "mua nhà", "2–5 tỷ", modifier = Modifier.weight(1f))
                            SuggestTileView("🚗", "mua xe", "500 Tr–2 tỷ", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(12.dp)) {
                            SuggestTileView("✈️", "du lịch", "10–50 Tr", modifier = Modifier.weight(1f))
                            SuggestTileView("🆘", "khẩn cấp", "3–6 tháng lương", danger = true, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_calendar),
            contentDescription = null,
            tint = cs.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = cs.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SavingGoalItem(
    g: SavingGoalDto,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    val savedAmount = parseMoneyStringToLong(g.currentAmount)
    val targetAmount = parseMoneyStringToLong(g.targetAmount)
    val deadlineDate = parseDeadlineString(g.deadline)
    val daysRemain = calculateDaysRemaining(deadlineDate)

    val progress = if (targetAmount > 0L) (savedAmount.toDouble() / targetAmount).coerceIn(0.0, 1.0) else 0.0
    val pct = (progress * 100).toInt()

    val goalEmoji = SavingGoalCategories.findIconById(g.categoryId)
    val goalColor = Color(0xFF4A90E2)

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(goalColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(goalEmoji, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(g.title ?: "Mục tiêu", fontWeight = FontWeight.Medium, color = scheme.onSurface)
                    Text(
                        "${formatVnd(savedAmount)} / ${formatVnd(targetAmount)}",
                        color = scheme.onSurfaceVariant, fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            ContinuousLinearProgress(
                progress = progress.toFloat(),
                color = goalColor,
                trackColor = scheme.surfaceVariant,
                height = 8.dp,
                corner = 6.dp
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$pct% hoàn thành", color = scheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("$daysRemain ngày", color = scheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SuggestTileView(
    icon: String,
    title: String,
    sub: String,
    danger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        modifier = modifier.heightIn(min = 96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = scheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(icon) }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = scheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = sub,
                color = if (danger) scheme.error else scheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ContinuousLinearProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    height: Dp,
    corner: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(color)
        )
    }
}