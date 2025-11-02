package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient
import com.example.test.utils.PasswordValidator
import com.example.test.utils.SoundManager

@Composable
fun RegisterScreen(
    onBack: () -> Unit = {},
    onRegister: (fullName: String, email: String, phone: String, password: String, onError: (String) -> Unit) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    // Step tracking
    var currentStep by rememberSaveable { mutableStateOf(1) }

    // Step 1: Personal Info
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var birthday by rememberSaveable { mutableStateOf("") }
    var occupation by rememberSaveable { mutableStateOf("") }

    // Step 2: Account Info
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var agreePolicy by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    AuthContainer(
        iconRes = R.drawable.ic_user,
        title = "Đăng ký",
        subtitle = when (currentStep) {
            1 -> "Thông tin cá nhân"
            2 -> "Thông tin liên hệ"
            else -> "Bảo mật tài khoản"
        },
        onBack = if (currentStep > 1) {
            {
                SoundManager.playClick(ctx)
                currentStep--
                errorMessage = ""
            }
        } else onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step indicator - Ngoài box (3 steps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(step = 1, isActive = currentStep == 1, isCompleted = currentStep > 1, scheme = scheme)
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(2.dp)
                        .background(if (currentStep > 1) scheme.primary else scheme.outlineVariant)
                )
                StepIndicator(step = 2, isActive = currentStep == 2, isCompleted = currentStep > 2, scheme = scheme)
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(2.dp)
                        .background(if (currentStep > 2) scheme.primary else scheme.outlineVariant)
                )
                StepIndicator(step = 3, isActive = currentStep == 3, isCompleted = false, scheme = scheme)
            }

            // Surface chứa form
            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant),
                color = scheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (currentStep == 1) {
                    // ===== STEP 1: Personal Info =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Tên", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = scheme.surfaceVariant,
                                unfocusedContainerColor = scheme.surfaceVariant,
                                focusedBorderColor = scheme.primary,
                                unfocusedBorderColor = scheme.outlineVariant
                            )
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Họ", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = scheme.surfaceVariant,
                                unfocusedContainerColor = scheme.surfaceVariant,
                                focusedBorderColor = scheme.primary,
                                unfocusedBorderColor = scheme.outlineVariant
                            )
                        )
                    }

                    OutlinedTextField(
                        value = birthday,
                        onValueChange = { birthday = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Sinh nhật (VD: 01/01/2000)", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_user),
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = scheme.surfaceVariant,
                            unfocusedContainerColor = scheme.surfaceVariant,
                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outlineVariant
                        )
                    )

                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Nghề nghiệp", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_user),
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = scheme.surfaceVariant,
                            unfocusedContainerColor = scheme.surfaceVariant,
                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outlineVariant
                        )
                    )

                    // Error box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (errorMessage.isNotBlank()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Button Tiếp tục
                    Button(
                        onClick = {
                            SoundManager.playClick(ctx)
                            errorMessage = ""
                            when {
                                firstName.isBlank() -> errorMessage = "❌ Vui lòng nhập tên"
                                lastName.isBlank() -> errorMessage = "❌ Vui lòng nhập họ"
                                birthday.isBlank() -> errorMessage = "❌ Vui lòng nhập sinh nhật"
                                occupation.isBlank() -> errorMessage = "❌ Vui lòng nhập nghề nghiệp"
                                else -> {
                                    currentStep = 2
                                }
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
                            Text("Tiếp tục", color = scheme.onPrimary, fontSize = 16.sp)
                        }
                    }

                } else if (currentStep == 2) {
                    // ===== STEP 2: Email & Phone =====
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Email", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = scheme.surfaceVariant,
                            unfocusedContainerColor = scheme.surfaceVariant,
                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outlineVariant
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Số điện thoại", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_phone),
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = scheme.surfaceVariant,
                            unfocusedContainerColor = scheme.surfaceVariant,
                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outlineVariant
                        )
                    )

                    // Error box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (errorMessage.isNotBlank()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Button Tiếp tục
                    Button(
                        onClick = {
                            SoundManager.playClick(ctx)
                            errorMessage = ""
                            when {
                                email.isBlank() -> errorMessage = "❌ Vui lòng nhập email"
                                phone.isBlank() -> errorMessage = "❌ Vui lòng nhập số điện thoại"
                                else -> {
                                    currentStep = 3
                                }
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
                            Text("Tiếp tục", color = scheme.onPrimary, fontSize = 16.sp)
                        }
                    }

                } else {
                    // ===== STEP 3: Password & Security =====
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Mật khẩu", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = scheme.surfaceVariant,
                            unfocusedContainerColor = scheme.surfaceVariant,
                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outlineVariant
                        )
                    )

                    if (password.isNotEmpty()) {
                        val validationResult = PasswordValidator.validate(password)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (validationResult.isValid) "✓" else "✗",
                                color = if (validationResult.isValid) Color(0xFF4CAF50) else scheme.error,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (validationResult.isValid) "Mật khẩu hợp lệ" else validationResult.message,
                                color = if (validationResult.isValid) Color(0xFF4CAF50) else scheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Nhập lại mật khẩu", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = scheme.surfaceVariant,
                            unfocusedContainerColor = scheme.surfaceVariant,
                            focusedBorderColor = scheme.primary,
                            unfocusedBorderColor = scheme.outlineVariant
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = agreePolicy,
                            onCheckedChange = { agreePolicy = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = scheme.primary,
                                checkmarkColor = scheme.onPrimary
                            )
                        )
                        Text("Đồng ý với điều khoản", fontSize = 14.sp)
                    }

                    // Error box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (errorMessage.isNotBlank()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Button Đăng ký
                    Button(
                        onClick = {
                            SoundManager.playClick(ctx)
                            errorMessage = ""
                            when {
                                email.isBlank() -> errorMessage = "❌ Vui lòng nhập email"
                                phone.isBlank() -> errorMessage = "❌ Vui lòng nhập số điện thoại"
                                password.isBlank() -> errorMessage = "❌ Vui lòng nhập mật khẩu"
                                password.isBlank() -> errorMessage = "❌ Vui lòng nhập mật khẩu"
                                !PasswordValidator.validate(password).isValid -> {
                                    errorMessage = "❌ ${PasswordValidator.validate(password).message}"
                                }
                                confirmPassword.isBlank() -> errorMessage = "❌ Vui lòng nhập lại mật khẩu"
                                password != confirmPassword -> errorMessage = "❌ Mật khẩu không khớp"
                                !agreePolicy -> errorMessage = "❌ Vui lòng đồng ý với điều khoản sử dụng"
                                else -> {
                                    val fullName = "$firstName $lastName"
                                    onRegister(fullName.trim(), email.trim(), phone.trim(), password) { error ->
                                        errorMessage = error
                                    }
                                }
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
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    isActive: Boolean,
    isCompleted: Boolean,
    scheme: ColorScheme
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = when {
                    isCompleted -> scheme.primary
                    isActive -> scheme.primary
                    else -> scheme.outlineVariant
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = step.toString(),
            color = if (isActive || isCompleted) scheme.onPrimary else scheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

