package com.example.test.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient

@Composable
fun RegisterScreen(
    onBack: () -> Unit = {},
    onRegister: (fullName: String, email: String, phone: String, password: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreePolicy by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    AuthContainer(
        iconRes = R.drawable.ic_user,
        title = "Đăng ký",
        subtitle = "Tạo tài khoản mới",
        onBack = onBack
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, scheme.outlineVariant),
            color = scheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-70).dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Họ và tên", color = scheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Full name",
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedContainerColor = scheme.surfaceVariant,
                        disabledContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        disabledBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
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
                        disabledContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        disabledBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Số điện thoại", color = scheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Phone",
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedContainerColor = scheme.surfaceVariant,
                        disabledContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        disabledBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                // Mật khẩu với eye/eye_off
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Mật khẩu", color = scheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Password",
                            tint = scheme.onSurfaceVariant
                        )
                    },
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
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedContainerColor = scheme.surfaceVariant,
                        disabledContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        disabledBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                // Nhập lại mật khẩu với eye/eye_off
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Nhập lại mật khẩu", color = scheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Confirm Password",
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        val icon = if (confirmPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = "Toggle Confirm Password",
                                tint = scheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surfaceVariant,
                        unfocusedContainerColor = scheme.surfaceVariant,
                        disabledContainerColor = scheme.surfaceVariant,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        disabledBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreePolicy,
                        onCheckedChange = { agreePolicy = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = scheme.primary,
                            checkmarkColor = scheme.onPrimary,
                            uncheckedColor = scheme.outline,
                            disabledUncheckedColor = scheme.outlineVariant
                        )
                    )
                    Text("Đồng ý với điều khoản", fontSize = 14.sp, color = scheme.onSurface)
                }

                Button(
                    onClick = {
                        if (!agreePolicy) {
                            Toast.makeText(ctx, "Vui lòng đồng ý với điều khoản sử dụng", Toast.LENGTH_SHORT).show()
                        } else {
                            onRegister(fullName.trim(), email.trim(), phone.trim(), password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = AppGradient.BluePurple,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Đăng ký", color = scheme.onPrimary, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
