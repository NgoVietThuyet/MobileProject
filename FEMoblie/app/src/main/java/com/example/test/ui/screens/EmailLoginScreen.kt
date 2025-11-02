package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient
import com.example.test.utils.SoundManager

@Composable
fun EmailLoginScreen(
    onBack: () -> Unit = {},
    onLogin: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onRegister: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val context = androidx.compose.ui.platform.LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AuthContainer(
        iconRes = R.drawable.ic_email,
        title = "Đăng nhập",
        subtitle = "Nhập email và mật khẩu",
        onBack = onBack
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, scheme.outlineVariant),
            color = scheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Email", color = scheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = "Email",
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Mật khẩu", color = scheme.onSurfaceVariant, fontSize = 14.sp) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = "Toggle Password",
                                tint = scheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Password",
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = scheme.primary,
                                checkmarkColor = scheme.onPrimary,
                                uncheckedColor = scheme.outline,
                                disabledUncheckedColor = scheme.outlineVariant
                            )
                        )
                        Text("Ghi nhớ", fontSize = 14.sp, color = scheme.onSurface)
                    }
                    Text(
                        "Quên mật khẩu?",
                        color = scheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { }
                    )
                }

                // Khoảng không gian cố định cho error message (tránh đẩy button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Button(
                    onClick = {
                        SoundManager.playClick(context)
                        errorMessage = "" // Xóa lỗi cũ
                        onLogin(email.trim(), password) { error ->
                            errorMessage = error
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(AppGradient.BluePurple, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Đăng nhập",
                            color = scheme.onPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Chưa có tài khoản?", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Đăng ký ngay",
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onRegister() }
                    )
                }
            }
        }
    }
}
