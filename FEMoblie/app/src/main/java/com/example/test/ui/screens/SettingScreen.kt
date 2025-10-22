@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.theme.AppGradient
import com.example.test.vm.ProfileViewModel
import java.util.*

private val BrandGreen = Color(0xFF16A34A)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    dark: Boolean,
    onToggleDark: (Boolean) -> Unit,
    onHome: () -> Unit = {},
    onReport: () -> Unit = {},
    onSaving: () -> Unit = {},
    onPersonalInfo: () -> Unit = {},
    onProfilePicture: () -> Unit = {},
    onLanguages: () -> Unit = {},
    onCurrency: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSetting: () -> Unit = {},
    onCamera: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var notif by rememberSaveable { mutableStateOf(false) }
    var sounds by rememberSaveable { mutableStateOf(true) }
    var isVi by rememberSaveable { mutableStateOf(true) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun initialsFromName(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> ""
            parts.size == 1 -> parts.first().take(2).uppercase(Locale.getDefault())
            else -> (parts.first().take(1) + parts.last().take(1)).uppercase(Locale.getDefault())
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppHeader(
                title = "Cài đặt",
                showBack = false,
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = BottomTab.SETTINGS,
                onHome = onHome,
                onReport = onReport,
                onCamera = onCamera,
                onSaving = onSaving,
                onSetting = onSetting
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(brush = AppGradient.BluePurple),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = initialsFromName(uiState.currentName)
                            Text(initials.ifBlank { "..." }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                uiState.currentName.ifBlank { "User" },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold, fontSize = 18.sp
                            )
                            Text(
                                uiState.currentEmail.ifBlank { "..." },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Người dùng",
                                color = BrandGreen,
                                fontSize = 12.sp, fontWeight = FontWeight.Medium
                            )
                        }

                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    "Tài khoản",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
                SectionCard {
                    NavRow(
                        icon = painterResource(R.drawable.ic_user),
                        title = "Thông tin cá nhân",
                        sub = "Họ tên, email, số điện thoại",
                        onMoreClick = onPersonalInfo
                    )
                    Divider(color = DividerDefaults.color)
                    NavRow(
                        icon = painterResource(R.drawable.ic_camera),
                        title = "Ảnh đại diện",
                        sub = "Đổi ảnh đại diện",
                        onMoreClick = onProfilePicture
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
            item {
                Text(
                    "Ứng dụng",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
                SectionCard {
                    ToggleRow(
                        icon = painterResource(R.drawable.ic_bell),
                        title = "Thông báo",
                        sub = "Nhận thông báo giao dịch",
                        checked = notif,
                        onChecked = { notif = it }
                    )
                    Divider(color = DividerDefaults.color)
                    ToggleRow(
                        icon = painterResource(R.drawable.ic_moon),
                        title = "Chế độ tối",
                        sub = "Tăng bảo mật và thoải mái mắt",
                        checked = dark,
                        onChecked = onToggleDark
                    )
                    Divider(color = DividerDefaults.color)
                    ToggleRow(
                        icon = painterResource(R.drawable.ic_volume),
                        title = "Âm thanh",
                        sub = "Âm thanh ứng dụng",
                        checked = sounds,
                        onChecked = { sounds = it }
                    )
                    Divider(color = DividerDefaults.color)
                    NavRow(
                        icon = painterResource(R.drawable.ic_language),
                        title = "Ngôn ngữ",
                        sub = if (isVi) "Hiện tại: Tiếng Việt" else "Current: English",
                        onMoreClick = {
                            isVi = !isVi
                            onLanguages()
                        }
                    )
                    Divider(color = DividerDefaults.color)
                    NavRow(
                        icon = rememberVectorPainter(Icons.Outlined.Language),
                        title = "Thay đổi đơn vị tiền tệ",
                        sub = "Chọn đơn vị tiền tệ mà bạn muốn",
                        onMoreClick = onCurrency
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLogoCircle(size = 64.dp)
                        Spacer(Modifier.height(10.dp))
                        Text("Quản lý thu chi", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("Phiên bản 0.0.1", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Ứng dụng quản lý tài chính cá nhân", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_logout), contentDescription = null, tint = Color.Unspecified)
                    Spacer(Modifier.width(10.dp))
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("© 2025", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AppLogoCircle(size: Dp = 72.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(AppGradient.BluePurple),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.piggy),
            contentDescription = "Logo",
            tint = Color.White,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { Column(content = content) }
}

@Composable
private fun NavRow(
    icon: Painter,
    title: String,
    sub: String,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onMoreClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ToggleRow(
    icon: Painter,
    title: String,
    sub: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        GradientSwitch(checked = checked, onCheckedChange = onChecked, label = title)
    }
}

@Composable
private fun GradientSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    val trackWidth = 52.dp
    val trackHeight = 32.dp
    Box(
        modifier = Modifier.width(trackWidth).height(trackHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(
                    brush = if (checked) AppGradient.BluePurple
                    else SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                )
                .then(
                    if (!checked) Modifier.border(
                        2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(trackHeight / 2)
                    ) else Modifier
                )
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .matchParentSize()
                .semantics { contentDescription = "Công tắc $label" }
        )
    }
}

