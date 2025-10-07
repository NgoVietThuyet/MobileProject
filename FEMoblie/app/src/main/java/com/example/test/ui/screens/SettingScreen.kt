package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.theme.AppGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onHome: () -> Unit = {},
    onReport: () -> Unit = {},
    onSaving: () -> Unit = {},
    onPersonalInfo: () -> Unit = {},
    onProfilePicture: () -> Unit = {},
    onLanguages: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSetting: () -> Unit = {},
    onCamera: () -> Unit = {}
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var notif by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var sounds by remember { mutableStateOf(true) }
    var isVi by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9D9D9).copy(alpha = 0.6f),
                    scrolledContainerColor = Color(0xFFD9D9D9).copy(alpha = 0.6f)
                ),
                windowInsets = WindowInsets(0),
                modifier = Modifier.height(appBarHeight)
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
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item { Spacer(Modifier.height(52.dp)) }

            item {
                Text(
                    "Cài đặt",
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
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
                        ) { Text("NA", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text("Nguyễn Văn A", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Text("nguyenvana@email.com", color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Người dùng", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF9CA3AF))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text("Tài khoản", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(8.dp))
                SectionCard {
                    NavRow(
                        icon = painterResource(R.drawable.ic_user),
                        title = "Thông tin cá nhân",
                        sub = "Họ tên, email, số điện thoại",
                        onMoreClick = onPersonalInfo
                    )
                    Divider(color = Color(0xFFE5E7EB))
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
                Text("Ứng dụng", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(8.dp))
                SectionCard {
                    ToggleRow(
                        icon = painterResource(R.drawable.ic_bell),
                        title = "Thông báo",
                        sub = "Nhận thông báo giao dịch",
                        checked = notif,
                        onChecked = { notif = it }
                    )
                    Divider(color = Color(0xFFE5E7EB))
                    ToggleRow(
                        icon = painterResource(R.drawable.ic_moon),
                        title = "Chế độ tối",
                        sub = "Tăng bảo mật và thoải mái mắt",
                        checked = darkMode,
                        onChecked = { darkMode = it }
                    )
                    Divider(color = Color(0xFFE5E7EB))
                    ToggleRow(
                        icon = painterResource(R.drawable.ic_volumn),
                        title = "Âm thanh",
                        sub = "Âm thanh ứng dụng",
                        checked = sounds,
                        onChecked = { sounds = it }
                    )
                    Divider(color = Color(0xFFE5E7EB))
                    NavRow(
                        icon = painterResource(R.drawable.ic_language),
                        title = "Ngôn ngữ",
                        sub = if (isVi) "Hiện tại: Tiếng Việt" else "Current: English",
                        onMoreClick = { isVi = !isVi }
                    )
                    Divider(color = Color(0xFFE5E7EB))
                    NavRow(
                        icon = rememberVectorPainter(Icons.Outlined.Language),
                        title = "Thay đổi đơn vị tiền tệ",
                        sub = "Chọn đơn vị tiền tệ mà bạn muốn",
                        onMoreClick = onLanguages
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
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLogoCircle(size = 64.dp)
                        Spacer(Modifier.height(10.dp))
                        Text("Quản lý thu chi", fontWeight = FontWeight.SemiBold)
                        Text("Phiên bản 0.0.1", color = Color(0xFF6B7280), fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Ứng dụng quản lý tài chính cá nhân", color = Color(0xFF9CA3AF), fontSize = 12.sp)
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
                    border = BorderStroke(2.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("© 2025", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// helpers //

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
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) { Icon(painter = icon, contentDescription = null, tint = Color(0xFF6B7280)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, color = Color(0xFF9CA3AF), fontSize = 12.sp)
        }
        IconButton(onClick = onMoreClick) {
            Icon(Icons.Outlined.Info, contentDescription = "More", tint = Color(0xFF9CA3AF))
        }
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
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) { Icon(painter = icon, contentDescription = null, tint = Color(0xFF6B7280)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            if (sub.isNotBlank()) Text(sub, color = Color(0xFF9CA3AF), fontSize = 12.sp)
        }
        GradientSwitch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun GradientSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                    else SolidColor(Color(0xFFE5E7EB))
                )
                .then(
                    if (!checked) Modifier.border(
                        2.dp, Color(0xFF9CA3AF), RoundedCornerShape(trackHeight / 2)
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
            modifier = Modifier.matchParentSize()
        )
    }
}
