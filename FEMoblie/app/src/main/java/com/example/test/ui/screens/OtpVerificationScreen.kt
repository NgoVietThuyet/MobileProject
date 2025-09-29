package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun OtpVerificationScreen(
    phoneNumber: String = "+84090808080",
    onBack: () -> Unit = {},
    onVerify: (String) -> Unit = {},
    onResend: () -> Unit = {}
) {
    var otp by remember { mutableStateOf(List(6) { "" }) }
    var counter by remember { mutableStateOf(60) }

    // Focus controllers cho 6 ô
    val focusRequesters = List(6) { FocusRequester() }
    /*val focusManager = LocalFocusManager.current*/

    // Countdown resend OTP
    LaunchedEffect(counter) {
        if (counter > 0) {
            delay(1000)
            counter--
        }
    }

    AuthContainer(
        iconRes = R.drawable.ic_shield,
        title = "Xác thực OTP",
        subtitle = "Nhập mã xác thực",
        onBack = onBack,
        gradientColors = listOf(Color(0xFF4C80FF), Color(0xFF3DDC84))
    ) {

        // Container chính
        Surface(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFD9D9D9)),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Mã đã được gửi đến $phoneNumber", color = Color.Gray, fontSize = 14.sp)

                Text("Mã xác thực 6 số", color = Color.Gray, fontSize = 14.sp)

                // Ô nhập OTP
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    otp.forEachIndexed { index, value ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = {
                                if (it.length <= 1 && it.all { c -> c.isDigit() }) {
                                    val newOtp = otp.toMutableList()
                                    newOtp[index] = it
                                    otp = newOtp

                                    if (it.isNotEmpty() && index < 5) {
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                }
                                if (it.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            },
                            modifier = Modifier
                                .width(48.dp)
                                .height(56.dp)
                                .focusRequester(focusRequesters[index])
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFD9D9D9),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.Black
                            )
                        )
                    }
                }

                // Nút xác thực
                Button(
                    onClick = { onVerify(otp.joinToString("")) },
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
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF4C80FF), Color(0xFF3DDC84))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Xác thực", color = Color.White, fontSize = 16.sp)
                    }
                }

                // Resend OTP
                if (counter > 0) {
                    Text("Không nhận được mã?", color = Color.Gray, fontSize = 14.sp)
                    Text("Gửi lại sau ${counter}s", color = Color.Black, fontSize = 14.sp)
                } else {
                    Text(
                        "Gửi lại mã",
                        color = Color(0xFF1877F2),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            counter = 60
                            onResend()
                        }
                    )
                }

                // Thông báo
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE6F0FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = "Warning",
                            tint = Color(0xFF1877F2)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mã OTP có hiệu lực trong 5 phút\nVui lòng không chia sẻ mã này với bất kỳ ai",
                            fontSize = 13.sp,
                            color = Color(0xFF1877F2)
                        )
                    }
                }
            }
        }
    }
}
