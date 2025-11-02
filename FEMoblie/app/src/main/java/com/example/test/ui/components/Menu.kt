package com.example.test.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.utils.SoundManager

enum class BottomTab { HOME, REPORT, SAVING, SETTINGS }

@Composable
fun MainBottomBar(
    selected: BottomTab,
    onHome: () -> Unit,
    onReport: () -> Unit,
    onCamera: () -> Unit,
    onSaving: () -> Unit,
    onSetting: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomItem(
                icon = R.drawable.ic_home, label = "Trang chủ",
                selected = selected == BottomTab.HOME,
                onClick = onHome, modifier = Modifier.weight(1f)
            )
            BottomItem(
                icon = R.drawable.ic_report, label = "Báo cáo",
                selected = selected == BottomTab.REPORT,
                onClick = onReport, modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        SoundManager.playClick(context)
                        onCamera()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            BottomItem(
                icon = R.drawable.ic_saving, label = "Tiết kiệm",
                selected = selected == BottomTab.SAVING,
                onClick = onSaving, modifier = Modifier.weight(1f)
            )
            BottomItem(
                icon = R.drawable.ic_settings, label = "Cài đặt",
                selected = selected == BottomTab.SETTINGS,
                onClick = onSetting, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomItem(
    icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBg = MaterialTheme.colorScheme.secondaryContainer
    val selectedContent = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedContent = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.clickable(onClick = {
        SoundManager.playClick(context)
        onClick()
    }), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) selectedBg else androidx.compose.ui.graphics.Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (selected) selectedContent else unselectedContent
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) selectedContent else unselectedContent
            )
        }
    }
}
