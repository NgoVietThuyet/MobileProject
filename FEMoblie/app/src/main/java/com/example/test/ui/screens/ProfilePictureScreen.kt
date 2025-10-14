@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.components.AppHeader
import com.example.test.ui.theme.AppGradient
import java.util.Locale

@Composable
fun ProfilePictureScreen(
    onBack: () -> Unit = {},
    onSave: (uri: Uri?, initials: String, bg: Brush) -> Unit = { _, _, _ -> },
    initialInitials: String = "NA",
    initialGradientIndex: Int = 0
) {
    val scheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var initials by remember { mutableStateOf(initialInitials) }

    val gradients = listOf(
        AppGradient.BluePurple,
        AppGradient.BlueGreen,
        AppGradient.PurplePink,
        AppGradient.OrangePink,
        AppGradient.TealBlue,
        AppGradient.AmberRose
    )
    var selectedIndex by remember { mutableStateOf(initialGradientIndex.coerceIn(0, gradients.lastIndex)) }
    val selectedBrush = gradients[selectedIndex]

    val photoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        selectedUri = uri
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppHeader(
                title = "Thay ảnh đại diện",
                showBack = true,
                onBack = onBack,
                actions = {
                    SaveButton(
                        onClick = {
                            onSave(
                                selectedUri,
                                initials.take(2).uppercase(Locale.getDefault()).ifBlank { "NA" },
                                selectedBrush
                            )
                        }
                    )
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(innerPadding)
        ) {
            Spacer(Modifier.height(52.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant),
                color = scheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedUri != null) {
                                AsyncImage(
                                    model = selectedUri,
                                    contentDescription = "Ảnh đại diện",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .background(selectedBrush)
                                )
                                Text(
                                    initials.take(2).uppercase(Locale.getDefault()).ifBlank { "NA" },
                                    color = scheme.onPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        var menuExpanded by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                            Surface(
                                onClick = { menuExpanded = true },
                                shape = CircleShape,
                                color = scheme.surfaceVariant,
                                border = BorderStroke(1.dp, scheme.outlineVariant),
                                shadowElevation = 0.dp,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_camera),
                                        contentDescription = "Sửa ảnh",
                                        tint = scheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chọn ảnh") },
                                    leadingIcon = { Icon(Icons.Outlined.Photo, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Xóa ảnh") },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        selectedUri = null
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Chọn ảnh đại diện mới", color = scheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Tải ảnh lên") {
                DashedUploadBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                            )
                        }
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Chọn 2 chữ") {
                Text("Nhập chữ cái (tối đa 2 ký tự)", color = scheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = initials,
                    onValueChange = { value -> initials = value.take(2) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = scheme.surface,
                        unfocusedContainerColor = scheme.surface,
                        disabledContainerColor = scheme.surface,
                        focusedBorderColor = scheme.primary,
                        unfocusedBorderColor = scheme.outlineVariant,
                        disabledBorderColor = scheme.outlineVariant,
                        cursorColor = scheme.primary
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard(title = "Chọn màu") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        (0..2).forEach { i ->
                            GradientDot(
                                brush = gradients[i],
                                selected = selectedIndex == i
                            ) { selectedIndex = i }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        (3..5).forEach { i ->
                            GradientDot(
                                brush = gradients[i],
                                selected = selectedIndex == i
                            ) { selectedIndex = i }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(shape)
            .background(brush = AppGradient.BluePurple, shape = shape)
            .border(0.75.dp, scheme.outlineVariant.copy(alpha = 0.6f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.ic_save),
                contentDescription = "Lưu",
                tint = scheme.onPrimary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Lưu",
                color = scheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        color = scheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DashedUploadBox(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .drawWithContent {
                drawContent()
                val stroke = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                )
                drawRoundRect(
                    color = scheme.outlineVariant,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = scheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text("Nhấn để chọn ảnh", color = scheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun GradientDot(brush: Brush, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val border = if (selected) BorderStroke(2.dp, scheme.onSurface) else BorderStroke(1.dp, scheme.outlineVariant)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        border = border,
        modifier = Modifier.size(56.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(brush)
        )
    }
}
