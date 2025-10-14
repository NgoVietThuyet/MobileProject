package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.AuthContainer
import com.example.test.ui.theme.AppGradient

@Composable
fun LoginScreen(
    onNavigateToEmail: () -> Unit = {},
    onNavigateToPhone: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onFb: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    AuthContainer(
        iconRes = R.drawable.piggy,
        title = "Chào mừng",
        subtitle = "Đăng nhập để quản lý tài chính của bạn"
    ) {
        // Login email + phone
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
                // Email button (gradient)
                Button(
                    onClick = { onNavigateToEmail() },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = "Email",
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Đăng nhập bằng email", color = scheme.onPrimary, fontSize = 16.sp)
                        }
                    }
                }

                // Phone button
                OutlinedButton(
                    onClick = { onNavigateToPhone() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.onSurface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Phone",
                            tint = scheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Đăng nhập bằng SĐT", color = scheme.onSurface, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        // Social login
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
                Text("Hoặc đăng nhập với", fontSize = 14.sp, color = scheme.onSurfaceVariant)

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SocialButton(R.drawable.ic_facebook, "Facebook", onClick = onFb)
                    SocialButton(R.drawable.ic_google, "Google")
                    SocialButton(R.drawable.ic_x, "X")
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Footer
        Row {
            Text("Chưa có tài khoản?", color = scheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "Đăng ký ngay",
                color = scheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Composable
fun SocialButton(iconRes: Int, desc: String, onClick: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    OutlinedIconButton(
        onClick = onClick,
        modifier = Modifier.size(50.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = scheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = desc,
            tint = Color.Unspecified,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        )
    }
}
