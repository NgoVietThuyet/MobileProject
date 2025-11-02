package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.test.utils.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String? = null,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
    containerBrush: Brush? = null,
    containerAlpha: Float = 1f,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backContentDescription: String = "Quay lại",
) {
    val context = LocalContext.current
    Column(modifier) {
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = containerAlpha }
                    .background(containerBrush ?: SolidColor(containerColor))
            )
            CenterAlignedTopAppBar(
                title = { title?.let { Text(it, fontWeight = FontWeight.SemiBold) } },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = {
                            SoundManager.playClick(context)
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = backContentDescription
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = windowInsets,
                scrollBehavior = scrollBehavior
            )
        }
        if (showDivider) Divider()
    }
}