@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.R
import com.example.test.data.SavingGoalCategories
import com.example.test.ui.components.AppHeader
import com.example.test.vm.SavingDetailViewModel
import com.example.test.vm.SavingsViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
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
                Log.e("SavingDetailScreen", "Failed to parse deadline with multiple formats: $dateStr", e2)
            }
        } else {
            Log.e("SavingDetailScreen", "Slash formatter is null, could not attempt parsing $dateStr with slashes.")
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
    return format.format(value).replace(" ₫", "đ").replace("₫", "đ")
}

private fun getGoalEmoji(categoryId: String?): String {
    return SavingGoalCategories.findIconById(categoryId)
}

private fun getGoalColor(categoryId: String?): Color {
    return Color(0xFF4A90E2)
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SavingDetailScreen(
    goalId: String?,
    onNavigateBack: (refreshNeeded: Boolean) -> Unit,
    onDeleteSuccess: () -> Unit = {},
    detailViewModel: SavingDetailViewModel = hiltViewModel(),
    savingsViewModel: SavingsViewModel = hiltViewModel(),
    onBack:() -> Unit = {},
) {
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val savingsUiState by savingsViewModel.uiState.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val appBarHeight = 36.dp

    val goal = remember(goalId, savingsUiState.savingGoals) {
        savingsUiState.savingGoals.firstOrNull { it.savingGoalId.equals(goalId, ignoreCase = true) }
    }

    val initialSaved = remember(goal?.currentAmount) { parseMoneyStringToLong(goal?.currentAmount) }
    val totalTarget = remember(goal?.targetAmount) { parseMoneyStringToLong(goal?.targetAmount) }
    var currentSavedDisplay by rememberSaveable(initialSaved) { mutableStateOf(initialSaved) }
    var input by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialSaved) {
        currentSavedDisplay = initialSaved
    }

    val progress = if (totalTarget > 0 && goal != null) (currentSavedDisplay.toDouble() / totalTarget).coerceIn(0.0, 1.0) else 0.0
    val pct = (progress * 100).roundToInt()
    val daysRemain = calculateDaysRemaining(parseDeadlineString(goal?.deadline))
    val goalEmoji = getGoalEmoji(goal?.categoryId)
    val goalColor = getGoalColor(goal?.categoryId)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(detailUiState.updateAmountError) {
        detailUiState.updateAmountError?.let {
            snackbarHostState.showSnackbar("Lỗi cập nhật: $it", duration = SnackbarDuration.Short)
            detailViewModel.clearStatusFlags()
        }
    }

    LaunchedEffect(detailUiState.deleteError) {
        detailUiState.deleteError?.let {
            snackbarHostState.showSnackbar("Lỗi xóa: $it", duration = SnackbarDuration.Short)
            detailViewModel.clearStatusFlags()
        }
    }

    LaunchedEffect(detailUiState.updateAmountSuccess) {
        if (detailUiState.updateAmountSuccess) {
            Log.d("SavingDetailScreen", "Update amount success, signaling refresh.")
            onNavigateBack(true)
            detailViewModel.clearStatusFlags()
        }
    }

    LaunchedEffect(detailUiState.deleteSuccess) {
        if (detailUiState.deleteSuccess) {
            Log.d("SavingDetailScreen", "Delete success, navigating back.")
            onDeleteSuccess()
            detailViewModel.clearStatusFlags()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Chi tiết mục tiêu",
                showBack = true,
                onBack = { onNavigateBack(false) },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(scheme.background).padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (goal == null && goalId != null && savingsUiState.isLoading) {
                    Spacer(Modifier.height(100.dp))
                    CircularProgressIndicator()
                } else if (goal == null && goalId != null && !savingsUiState.isLoading) {
                    Spacer(Modifier.height(100.dp))
                    Text("Không tìm thấy thông tin mục tiêu.")
                } else if (goal != null) {
                    Spacer(Modifier.height(8.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, scheme.outlineVariant),
                        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(goalColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(goalEmoji, fontSize = 24.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = goal.title ?: "Mục tiêu không xác định",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = scheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "${formatVnd(currentSavedDisplay)} / ${formatVnd(totalTarget)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = scheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            ContinuousLinearProgress(
                                progress = progress.toFloat(),
                                color = goalColor,
                                trackColor = scheme.surfaceVariant,
                                height = 10.dp,
                                corner = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$pct% hoàn thành",
                                    fontSize = 14.sp,
                                    color = scheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "$daysRemain ngày",
                                        fontSize = 14.sp,
                                        color = if (daysRemain < 0) scheme.error else scheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, scheme.outlineVariant),
                        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Cập nhật số tiền",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = scheme.onSurface
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = input,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        input = newValue
                                    }
                                },
                                label = { Text("Số tiền") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !detailUiState.isUpdatingAmount && !detailUiState.isDeleting
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val delta = input.toLongOrNull() ?: 0L
                                        if (delta > 0) {
                                            val newAmount = (currentSavedDisplay + delta).coerceAtMost(totalTarget)
                                            detailViewModel.updateAmount(goal.savingGoalId, delta)
                                            currentSavedDisplay = newAmount
                                            input = ""
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    enabled = input.isNotBlank() && !detailUiState.isUpdatingAmount && !detailUiState.isDeleting
                                ) {
                                    val isAddingAmount = (input.toLongOrNull() ?: 0L) > 0
                                    if (detailUiState.isUpdatingAmount && isAddingAmount) {
                                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Add, contentDescription = "Thêm tiền", tint=Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Thêm tiền", color=Color.White)
                                    }
                                }
                                Button(
                                    onClick = {
                                        val delta = input.toLongOrNull() ?: 0L
                                        if (delta > 0) {
                                            val newAmount = (currentSavedDisplay - delta).coerceAtLeast(0L)
                                            detailViewModel.updateAmount(goal.savingGoalId, -delta)
                                            currentSavedDisplay = newAmount
                                            input = ""
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                                    enabled = input.isNotBlank() && !detailUiState.isUpdatingAmount && !detailUiState.isDeleting
                                ) {
                                    val isSubtractingAmount = (input.toLongOrNull() ?: 0L) > 0
                                    if (detailUiState.isUpdatingAmount && isSubtractingAmount) {
                                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(22.dp)) {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("—", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text("Rút ra", color=Color.White, fontSize = 16.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Số dư hiện tại: " + formatVnd(currentSavedDisplay),
                                fontSize = 13.sp,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { detailViewModel.deleteGoal(goal.savingGoalId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
                        border = BorderStroke(1.dp, scheme.error),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !detailUiState.isDeleting && !detailUiState.isUpdatingAmount,
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (detailUiState.isDeleting) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = scheme.error, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Delete, null, tint = scheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text("Xóa mục tiêu")
                        }
                    }

                } else {
                    Spacer(Modifier.height(100.dp))
                    Text("ID mục tiêu không hợp lệ hoặc không tìm thấy.")
                }
            }
        }
    }
}
@Composable
private fun ContinuousLinearProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    height: Dp,
    corner: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(color)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatM(v: Float): String {
    if (v == 0f) return "0.0M"
    val r = (kotlin.math.round(v * 10f) / 10f)
    val s = if (r % 1f == 0f) r.toInt().toString() else String.format(java.util.Locale.US, "%.1f", r)
    return "${s}M"
}