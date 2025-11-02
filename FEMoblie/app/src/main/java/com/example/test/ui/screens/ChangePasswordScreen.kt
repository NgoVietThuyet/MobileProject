package com.example.test.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.components.AppHeader
import com.example.test.utils.PasswordValidator
import com.example.test.utils.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    onChangePassword: (oldPassword: String, newPassword: String, onError: (String) -> Unit) -> Unit = { _, _, _ -> }
) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var oldPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            AppHeader(
                title = "Đổi mật khẩu",
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Thay đổi mật khẩu đăng nhập của bạn",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Old Password
            OutlinedTextField(
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    errorMessage = null
                },
                label = { Text("Mật khẩu hiện tại") },
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(
                            imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (oldPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // New Password
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = null
                },
                label = { Text("Mật khẩu mới") },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (newPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (newPassword.isNotEmpty()) {
                val validationResult = PasswordValidator.validate(newPassword)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    PasswordRequirement("Ít nhất 8 ký tự", newPassword.length >= 8)
                    PasswordRequirement("Ít nhất 1 chữ hoa", newPassword.any { it.isUpperCase() })
                    PasswordRequirement("Ít nhất 1 chữ thường", newPassword.any { it.isLowerCase() })
                    PasswordRequirement("Ít nhất 1 số", newPassword.any { it.isDigit() })
                }
            }

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Xác nhận mật khẩu mới") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = errorMessage != null
            )

            // Khoảng không gian cố định cho error message (tránh đẩy button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Change Password Button
            Button(
                onClick = {
                    SoundManager.playClick(context)
                    errorMessage = null // Xóa lỗi cũ
                    when {
                        oldPassword.isBlank() -> {
                            errorMessage = "❌ Vui lòng nhập mật khẩu hiện tại"
                        }
                        newPassword.isBlank() -> {
                            errorMessage = "❌ Vui lòng nhập mật khẩu mới"
                        }
                        !PasswordValidator.validate(newPassword).isValid -> {
                            errorMessage = "❌ ${PasswordValidator.validate(newPassword).message}"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "❌ Mật khẩu xác nhận không khớp"
                        }
                        oldPassword == newPassword -> {
                            errorMessage = "❌ Mật khẩu mới phải khác mật khẩu hiện tại"
                        }
                        else -> {
                            onChangePassword(oldPassword, newPassword) { error ->
                                errorMessage = error
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đổi mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            // Security Tips
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = scheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "💡 Mẹo tạo mật khẩu mạnh",
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSecondaryContainer,
                        fontSize = 14.sp
                    )
                    Text(
                        "• Kết hợp chữ hoa, chữ thường và số",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSecondaryContainer
                    )
                    Text(
                        "• Độ dài tối thiểu 8 ký tự",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSecondaryContainer
                    )
                    Text(
                        "• Không sử dụng thông tin cá nhân (tên, ngày sinh, số điện thoại)",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSecondaryContainer
                    )
                    Text(
                        "• Không sử dụng lại mật khẩu từ các tài khoản khác",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSecondaryContainer
                    )
                    Text(
                        "• Thay đổi mật khẩu định kỳ 3-6 tháng/lần",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirement(text: String, isMet: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (isMet) "✓" else "○",
            color = if (isMet) Color(0xFF10B981) else scheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = text,
            color = if (isMet) scheme.onSurface else scheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal
        )
    }
}
