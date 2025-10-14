// ScanResultScreen.kt
package com.example.test.ui.screens

import UploadResult
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.test.ui.components.AppHeader
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavHostController,
    onDone: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val saved = navController.currentBackStackEntry?.savedStateHandle

    // KQ điều hướng cũ
    val flow = remember(saved) { saved?.getStateFlow("upload_result", null as UploadResult?) }
    val result by (flow?.collectAsState() ?: remember { mutableStateOf<UploadResult?>(null) })
    LaunchedEffect(result) { if (result != null) saved?.remove<UploadResult>("upload_result") }

    // JSON đầy đủ từ API: set("scan_payload", json) ở nơi upload
    val payloadFlow = remember(saved) { saved?.getStateFlow("scan_payload", null as String?) }
    val payloadJson by (payloadFlow?.collectAsState() ?: remember { mutableStateOf<String?>(null) })
    LaunchedEffect(payloadJson) { if (payloadJson != null) saved?.remove<String>("scan_payload") }

    val api = remember(payloadJson) {
        payloadJson?.let { runCatching { Gson().fromJson(it, UploadApiResp::class.java) }.getOrNull() }
    }
    val tx = api?.transactions?.firstOrNull()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Kết quả quét hóa đơn",
                showBack = true,
                onBack = onBack
            )
        },
        bottomBar = {
            if (result != null) {
                Divider()
                Row(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34A853),
                            contentColor = Color.White
                        )
                    ) { Text("Lưu giao dịch", fontWeight = FontWeight.Medium) }

                    Spacer(Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("scan") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
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

            when (val r = result) {
                null -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Chưa có kết quả")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { navController.navigate("scan") }) { Text("Quét hoá đơn") }
                    }
                }
                else -> {
                    // Banner dùng ưu tiên trạng thái từ API nếu có
                    StatusBanner(success = api?.success ?: r.success, message = api?.message ?: r.message)
                    Spacer(Modifier.height(16.dp))

                    // Card thông tin từ API nếu có, nếu không fallback từ UploadResult->ReceiptUi
                    if (tx != null) {
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Thông tin giao dịch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(12.dp))
                                KeyValueRow("Danh mục", tx.categoryId ?: "—")
                                KeyValueRow(
                                    "Loại",
                                    when (tx.type) {
                                        "expense" -> "Chi tiêu"
                                        "income" -> "Thu nhập"
                                        else -> tx.type ?: "—"
                                    }
                                )
                                KeyValueRow(
                                    "Tổng tiền",
                                    tx.amount?.let(::formatVn) ?: "—",
                                    valueColor = Color(0xFFDC2626),
                                    boldValue = true
                                )
                                KeyValueRow("Ngày", tx.createdDate ?: "—")
                                KeyValueRow("Ghi chú", tx.note ?: "—")
                            }
                        }
                    } else {
                        val ui = r.toReceiptUiOrNull()
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Thông tin giao dịch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(12.dp))
                                KeyValueRow("Cửa hàng", ui?.store ?: "-")
                                KeyValueRow(
                                    "Tổng tiền",
                                    ui?.total?.let(::formatVn) ?: "-",
                                    valueColor = Color(0xFFDC2626),
                                    boldValue = true
                                )
                                KeyValueRow("Ngày", ui?.date ?: "-")
                                KeyChipRow("Danh mục", ui?.category ?: "—")
                            }
                        }
                    }

                    val uiItems = r.toReceiptUiOrNull()?.items.orEmpty()
                    if (uiItems.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            colors = CardDefaults.outlinedCardColors(containerColor = scheme.surface),
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Chi tiết sản phẩm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(12.dp))
                                uiItems.forEachIndexed { i, it ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(it.name, modifier = Modifier.weight(1f))
                                        Text(formatVn(it.price), fontWeight = FontWeight.SemiBold)
                                    }
                                    if (i != uiItems.lastIndex) Divider(color = scheme.outlineVariant)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
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
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (success) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                contentDescription = null,
                tint = if (success) okFg else errFg
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (success) "Quét thành công" else "Quét thất bại",
                color = if (success) okFg else errFg,
                fontWeight = FontWeight.SemiBold
            )
            if (!message.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(text = message, color = (if (success) okFg else errFg).copy(alpha = 0.8f))
            }
        }
    }
}

/* ---------- helpers ---------- */

private data class ReceiptUi(
    val store: String,
    val total: Long,
    val date: String,
    val category: String,
    val items: List<LineItemUi>
)

private data class LineItemUi(val name: String, val price: Long)

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
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(key, color = scheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, color = valueColor, fontWeight = if (boldValue) FontWeight.SemiBold else null)
    }
}

@Composable
private fun KeyChipRow(key: String, chip: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(key, color = scheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = scheme.primary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.16f))
        ) { Text(chip, color = scheme.primary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }
    }
}

@Suppress("UNCHECKED_CAST")
private fun UploadResult.toReceiptUiOrNull(): ReceiptUi? {
    val tr = try {
        val f = this::class.members.firstOrNull { it.name == "transaction" } ?: return null
        f.call(this)
    } catch (_: Throwable) { return null } ?: return null

    fun anyToLong(a: Any?): Long = when (a) {
        is Number -> a.toLong()
        is String -> a.filter { it.isDigit() }.toLongOrNull() ?: 0L
        else -> 0L
    }

    val km = runCatching { tr as Map<String, *> }.getOrNull()
    val store = (km?.get("store") as? String).orEmpty()
    val total = anyToLong(km?.get("total"))
    val date = (km?.get("date") as? String).orEmpty()
    val category = (km?.get("category") as? String).orEmpty()

    val itemsAny = km?.get("items")
    val items = when (itemsAny) {
        is List<*> -> itemsAny.mapNotNull {
            val m = it as? Map<*, *> ?: return@mapNotNull null
            val name = (m["name"] as? String).orEmpty()
            val price = anyToLong(m["price"])
            LineItemUi(name, price)
        }
        else -> emptyList()
    }

    return ReceiptUi(store, total, date, category, items)
}

data class UploadApiResp(
    val success: Boolean,
    val message: String?,
    val transactions: List<UploadTx>?
)
data class UploadTx(
    val transactionId: String?,
    val userId: String?,
    val categoryId: String?,
    val type: String?,
    val amount: Long?,
    val note: String?,
    val createdDate: String?,
    val updatedDate: String?
)
