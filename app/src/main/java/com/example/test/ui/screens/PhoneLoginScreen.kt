package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    var phone by remember { mutableStateOf("") }

    AuthContainer(
        iconRes = R.drawable.ic_phone,
        title = "Đăng nhập",
        subtitle = "Nhập số điện thoại",
        onBack = onBack
    ) {
        // Container form
        Surface(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFD9D9D9)),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TextField nhập SĐT
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Số điện thoại", color = Color.DarkGray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Phone"
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFD9D9D9),
                        unfocusedContainerColor = Color(0xFFD9D9D9),
                        disabledContainerColor = Color(0xFFD9D9D9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )

                // Button nhận mã xác thực
                Button(
                    onClick = { onRequestOtp(phone) }, // 🔹 truyền số điện thoại sang OTP
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = AppGradient.BluePurple, // dùng gradient.kt
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nhận mã xác thực", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
