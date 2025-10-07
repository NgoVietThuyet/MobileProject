@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.theme.AppGradient
import java.util.Locale

@Composable
fun ProfilePictureScreen(
    onBack: () -> Unit = {},
    onSave: (uri: Uri?, initials: String, bg: Brush) -> Unit = { _, _, _ -> },
    initialInitials: String = "NA",
    initialGradientIndex: Int = 0
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val appBarHeight = 36.dp

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
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                )
        ) {
            Spacer(Modifier.height(52.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Quay lại",
                        tint = Color.Unspecified
                    )
                }
                Text(
                    "Thay ảnh đại diện",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                GradientButton(
                    onClick = {
                        onSave(
                            selectedUri,
                            initials.trim().uppercase(Locale.getDefault()),
                            selectedBrush
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        contentDescription = "Lưu",
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Lưu", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                color = Color.White,
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
                                    color = Color.White,
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
                                color = Color(0xFFD9D9D9),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                shadowElevation = 0.dp,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_camera),
                                        contentDescription = "Sửa ảnh",
                                        tint = Color(0xFF111827),
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
                    Text("Chọn ảnh đại diện mới", color = Color(0xFF6B7280))
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
                Text("Nhập chữ cái (tối đa 2 ký tự)", color = Color(0xFF6B7280), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = initials,
                    onValueChange = { value -> initials = value.take(2) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
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

// helpers //

@Composable
private fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(brush = AppGradient.BluePurple)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DashedUploadBox(modifier: Modifier = Modifier) {
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
                    color = Color(0xFFCBD5E1),
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = Color(0xFF64748B))
            Spacer(Modifier.height(6.dp))
            Text("Nhấn để chọn ảnh", color = Color(0xFF64748B), fontSize = 13.sp)
        }
    }
}

@Composable
private fun GradientDot(brush: Brush, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) BorderStroke(2.dp, Color(0xFF111827)) else BorderStroke(1.dp, Color(0xFFE5E7EB))
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
