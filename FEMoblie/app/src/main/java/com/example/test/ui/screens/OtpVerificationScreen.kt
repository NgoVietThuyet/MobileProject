package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    phoneNumber: String = "+84090808080",
    onBack: () -> Unit = {},
    onVerify: (String) -> Unit = {},
    onResend: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    var otp by remember { mutableStateOf(List(6) { "" }) }
    var counter by remember { mutableStateOf(60) }
    val focusRequesters = List(6) { FocusRequester() }

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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Mã đã được gửi đến $phoneNumber",
                    color = scheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    "Mã xác thực 6 số",
                    color = scheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

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
                                    if (it.isNotEmpty() && index < 5) focusRequesters[index + 1].requestFocus()
                                }
                                if (it.isEmpty() && index > 0) focusRequesters[index - 1].requestFocus()
                            },
                            modifier = Modifier
                                .width(48.dp)
                                .height(56.dp)
                                .focusRequester(focusRequesters[index]),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = scheme.surfaceVariant,
                                unfocusedContainerColor = scheme.surfaceVariant,
                                disabledContainerColor = scheme.surfaceVariant,
                                focusedBorderColor = scheme.primary,
                                unfocusedBorderColor = scheme.outlineVariant,
                                disabledBorderColor = scheme.outlineVariant,
                                cursorColor = scheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Button(
                    onClick = { onVerify(otp.joinToString("")) },
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
                        Text("Xác thực", color = scheme.onPrimary, fontSize = 16.sp)
                    }
                }

                if (counter > 0) {
                    Text("Không nhận được mã?", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                    Text("Gửi lại sau ${counter}s", color = scheme.onSurface, fontSize = 14.sp)
                } else {
                    Text(
                        "Gửi lại mã",
                        color = scheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            counter = 60
                            onResend()
                        }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.secondaryContainer,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = "Warning",
                            tint = scheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mã OTP có hiệu lực trong 5 phút\nVui lòng không chia sẻ mã này với bất kỳ ai",
                            fontSize = 13.sp,
                            color = scheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
