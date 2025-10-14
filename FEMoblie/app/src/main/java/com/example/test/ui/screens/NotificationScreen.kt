@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.mock.MockData
import com.example.test.ui.mock.NotificationItem

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var all by rememberSaveable { mutableStateOf(MockData.items) }
    var tab by rememberSaveable { mutableStateOf(NotifTab.ALL) }

    val filtered = remember(all, tab) { if (tab == NotifTab.UNREAD) all.filter { it.unread } else all }
    val totalCount = all.size
    val unreadCount = all.count { it.unread }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        bottomBar = bottomBar
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(inner)
                .consumeWindowInsets(inner)
        ) {
            HeaderNotification(total = totalCount, onBack = onBack)

            SegmentedTabs(
                allCount = totalCount,
                unreadCount = unreadCount,
                selected = tab,
                onSelect = { tab = it }
            )

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                EmptyUnread()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        NotificationCard(
                            data = item,
                            onClick = {
                                all = all.map { if (it.id == item.id) it.copy(unread = false) else it }
                            },
                            onDelete = { all = all.filterNot { it.id == item.id } }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderNotification(total: Int, onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(scheme.tertiary, scheme.primary))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại", tint = scheme.onPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("Thông báo", color = scheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(scheme.onPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bell),
                        contentDescription = null,
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Tổng thông báo", color = scheme.onPrimary.copy(alpha = 0.9f), fontSize = 13.sp)
                    Text("$total", color = scheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private enum class NotifTab { ALL, UNREAD }

@Composable
private fun SegmentedTabs(
    allCount: Int,
    unreadCount: Int,
    selected: NotifTab,
    onSelect: (NotifTab) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChipPill(
            text = "Tất cả ($allCount)",
            selected = selected == NotifTab.ALL,
            trailingCount = null,
            onClick = { onSelect(NotifTab.ALL) }
        )
        FilterChipPill(
            text = "Chưa đọc",
            trailingCount = unreadCount,
            selected = selected == NotifTab.UNREAD,
            onClick = { onSelect(NotifTab.UNREAD) }
        )
    }
}

@Composable
private fun FilterChipPill(
    text: String,
    selected: Boolean,
    trailingCount: Int? = null,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(100)
    if (selected) {
        Button(
            onClick = onClick,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = scheme.primary,
                contentColor = scheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (trailingCount != null) {
                Spacer(Modifier.width(6.dp))
                CountBadge(trailingCount, bg = scheme.onPrimary, fg = scheme.primary)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.onSurface),
            border = BorderStroke(1.dp, scheme.outlineVariant),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (trailingCount != null) {
                Spacer(Modifier.width(6.dp))
                CountBadge(trailingCount, bg = scheme.surfaceVariant, fg = scheme.onSurface)
            }
        }
    }
}

@Composable
private fun CountBadge(count: Int, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = if (count > 99) "99+" else "$count",
            color = fg,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun NotificationCard(
    data: NotificationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(data.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(data.iconEmoji, fontSize = 18.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        data.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (data.unread) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(scheme.primary)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(data.message, fontSize = 13.sp, color = scheme.onSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Text(data.time, fontSize = 12.sp, color = scheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Xoá", tint = scheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyUnread() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(scheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Không có thông báo chưa đọc", color = scheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Preview(widthDp = 412, heightDp = 918, showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    NotificationScreen()
}
