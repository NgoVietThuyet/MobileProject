@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.R
import com.example.test.ui.components.BottomTab
import com.example.test.ui.components.MainBottomBar
import com.example.test.ui.mock.MockData
import com.example.test.ui.mock.SavingGoalMock
import com.example.test.ui.theme.AppGradient
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SavingsScreen(
    onHome: () -> Unit = {},
    onReport: () -> Unit = {},
    onAddGoal: () -> Unit = {},
    onSettings: () -> Unit = {},
    onCamera: () -> Unit = {},
    onSaving: () -> Unit = {},
    onGoalClick: (Int) -> Unit = {}
) {
    val appBarHeight = 36.dp
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            MainBottomBar(
                selected = BottomTab.SAVING,
                onHome = onHome,
                onReport = onReport,
                onCamera = onCamera,
                onSaving = onSaving,
                onSetting = onSettings
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(brush = AppGradient.BluePurple)
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(appBarHeight + 12.dp))
                        Text("Tiết kiệm", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Xây dựng tương lai tài chính 👋", color = Color.White, fontSize = 14.sp)

                        Spacer(Modifier.height(16.dp))

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tổng tiết kiệm", color = Color.White, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Outlined.ArrowOutward, null, tint = Color.White)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text("70.000.000 đ", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(10.dp))
                                Text("tăng 12% so với tháng trước", color = scheme.tertiaryContainer, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mục tiêu tiết kiệm", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = scheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onAddGoal, colors = ButtonDefaults.textButtonColors(contentColor = scheme.primary)) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Thêm")
                    }
                }
            }

            itemsIndexed(MockData.savingGoals, key = { i, _ -> i }) { i, g ->
                SavingGoalItem(g, onClick = { onGoalClick(i) })
                Spacer(Modifier.height(12.dp))
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                Text(
                    "Mục tiêu phổ biến",
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Medium,
                    color = scheme.onBackground
                )
            }
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(12.dp)) {
                            SuggestTileView("🏠", "mua nhà", "2–5 tỷ", modifier = Modifier.weight(1f))
                            SuggestTileView("🚗", "mua xe", "500M–2 tỷ", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = spacedBy(12.dp)) {
                            SuggestTileView("✈️", "du lịch", "10–50M", modifier = Modifier.weight(1f))
                            SuggestTileView("🆘", "khẩn cấp", "3–6 tháng lương", danger = true, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingGoalItem(
    g: SavingGoalMock,
    onClick: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(g.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(g.emoji, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(g.title, fontWeight = FontWeight.Medium, color = scheme.onSurface)
                    Text("${fmtM(g.savedM)} / ${fmtM(g.totalM)}", color = scheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            val raw = if (g.totalM > 0f) g.savedM / g.totalM else 0f
            ContinuousLinearProgress(
                progress = raw.coerceIn(0f, 1f),
                color = g.color,
                trackColor = scheme.surfaceVariant,
                height = 8.dp,
                corner = 6.dp
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val pct = (raw.coerceIn(0f, 1f) * 100f).roundToInt()
                Text("$pct% hoàn thành", color = scheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, tint = scheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text("${g.daysRemain} ngày", color = scheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SuggestTileView(
    icon: String,
    title: String,
    sub: String,
    danger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedCard(
        modifier = modifier.heightIn(min = 96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = scheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(icon) }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = scheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = sub,
                color = if (danger) scheme.error else scheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ContinuousLinearProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    height: Dp,
    corner: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(color)
        )
    }
}

private fun fmtM(v: Float): String {
    val r = (kotlin.math.round(v * 10f) / 10f)
    val s = if (r % 1f == 0f) r.toInt().toString() else String.format(java.util.Locale.US, "%.1f", r)
    return "${s}M"
}
