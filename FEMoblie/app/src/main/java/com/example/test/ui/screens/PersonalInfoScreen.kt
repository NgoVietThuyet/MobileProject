@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val appBarHeight = 36.dp

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
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(20.dp)
        ) {
            item { Spacer(Modifier.height(52.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Quay lại",
                            tint = Color.Unspecified
                        )
                    }
                    Text(
                        "Thay đổi thông tin cá nhân",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    GradientButton(
                        onClick = {
                            onSave(
                                PersonalInfo(
                                    fullName, email, phone, address, birthdayMillis, jobTitle
                                )
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            contentDescription = "Lưu",
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Lưu", color = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

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
                Text("Thông tin chi tiết", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        InfoFieldRow(
                            icon = Icons.Outlined.Person,
                            placeholder = "Họ và tên",
                            value = fullName,
                            onValueChange = { fullName = it }
                        )
                        Divider(color = Color(0xFFE5E7EB))
                        InfoFieldRow(
                            icon = Icons.Outlined.Email,
                            placeholder = "Email",
                            value = email,
                            onValueChange = { email = it },
                            keyboard = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Divider(color = Color(0xFFE5E7EB))
                        InfoFieldRow(
                            icon = Icons.Outlined.Phone,
                            placeholder = "Số điện thoại",
                            value = phone,
                            onValueChange = { phone = it.filter(Char::isDigit) },
                            keyboard = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        Divider(color = Color(0xFFE5E7EB))
                        InfoFieldRow(
                            icon = Icons.Outlined.Place,
                            placeholder = "Địa chỉ",
                            value = address,
                            onValueChange = { address = it }
                        )
                        Divider(color = Color(0xFFE5E7EB))
                        InfoFieldRow(
                            icon = Icons.Outlined.CalendarMonth,
                            placeholder = "Ngày sinh",
                            value = birthdayMillis.formatDate(),
                            onValueChange = {},
                            readOnly = true,
                            onClick = { showDatePicker = true }
                        )
                        Divider(color = Color(0xFFE5E7EB))
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

// helpers //

@Composable
private fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(brush = AppGradient.BluePurple)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
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
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(brush = AppGradient.BluePurple),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(job, color = Color(0xFF6B7280), fontSize = 14.sp)
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
        Text(label, color = Color(0xFF6B7280), fontSize = 12.sp)
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
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6B7280))
        }
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .let { m -> if (onClick != null) m.clickable { onClick() } else m },
            placeholder = { Text(placeholder) },
            singleLine = true,
            readOnly = readOnly,
            keyboardOptions = keyboard,
            shape = RoundedCornerShape(12.dp)
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
