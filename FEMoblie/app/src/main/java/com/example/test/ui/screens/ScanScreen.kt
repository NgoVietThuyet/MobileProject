@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.test.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.test.R
import com.example.test.ui.theme.AppGradient
import java.io.File

@Composable
fun ReceiptScanScreen(
    onBack: () -> Unit = {},
    onCaptured: (Uri) -> Unit = {},
    onCaptureError: (String) -> Unit = {},
    showScanTips: Boolean = true,
    showPermissionTexts: Boolean = false
) {
    val appBarHeight = 36.dp
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    val ctx = LocalContext.current

    // launcher mở thư viện, không cần quyền
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let(onCaptured) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var cameraController by remember { mutableStateOf<LifecycleCameraController?>(null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9D9D9).copy(alpha = 0.6f),
                    scrolledContainerColor = Color(0xFFD9D9D9).copy(alpha = 0.6f)
                ),
                windowInsets = WindowInsets(0),
                scrollBehavior = scroll,
                modifier = Modifier.height(appBarHeight)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(
                    start = pad.calculateStartPadding(LayoutDirection.Ltr),
                    end = pad.calculateEndPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    bottom = pad.calculateBottomPadding()
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // TOP 60%
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                        .background(Color(0xFF0B1220))
                        .padding(horizontal = 20.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = appBarHeight + 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Quay lại",
                            tint = Color(0xFFE5E7EB)
                        )
                    }

                    CameraCard(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(180.dp),
                        onControllerReady = { cameraController = it },
                        showPermissionTexts = showPermissionTexts
                    )
                }

                // BOTTOM 40%
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .background(Color(0xFF0F172A))
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        if (showScanTips) {
                            OutlinedCard(
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1).copy(alpha = 0.6f)),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0B1220)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Mẹo quét hiệu quả:", color = Color(0xFFE5E7EB), fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(10.dp))
                                    Tip("Đặt hóa đơn phẳng và đủ sáng")
                                    Tip("Đảm bảo toàn bộ hóa đơn nằm trong khung")
                                    Tip("Giữ máy ổn định khi quét")
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        cameraController?.let { ctrl ->
                            takePhoto(
                                context = ctx,
                                controller = ctrl,
                                onSaved = onCaptured,
                                onError = { onCaptureError(it.message ?: "Lỗi chụp ảnh") }
                            )
                        } ?: takePhoto(
                            context = ctx,
                            imageCapture = imageCapture,
                            onSaved = onCaptured,
                            onError = { onCaptureError(it.message ?: "Lỗi chụp ảnh") }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(AppGradient.BluePurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Chụp & quét", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { pickImage.launch("image/*") },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0B1220)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_upload),
                        contentDescription = "Tải ảnh",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraCard(
    modifier: Modifier = Modifier,
    onControllerReady: (LifecycleCameraController) -> Unit,
    showPermissionTexts: Boolean = false
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val reqPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    OutlinedCard(
        border = BorderStroke(1.dp, Color(0xFFCBD5E1).copy(alpha = 0.6f)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        if (!granted) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(painterResource(R.drawable.ic_camera), null, tint = Color(0xFFCBD5E1))
                if (showPermissionTexts) {
                    Spacer(Modifier.height(12.dp))
                    Text("Đặt hóa đơn vào khung hình", color = Color(0xFFE5E7EB), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Cần cấp quyền camera", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { reqPermission.launch(Manifest.permission.CAMERA) }) {
                    Text("Cho phép camera")
                }
            }
        } else {
            val controller = remember(ctx, lifecycleOwner) {
                LifecycleCameraController(ctx).apply {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                    bindToLifecycle(lifecycleOwner)
                }
            }

            LaunchedEffect(controller) { onControllerReady(controller) }

            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        this.controller = controller
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }
    }
}

private fun takePhoto(
    context: android.content.Context,
    controller: LifecycleCameraController,
    onSaved: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val file = File.createTempFile("scan_", ".jpg", context.cacheDir)
    val opts = ImageCapture.OutputFileOptions.Builder(file).build()
    controller.takePicture(
        opts,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) = onError(exc)
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                onSaved(result.savedUri ?: Uri.fromFile(file))
            }
        }
    )
}

private fun takePhoto(
    context: android.content.Context,
    imageCapture: ImageCapture,
    onSaved: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val file = File.createTempFile("scan_", ".jpg", context.cacheDir)
    val output = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        output,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) = onError(exc)
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSaved(outputFileResults.savedUri ?: Uri.fromFile(file))
            }
        }
    )
}

@Composable
private fun Tip(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF94A3B8))
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color(0xFF94A3B8), fontSize = 13.sp, textAlign = TextAlign.Start)
    }
    Spacer(Modifier.height(6.dp))
}
