package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.platform.LocalContext
import com.example.test.R
import com.example.test.ui.theme.AppGradient
import com.example.test.utils.SoundManager

@Composable
fun AuthContainer(
    iconRes: Int,
    title: String,
    subtitle: String,
    functionTitle: String? = null,
    onBack: (() -> Unit)? = null,
    gradientColors: List<Color>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val topBarHeight = 64.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight)
        ) {
            if (onBack != null || functionTitle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = {
                            SoundManager.playClick(context)
                            onBack()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = scheme.onBackground
                            )
                        }
                    }
                    if (functionTitle != null) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            functionTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBackground
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Logo
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)
                .background(AppGradient.BluePurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Icon",
                tint = scheme.onPrimary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(48.dp))

        // Title & subtitle
        Text(
            title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            fontSize = 16.sp,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(80.dp))

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
