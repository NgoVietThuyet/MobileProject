package com.example.test.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.test.data.TransactionRepo
import com.example.test.ui.components.AppHeader
import com.example.test.ui.scan.UploadResult
import com.example.test.vm.AddTransactionViewModel
import com.example.test.vm.SaveStatus
import com.google.gson.Gson
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavHostController,
    onBack: () -> Unit = {},
    addTxViewModel: AddTransactionViewModel = hiltViewModel(),
    transactionRepo: TransactionRepo = hiltViewModel<AddTransactionViewModel>().repository
) {
    val scheme = MaterialTheme.colorScheme
    val saved = navController.currentBackStackEntry?.savedStateHandle
    val context = LocalContext.current
    val saveState by addTxViewModel.uiState.collectAsStateWithLifecycle()

    val flow = remember(saved) { saved?.getStateFlow("upload_result", null as UploadResult?) }
    val result by (flow?.collectAsState() ?: remember { mutableStateOf<UploadResult?>(null) })
    LaunchedEffect(result) { if (result != null) saved?.remove<UploadResult>("upload_result") }

    val payloadFlow = remember(saved) { saved?.getStateFlow("scan_payload", null as String?) }
    val payloadJson by (payloadFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
    LaunchedEffect(payloadJson) { if (payloadJson != null) saved?.remove<String>("scan_payload") }

    val api = remember(payloadJson) {
        payloadJson?.let { runCatching { Gson().fromJson(it, UploadApiResp::class.java) }.getOrNull() }
    }
    val tx = api?.transactions?.firstOrNull()

    val categoryMap = remember { transactionRepo.getLocalCategories().associateBy { it.categoryId } }

    LaunchedEffect(saveState.saveStatus) {
        when (saveState.saveStatus) {
            SaveStatus.SUCCESS -> {
                Toast.makeText(context, "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show()
                addTxViewModel.resetStatus()
                onBack() // Quay lại màn hình trước
            }
            SaveStatus.ERROR -> {
                Toast.makeText(context, "Lỗi lưu: ${saveState.errorMessage}", Toast.LENGTH_LONG).show()
                addTxViewModel.resetStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "Kết quả quét hóa đơn", showBack = true, onBack = onBack) },
        bottomBar = {
            if (tx != null) {
                Divider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val amount = tx.amount ?: "0"
                            val categoryId = tx.categoryId ?: ""
                            val note = tx.note ?: ""
                            val type = tx.type ?: "Expense"
                            val dateMillis = tx.createdDate?.let { parseScanDate(it) } ?: System.currentTimeMillis()

                            addTxViewModel.saveTransaction(amount, categoryId, note, dateMillis, type)
                        },
                        enabled = saveState.saveStatus != SaveStatus.LOADING,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34A853),
                            contentColor = Color.White
                        )
                    ) {
                        if (saveState.saveStatus == SaveStatus.LOADING) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Lưu giao dịch", fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("scan") { popUpTo(navController.graph.startDestinationId){inclusive = true} } },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, scheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = scheme.surface)
                    ) { Text("Quét lại") }
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(
                    start = inner.calculateStartPadding(LayoutDirection.Ltr),
                    end = inner.calculateEndPadding(LayoutDirection.Ltr),
                    top = inner.calculateTopPadding(),
                    bottom = inner.calculateBottomPadding()
                )
        ) {
            Spacer(Modifier.height(12.dp))

            if (tx == null) {
                val success = api?.success ?: result?.success ?: false
                val message = api?.message ?: result?.message
                StatusBanner(success = success, message = message)
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Chưa có chi tiết giao dịch", color = scheme.onBackground)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.navigate("scan") }) { Text("Quét hoá đơn") }
                }
            } else {
                StatusBanner(success = api?.success ?: true, message = api?.message)
                Spacer(Modifier.height(16.dp))

                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                    colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Thông tin giao dịch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
                        Spacer(Modifier.height(12.dp))

                        val categoryName = categoryMap[tx.categoryId]?.name ?: tx.categoryId ?: "—"
                        KeyValueRow("Danh mục", categoryName, valueColor = scheme.onSurface)

                        KeyValueRow(
                            "Loại",
                            when (tx.type?.lowercase(Locale.ROOT)) {
                                "expense", "chi", "expense".uppercase() -> "Chi tiêu"
                                "income", "thu", "income".uppercase() -> "Thu nhập"
                                else -> tx.type ?: "—"
                            },
                            valueColor = scheme.onSurface
                        )
                        KeyValueRow(
                            "Tổng tiền",
                            formatVn(parseAmount(tx.amount)),
                            valueColor = if(tx.type?.equals("income", ignoreCase=true)==true) Color(0xFF34A853) else Color(0xFFDC2626) ,
                            boldValue = true
                        )
                        KeyValueRow("Ngày", tx.createdDate ?: "—", valueColor = scheme.onSurface)
                        KeyValueRow("Ghi chú", tx.note ?: "—", valueColor = scheme.onSurface)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseScanDate(dateString: String): Long {
    val formatters = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
    for (formatter in formatters) {
        try {
            return LocalDateTime.parse(dateString, formatter)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: DateTimeParseException) {
        }
    }
    return System.currentTimeMillis()
}

@Composable
private fun StatusBanner(success: Boolean, message: String?) {
    val okBg = Color(0xFFDCFCE7)
    val okFg = Color(0xFF166534)
    val errBg = Color(0xFFFFE4E6)
    val errFg = Color(0xFF991B1B)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (success) okBg else errBg,
        border = BorderStroke(1.dp, (if (success) okFg else errFg).copy(alpha = 0.25f)),
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (success) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                contentDescription = null,
                tint = if (success) okFg else errFg
            )
            Spacer(Modifier.width(8.dp))
            Text(text = if (success) "Quét thành công" else "Quét thất bại", color = if (success) okFg else errFg, fontWeight = FontWeight.SemiBold)
            if (!message.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(text = message, color = (if (success) okFg else errFg).copy(alpha = 0.8f))
            }
        }
    }
}
private fun formatVn(v: Long): String =
    NumberFormat.getInstance(Locale("vi", "VN")).format(v) + "đ"

@Composable
private fun KeyValueRow(
    key: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    boldValue: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(key, color = scheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, color = valueColor, fontWeight = if (boldValue) FontWeight.SemiBold else null)
    }
}

private fun parseAmount(s: String?): Long =
    s?.filter { it.isDigit() }?.toLongOrNull() ?: 0L

data class UploadApiResp(
    val success: Boolean,
    val message: String?,
    val transactions: List<UploadTx> = emptyList()
)

data class UploadTx(
    val transactionId: String?,
    val userId: String?,
    val categoryId: String?,
    val type: String?,
    val amount: String?,
    val note: String?,
    val createdDate: String?,
    val updatedDate: String?
)

