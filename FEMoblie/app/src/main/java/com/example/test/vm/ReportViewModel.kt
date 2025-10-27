package com.example.test.vm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.BudgetApi
import com.example.test.ui.api.ReportApi
import com.example.test.ui.models.BudgetDto
import com.example.test.data.TransactionRepo
import com.example.test.ui.mock.TxType
import com.example.test.ui.mock.TxUi
import com.example.test.ui.models.CategoryDto
import com.example.test.ui.models.TransactionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.ResponseBody
import retrofit2.Response

data class CategoryPieData(
    val label: String,
    val value: Float
)

data class ReportUiState(
    val isLoading: Boolean = true,
    val currentPeriod: Int = 1,

    val kpiIncome: Long = 0L,
    val kpiExpense: Long = 0L,

    val overviewLabels: List<String> = emptyList(),
    val overviewIncome: List<Float> = emptyList(),
    val overviewExpense: List<Float> = emptyList(),

    val expensePieData: List<CategoryPieData> = emptyList(),
    val budgetPieData: List<CategoryPieData> = emptyList(),

    val trendLabels: List<String> = emptyList(),
    val trendExpense: List<Float> = emptyList(),

    val error: String? = null,

    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val exportErrorMessage: String? = null,

    val insightCard1: String? = null,
    val insightCard2: String? = null,
    val insightCard3: String? = null
)

private val apiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
private val budgetDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val budgetApi: BudgetApi,
    private val reportApi: ReportApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var allTransactions: List<TxUi> = emptyList()
    private var allBudgets: List<BudgetDto> = emptyList()
    private var categoryMap: Map<String, CategoryDto> = emptyMap()
    private val zone = ZoneId.systemDefault()

    init {
        fetchAllData()
    }

    fun setPeriod(newPeriod: Int) {
        if (newPeriod == _uiState.value.currentPeriod) return

        _uiState.update { it.copy(currentPeriod = newPeriod) }
        recalculateCharts()
    }

    private fun fetchAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val transactionResponse = transactionRepo.getAllFromAuth()
                val categories = transactionRepo.getLocalCategories()
                categoryMap = categories.associateBy { it.categoryId }

                val uid = AuthStore.userId.orEmpty()
                val budgetResponse = budgetApi.getAllBudgets(uid)

                if (transactionResponse.isSuccessful && budgetResponse.isSuccessful) {
                    val dtos = transactionResponse.body()?.transactions ?: emptyList()
                    allTransactions = dtos.map { it.toTxUi(categoryMap) }

                    allBudgets = budgetResponse.body().orEmpty()

                    recalculateCharts()
                } else {
                    var errorMsg = ""
                    if (!transactionResponse.isSuccessful) errorMsg += "Lỗi tải giao dịch. "
                    if (!budgetResponse.isSuccessful) errorMsg += "Lỗi tải ngân sách."
                    _uiState.update { it.copy(error = errorMsg.trim()) }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Lỗi fetch: ${e.message}", e)
                _uiState.update { it.copy(error = "Đã xảy ra lỗi.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun recalculateCharts() {
        if (allTransactions.isEmpty() && allBudgets.isEmpty()) return

        val period = _uiState.value.currentPeriod
        val today = LocalDate.now(zone)

        val groupedByDay = allTransactions.groupBy { it.dateTime.toLocalDate() }

        fun sumFor(d: LocalDate, isIncome: Boolean): Pair<Long, Float> {
            val sum = groupedByDay[d]
                ?.filter { (if (isIncome) it.type == TxType.INCOME else it.type == TxType.EXPENSE) }
                ?.sumOf { it.amount.toLongOrNull() ?: 0L } ?: 0L
            return Pair(sum, sum / 1_000_000f)
        }

        when (period) {
            2 -> {
                val year = today.year
                val months = (1..12).map { Month.of(it) }
                val labels = months.map { "T${it.value}" }

                val groupedByMonth = allTransactions.filter { it.dateTime.year == year }.groupBy { it.dateTime.month }
                val incomes = mutableListOf<Float>()
                val expenses = mutableListOf<Float>()
                var totalIncome = 0L
                var totalExpense = 0L
                for (month in months) {
                    val monthTxs = groupedByMonth[month] ?: emptyList()
                    val monthIncome = monthTxs.filter { it.type == TxType.INCOME }.sumOf { it.amount.toLongOrNull() ?: 0L }
                    val monthExpense = monthTxs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount.toLongOrNull() ?: 0L }
                    incomes.add(monthIncome / 1_000_000f)
                    expenses.add(monthExpense / 1_000_000f)
                    totalIncome += monthIncome
                    totalExpense += monthExpense
                }

                val pieTxs = allTransactions.filter { it.dateTime.year == year }
                val expensePie = calculateExpensePieData(pieTxs)
                val periodBudgets = allBudgets.filter {
                    try { LocalDateTime.parse(it.startDate, budgetDateFormatter).toLocalDate().year == year }
                    catch (e: DateTimeParseException) { false }
                }
                val budgetPie = calculateBudgetPieData(periodBudgets)

                val insight1 = calculateInsightCard1(periodBudgets)
                val insight2 = calculateInsightCard2(pieTxs)
                val insight3 = calculateInsightCard3(expensePie, totalExpense)

                _uiState.update { it.copy(
                    overviewLabels = labels,
                    overviewIncome = incomes,
                    overviewExpense = expenses,
                    trendLabels = labels,
                    trendExpense = expenses,
                    expensePieData = expensePie,
                    budgetPieData = budgetPie,
                    kpiIncome = totalIncome,
                    kpiExpense = totalExpense,
                    insightCard1 = insight1,
                    insightCard2 = insight2,
                    insightCard3 = insight3
                )}
            }

            1 -> {
                val currentMonth = today.month
                val currentYear = today.year
                val labels = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4")

                val firstDay = today.withDayOfMonth(1)
                val dayRanges = listOf(firstDay..firstDay.plusDays(6), firstDay.plusDays(7)..firstDay.plusDays(13), firstDay.plusDays(14)..firstDay.plusDays(20), firstDay.plusDays(21)..today.withDayOfMonth(today.lengthOfMonth()))
                val incomes = mutableListOf<Float>()
                val expenses = mutableListOf<Float>()
                var totalIncome = 0L
                var totalExpense = 0L
                for (range in dayRanges) {
                    var weekIncome = 0L
                    var weekExpense = 0L
                    var currentDay = range.start
                    while (currentDay <= range.endInclusive) {
                        if (currentDay.month == currentMonth) {
                            val (dayIncome, _) = sumFor(currentDay, true)
                            val (dayExpense, _) = sumFor(currentDay, false)
                            weekIncome += dayIncome
                            weekExpense += dayExpense
                        }
                        currentDay = currentDay.plusDays(1)
                    }
                    incomes.add(weekIncome / 1_000_000f)
                    expenses.add(weekExpense / 1_000_000f)
                    totalIncome += weekIncome
                    totalExpense += weekExpense
                }

                val pieTxs = allTransactions.filter { it.dateTime.month == currentMonth && it.dateTime.year == currentYear }
                val expensePie = calculateExpensePieData(pieTxs)
                val periodBudgets = allBudgets.filter {
                    try {
                        val d = LocalDateTime.parse(it.startDate, budgetDateFormatter).toLocalDate()
                        d.month == currentMonth && d.year == currentYear
                    } catch (e: DateTimeParseException) { false }
                }
                val budgetPie = calculateBudgetPieData(periodBudgets)

                val insight1 = calculateInsightCard1(periodBudgets)
                val insight2 = calculateInsightCard2(pieTxs)
                val insight3 = calculateInsightCard3(expensePie, totalExpense)

                _uiState.update { it.copy(
                    overviewLabels = labels,
                    overviewIncome = incomes,
                    overviewExpense = expenses,
                    trendLabels = labels,
                    trendExpense = expenses,
                    expensePieData = expensePie,
                    budgetPieData = budgetPie,
                    kpiIncome = totalIncome,
                    kpiExpense = totalExpense,
                    insightCard1 = insight1,
                    insightCard2 = insight2,
                    insightCard3 = insight3
                )}
            }

            else -> {
                val days = (5 downTo 0).map { today.minusDays(it.toLong()) }
                val fmt = DateTimeFormatter.ofPattern("dd/MM")
                val overviewLbls = days.map { it.format(fmt) }
                val incomes = days.map { sumFor(it, true).second }
                val expenses = days.map { sumFor(it, false).second }

                val startOfWeek = today.with(DayOfWeek.MONDAY)
                val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
                val trendLbls = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                val trendData = weekDays.map { sumFor(it, false).second }
                var totalIncome = 0L
                var totalExpense = 0L
                weekDays.forEach {
                    totalIncome += sumFor(it, true).first
                    totalExpense += sumFor(it, false).first
                }

                val pieTxs = allTransactions.filter { it.dateTime.toLocalDate() in weekDays }
                val expensePie = calculateExpensePieData(pieTxs)
                val periodBudgets = allBudgets.filter {
                    try { LocalDateTime.parse(it.startDate, budgetDateFormatter).toLocalDate() in weekDays }
                    catch (e: DateTimeParseException) { false }
                }
                val budgetPie = calculateBudgetPieData(periodBudgets)

                val insight1 = calculateInsightCard1(periodBudgets)
                val insight2 = calculateInsightCard2(pieTxs)
                val insight3 = calculateInsightCard3(expensePie, totalExpense)

                _uiState.update { it.copy(
                    overviewLabels = overviewLbls,
                    overviewIncome = incomes,
                    overviewExpense = expenses,
                    trendLabels = trendLbls,
                    trendExpense = trendData,
                    expensePieData = expensePie,
                    budgetPieData = budgetPie,
                    kpiIncome = totalIncome,
                    kpiExpense = totalExpense,
                    insightCard1 = insight1,
                    insightCard2 = insight2,
                    insightCard3 = insight3
                )}
            }
        }
    }

    private fun calculateInsightCard1(budgets: List<BudgetDto>): String {
        val overspentCount = budgets.count { (toMoney(it.currentAmount) ?: 0.0) < 0.0 }
        return if (overspentCount > 0) {
            "Bạn đã chi tiêu vượt $overspentCount ngân sách trong kỳ này."
        } else {
            "Tốt! Bạn chưa vượt ngân sách nào trong kỳ này."
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateInsightCard2(expenseTxs: List<TxUi>): String {
        if (expenseTxs.isEmpty()) return "Không có dữ liệu chi tiêu trong kỳ."

        val topDay = expenseTxs
            .groupBy { it.dateTime.dayOfWeek }
            .mapValues { (_, txs) -> txs.sumOf { it.amount.toLongOrNull() ?: 0L } }
            .maxByOrNull { it.value }
            ?.key

        return if (topDay != null) {
            "Bạn chi tiêu nhiều nhất vào ${topDay.toVietnamese()}."
        } else {
            "Không có chi tiêu trong kỳ này."
        }
    }

    private fun calculateInsightCard3(expensePie: List<CategoryPieData>, totalExpense: Long): String {
        val topCategory = expensePie.firstOrNull()

        val totalExpenseFloat = totalExpense / 1_000_000f

        if (topCategory == null || topCategory.value <= 0 || totalExpenseFloat <= 0) {
            return "Bạn chưa có chi tiêu nào trong kỳ này."
        }

        val percentage = (topCategory.value / totalExpenseFloat * 100).toInt()

        return "Danh mục “${topCategory.label}” chiếm ${percentage}% tổng chi tiêu."
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun DayOfWeek.toVietnamese(): String {
        return when (this) {
            DayOfWeek.MONDAY -> "Thứ Hai"
            DayOfWeek.TUESDAY -> "Thứ Ba"
            DayOfWeek.WEDNESDAY -> "Thứ Tư"
            DayOfWeek.THURSDAY -> "Thứ Năm"
            DayOfWeek.FRIDAY -> "Thứ Sáu"
            DayOfWeek.SATURDAY -> "Thứ Bảy"
            DayOfWeek.SUNDAY -> "Chủ Nhật"
        }
    }

    private fun calculateExpensePieData(transactions: List<TxUi>): List<CategoryPieData> {
        val byCat = transactions
            .filter { it.type == TxType.EXPENSE }
            .groupBy { it.title }
            .mapValues { entry -> entry.value.sumOf { it.amount.toLongOrNull() ?: 0L } }

        return byCat.entries.sortedByDescending { it.value }
            .map { (label, sum) ->
                CategoryPieData(label, sum / 1_000_000f)
            }
    }

    private fun calculateBudgetPieData(budgets: List<BudgetDto>): List<CategoryPieData> {
        val byCat = budgets
            .groupBy { it.categoryId }
            .mapValues { entry ->
                entry.value.sumOf { toMoney(it.currentAmount) }
            }

        return byCat.entries.sortedByDescending { it.value }
            .map { (catId, sum) ->
                val label = categoryMap[catId]?.name ?: "Ngân sách khác"
                CategoryPieData(label, (sum / 1_000_000.0).toFloat())
            }
    }

    private fun toMoney(value: String?): Double {
        if (value.isNullOrBlank()) return 0.0
        var s = value.trim()
        val lower = s.lowercase(Locale.US)
        val isMillionUnit = lower.contains("m") || lower.contains("triệu")
        s = s.replace("[₫đvnd\\s]".toRegex(RegexOption.IGNORE_CASE), "")
        s = s.replace("[^0-9,.]".toRegex(), "")
        s = normalizeDecimal(s)
        val v = s.toDoubleOrNull() ?: return 0.0
        return if (isMillionUnit) v * 1_000_000.0 else v
    }

    private fun normalizeDecimal(raw: String): String {
        val hasComma = raw.contains(',')
        val hasDot = raw.contains('.')
        if (hasComma && hasDot) {
            val lc = raw.lastIndexOf(',')
            val ld = raw.lastIndexOf('.')
            return if (lc > ld) raw.replace(".", "").replace(',', '.') else raw.replace(",", "")
        }
        if (hasComma) return if (raw.count { it == ',' } > 1) raw.replace(",", "") else raw.replace(',', '.')
        if (hasDot) return if (raw.count { it == '.' } > 1) raw.replace(".", "") else raw
        return raw
    }

    fun exportExcelReport(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportErrorMessage = null, exportSuccessMessage = null) }
            val userId = AuthStore.userId.orEmpty()
            if (userId.isBlank()) {
                _uiState.update { it.copy(isExporting = false, exportErrorMessage = "Bạn chưa đăng nhập.") }
                return@launch
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val startStr = startDate.format(formatter)
            val endStr = endDate.format(formatter)
            try {
                val response: Response<ResponseBody> = reportApi.exportReport(
                    userId = userId,
                    startDate = startStr,
                    endDate = endStr
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val contentDisposition = response.headers()["content-disposition"]
                    val filename = contentDisposition
                        ?.substringAfter("filename=\"", "")
                        ?.substringBefore("\"", "BaoCaoTaiChinh.xlsx") ?: "BaoCaoTaiChinh_${startStr}_${endStr}.xlsx"
                    saveFileToDownloads(body, filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    _uiState.update { it.copy(isExporting = false, exportSuccessMessage = "Đã lưu file: $filename") }
                } else {
                    _uiState.update { it.copy(isExporting = false, exportErrorMessage = "Lỗi: ${response.code()}") }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Export Excel failed", e)
                _uiState.update { it.copy(isExporting = false, exportErrorMessage = e.message ?: "Export Excel thất bại") }
            }
        }
    }

    fun exportPdfReport(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportErrorMessage = null, exportSuccessMessage = null) }
            val userId = AuthStore.userId.orEmpty()
            if (userId.isBlank()) {
                _uiState.update { it.copy(isExporting = false, exportErrorMessage = "Bạn chưa đăng nhập.") }
                return@launch
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val startStr = startDate.format(formatter)
            val endStr = endDate.format(formatter)
            try {
                val response: Response<ResponseBody> = reportApi.exportReportPdf(
                    userId = userId,
                    startDate = startStr,
                    endDate = endStr
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val filename = "BaoCaoTaiChinh_${startStr}_${endStr}.pdf"
                    saveFileToDownloads(body, filename, "application/pdf")
                    _uiState.update { it.copy(isExporting = false, exportSuccessMessage = "Đã lưu file: $filename") }
                } else {
                    _uiState.update { it.copy(isExporting = false, exportErrorMessage = "Chức năng xuất PDF chưa được hỗ trợ (Lỗi: ${response.code()})") }
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Export PDF failed (API likely missing)", e)
                _uiState.update { it.copy(isExporting = false, exportErrorMessage = "Chức năng xuất PDF chưa được hỗ trợ.") }
            }
        }
    }

    private fun saveFileToDownloads(body: ResponseBody, filename: String, mimeType: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri).use { outputStream ->
                body.byteStream().use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
        }
    }

    fun clearExportMessages() {
        _uiState.update { it.copy(exportErrorMessage = null, exportSuccessMessage = null) }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun TransactionDto.toTxUi(categoryMap: Map<String, CategoryDto>): TxUi {
    val localDateTime = try {
        LocalDateTime.parse(this.createdDate, apiDateFormatter)
    } catch (e: Exception) {
        LocalDateTime.now()
    }
    val isIncome = this.type.equals("Income", ignoreCase = true)
    val category = categoryMap[this.categoryId]
    val title = category?.name ?: (if (isIncome) "Thu nhập" else "Chi tiêu")
    val noteText = this.note?.takeIf { it.isNotBlank() && it != "string" } ?: "Không có ghi chú"
    val timeString = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    val subtitle = "$noteText • $timeString"
    return TxUi(
        id = this.transactionId,
        title = title,
        category = subtitle,
        emoji = category?.icon ?: "💰",
        dateTime = localDateTime,
        amount = this.amount,
        type = if (isIncome) TxType.INCOME else TxType.EXPENSE
    )
}