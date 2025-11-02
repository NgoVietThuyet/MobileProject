package com.example.test.ui.screens

import android.app.Activity
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

private const val TAG = "PhoneLoginScreen"

@Composable
fun PhoneLoginScreen(
    onBack: () -> Unit = {},
    onRequestOtp: (String, String) -> Unit = { _, _ -> } // (phoneNumber, verificationId)
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Callback để nhận verification ID từ Firebase
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Tự động xác thực thành công (hiếm khi xảy ra)
                Log.d(TAG, "onVerificationCompleted: Auto-verified")
                isLoading = false
                Toast.makeText(context, "Xác thực tự động thành công", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed", e)
                Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                isLoading = false
                Toast.makeText(
                    context,
                    "Gửi OTP thất bại: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent: verificationId=$verificationId")
                isLoading = false
                Toast.makeText(context, "Mã OTP đã được gửi", Toast.LENGTH_SHORT).show()
                onRequestOtp(phone.trim(), verificationId)
            }
        }
    }

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
                        Text("+84 xxx xxx xxx", color = scheme.onSurfaceVariant, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Phone",
                            tint = scheme.onSurfaceVariant
                        )
                    },
                    enabled = !isLoading,
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

                // Thông tin hướng dẫn
                Text(
                    "Nhập số điện thoại theo định dạng quốc tế (ví dụ: +84901234567)",
                    color = scheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = {
                        val trimmedPhone = phone.trim()
                        Log.d(TAG, "Button clicked, phone input: $trimmedPhone")

                        // Validate số điện thoại
                        if (trimmedPhone.isEmpty()) {
                            Log.w(TAG, "Phone is empty")
                            Toast.makeText(context, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!trimmedPhone.startsWith("+")) {
                            Log.w(TAG, "Phone doesn't start with +")
                            Toast.makeText(
                                context,
                                "Số điện thoại phải bắt đầu bằng + và mã quốc gia (ví dụ: +84)",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        Log.d(TAG, "Starting Firebase phone verification for: $trimmedPhone")
                        Log.d(TAG, "Firebase Auth instance: ${auth.app.name}")
                        isLoading = true

                        try {
                            // Gửi OTP qua Firebase
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(trimmedPhone)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(context as Activity)
                                .setCallbacks(callbacks)
                                .build()

                            Log.d(TAG, "Calling PhoneAuthProvider.verifyPhoneNumber...")
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception when calling verifyPhoneNumber", e)
                            isLoading = false
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = scheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Nhận mã xác thực", color = scheme.onPrimary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
