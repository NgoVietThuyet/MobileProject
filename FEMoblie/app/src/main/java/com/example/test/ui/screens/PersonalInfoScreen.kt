@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.theme.AppGradient

data class PersonalInfo(
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val birthdayMillis: Long?,
    val jobTitle: String
)

@Composable
fun PersonalInfoScreen(
    onBack: () -> Unit = {},
    onSave: (PersonalInfo) -> Unit = {},
    initial: PersonalInfo = PersonalInfo(
        fullName = "Nguyễn Văn A",
        email = "nguyenvana@email.com",
        phone = "0987654321",
        address = "144, Xuân Thuỷ",
        birthdayMillis = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse("29/03/2002")?.time,
        jobTitle = "Kỹ sư phần mềm"
    ),
    totalTx: Int = 127,
    activeDays: Int = 45
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var fullName by rememberSaveable { mutableStateOf(initial.fullName) }
    var email by rememberSaveable { mutableStateOf(initial.email) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var address by rememberSaveable { mutableStateOf(initial.address) }
    var birthdayMillis by rememberSaveable { mutableStateOf(initial.birthdayMillis) }
    var jobTitle by rememberSaveable { mutableStateOf(initial.jobTitle) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = birthdayMillis)

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
                            onSave(
                                PersonalInfo(
                                    fullName, email, phone, address, birthdayMillis, jobTitle
                                )
                            )
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
                    totalTx = totalTx,
                    activeDays = activeDays
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
                            onValueChange = { email = it },
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
                            placeholder = "Địa chỉ",
                            value = address,
                            onValueChange = { address = it }
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.CalendarMonth,
                            placeholder = "Ngày sinh",
                            value = birthdayMillis.formatDate(),
                            onValueChange = {},
                            readOnly = true,
                            onClick = { showDatePicker = true }
                        )
                        HorizontalDivider(color = scheme.outlineVariant)
                        InfoFieldRow(
                            icon = Icons.Outlined.Work,
                            placeholder = "Nghề nghiệp",
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

// helpers

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
                Text("".plus(initials), color = scheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
            modifier = Modifier
                .weight(1f)
                .let { m -> if (onClick != null) m.clickable { onClick() } else m },
            placeholder = { Text(placeholder, color = scheme.onSurfaceVariant) },
            singleLine = true,
            readOnly = readOnly,
            keyboardOptions = keyboard,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = scheme.surface,
                unfocusedContainerColor = scheme.surface,
                disabledContainerColor = scheme.surface,
                focusedBorderColor = scheme.primary,
                unfocusedBorderColor = scheme.outlineVariant,
                disabledBorderColor = scheme.outlineVariant,
                cursorColor = scheme.primary
            )
        )
    }
}

private fun initialsFromName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "NA"
        parts.size == 1 -> parts.first().take(2).uppercase(Locale.getDefault())
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase(Locale.getDefault())
    }
}

private fun Long?.formatDate(): String {
    if (this == null) return ""
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
}
