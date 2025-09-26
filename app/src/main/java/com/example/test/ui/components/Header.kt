@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.components.header

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Header xám translucent, tái sử dụng cho mọi screen.
 * - Mặc định cao 36.dp, màu xám #D9D9D9 @ 60% để "nhìn xuyên".
 * - Hỗ trợ scrollBehavior để dùng với nestedScroll().
 * - Có slots: navigationIcon, title, actions giống TopAppBar chuẩn.
 * - windowInsets = WindowInsets(0) để tràn viền (edge-to-edge) như yêu cầu.
 */
@Composable
fun TranslucentHeader(
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    containerColor: Color = Color(0xFFD9D9D9).copy(alpha = 0.6f),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = WindowInsets(0),
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier.then(Modifier.height(height)),
        navigationIcon = navigationIcon,
        title = title,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor
        ),
        windowInsets = windowInsets
    )
}

/**
 * Overload nhanh gọn nếu chỉ cần title dạng text.
 */
@Composable
fun SimpleTranslucentHeader(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    containerColor: Color = Color(0xFFD9D9D9).copy(alpha = 0.6f),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = WindowInsets(0),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TranslucentHeader(
        modifier = modifier,
        height = height,
        containerColor = containerColor,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
        navigationIcon = navigationIcon,
        title = { Text(text) },
        actions = actions
    )
}
