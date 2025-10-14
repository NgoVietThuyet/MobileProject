package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient

@Composable
fun PhoneLoginScreen(
    onBack: () -> Unit = {},
    onRequestOtp: (String) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    var phone by remember { mutableStateOf("") }

    AuthContainer(
        iconRes = R.drawable.ic_phone,
        title = "Đăng nhập",
        subtitle = "Nhập số điện thoại",
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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text("Số điện thoại", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                    },
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

                Button(
                    onClick = { onRequestOtp(phone.trim()) },
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
                        Text("Nhận mã xác thực", color = scheme.onPrimary, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
