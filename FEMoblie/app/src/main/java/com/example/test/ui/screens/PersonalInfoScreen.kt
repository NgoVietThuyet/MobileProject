@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.theme.AppGradient
import com.example.test.vm.ProfileViewModel
import com.example.test.vm.SaveStatus
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PersonalInfoScreen(
    onBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var fullName by remember(uiState.currentName) { mutableStateOf(uiState.currentName) }
    var phone by remember(uiState.currentPhone) { mutableStateOf(uiState.currentPhone) }
    val email = uiState.currentEmail

    var address by rememberSaveable { mutableStateOf("") }
    var birthdayMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var jobTitle by rememberSaveable { mutableStateOf("Kỹ sư phần mềm") }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = birthdayMillis)

    val canSave = uiState.saveStatus != SaveStatus.LOADING

    LaunchedEffect(uiState.saveStatus) {
        when (uiState.saveStatus) {
            SaveStatus.SUCCESS -> {
                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
                onBack()
            }
            SaveStatus.ERROR -> {
                Toast.makeText(context, "Lỗi: ${uiState.errorMessage}", Toast.LENGTH_LONG).show()
                viewModel.resetStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Chỉnh sửa thông tin cá nhân",
                showBack = true,
                onBack = onBack,
                actions = {
                    SaveButton(
                        onClick = {
                            viewModel.updateUser(fullName, phone)
                        }
                    )
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) + 20.dp,
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) + 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp
            )
        ) {
            item {
                ProfileCard(
                    initials = initialsFromName(fullName),
                    name = fullName,
                    job = jobTitle,
                    totalTx = uiState.transactionCount,
                    activeDays = uiState.activeDays.toInt()
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text("Thông tin chi tiết", fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
                Spacer(Modifier.height(12.dp))
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        InfoFieldRow(
                            icon = Icons.Outlined.Person,
                            placeholder = "Họ và tên",
                            value = fullName,
                            onValueChange = { fullName = it }
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.Email,
                            placeholder = "Email",
                            value = email,
                            onValueChange = { /* Email không được thay đổi */ },
                            readOnly = true,
                            keyboard = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.Phone,
                            placeholder = "Số điện thoại",
                            value = phone,
                            onValueChange = { phone = it.filter(Char::isDigit) },
                            keyboard = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.Place,
                            placeholder = "Địa chỉ (chưa hỗ trợ)",
                            value = address,
                            onValueChange = { address = it }
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.CalendarMonth,
                            placeholder = "Ngày sinh (chưa hỗ trợ)",
                            value = birthdayMillis.formatDate(),
                            onValueChange = {},
                            readOnly = true,
                            onClick = { showDatePicker = true }
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.Work,
                            placeholder = "Nghề nghiệp (chưa hỗ trợ)",
                            value = jobTitle,
                            onValueChange = { jobTitle = it }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    birthdayMillis = dateState.selectedDateMillis
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Hủy") } }
        ) { DatePicker(state = dateState) }
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(shape)
            .background(brush = AppGradient.BluePurple, shape = shape)
            .border(0.75.dp, Color.White.copy(alpha = 0.6f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.ic_save),
                contentDescription = "Lưu",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Lưu",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun ProfileCard(
    initials: String,
    name: String,
    job: String,
    totalTx: Int,
    activeDays: Int
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(brush = AppGradient.BluePurple),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = scheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
            Text(job, color = scheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(value = totalTx.toString(), label = "Giao dịch")
                StatItem(value = activeDays.toString(), label = "Ngày")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    val scheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = scheme.primary)
        Text(label, color = scheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .let { if (onClick != null && !readOnly) it.clickable { onClick() } else it },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, color = scheme.onSurfaceVariant) },
            singleLine = true,
            readOnly = readOnly,
            keyboardOptions = keyboard,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = scheme.primary,
                unfocusedTextColor = scheme.onSurface,
                focusedTextColor = scheme.onSurface,
            )
        )
    }
}

private fun initialsFromName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts.first().take(2).uppercase(Locale.getDefault())
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase(Locale.getDefault())
    }
}

private fun Long?.formatDate(): String {
    if (this == null) return ""
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
}

