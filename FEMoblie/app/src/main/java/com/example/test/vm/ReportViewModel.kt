package com.example.test.vm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Import thêm
import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.BudgetApi
import com.example.test.ui.models.BudgetDto
// ----
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
// Import thêm
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import kotlin.math.round
// ----

/**
 * Data class cho dữ liệu biểu đồ tròn.
 * ViewModel chỉ cung cấp dữ liệu thô (label, value).
 * Composable sẽ chịu trách nhiệm gán màu sắc.
 */
data class CategoryPieData(
    val label: String,
    val value: Float // Giá trị (đã chia cho 1 triệu)
)

/**
 * Trạng thái (State) cho ReportScreen
 */
data class ReportUiState(
    val isLoading: Boolean = true,
    val currentPeriod: Int = 1, // 0=Tuần, 1=Tháng (mặc định), 2=Năm

    // Dữ liệu cho Thẻ KPI
    val kpiIncome: Long = 0L,
    val kpiExpense: Long = 0L,

    // Dữ liệu Biểu đồ Tổng quan (Tab 0)
    val overviewLabels: List<String> = emptyList(),
    val overviewIncome: List<Float> = emptyList(),
    val overviewExpense: List<Float> = emptyList(),

    // Dữ liệu Biểu đồ Danh mục (Tab 1)
    val expensePieData: List<CategoryPieData> = emptyList(), // Pie chart chi tiêu (giữ lại)
    val budgetPieData: List<CategoryPieData> = emptyList(), // **Pie chart ngân sách (mới)**

    // Dữ liệu Biểu đồ Xu hướng (Tab 2)
    val trendLabels: List<String> = emptyList(),
    val trendExpense: List<Float> = emptyList(),

    val error: String? = null
)

// Định dạng ngày tháng từ API (giống với các ViewModel khác)
private val apiDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
// **SỬA LỖI 1: Sửa định dạng cho đúng với API log**
private val budgetDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val budgetApi: BudgetApi // 1. Inject BudgetApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    // Lưu trữ toàn bộ giao dịch, categories VÀ budgets
    private var allTransactions: List<TxUi> = emptyList()
    private var allBudgets: List<BudgetDto> = emptyList() // 2. Thêm list budgets
    private var categoryMap: Map<String, CategoryDto> = emptyMap()
    private val zone = ZoneId.systemDefault()

    init {
        // Tải dữ liệu ngay khi ViewModel được tạo
        fetchAllData()
    }

    /**
     * Thay đổi khoảng thời gian (Tuần/Tháng/Năm) và tính toán lại biểu đồ.
     */
    fun setPeriod(newPeriod: Int) {
        if (newPeriod == _uiState.value.currentPeriod) return // Không thay đổi

        _uiState.update { it.copy(currentPeriod = newPeriod) }
        recalculateCharts()
    }

    /**
     * Tải tất cả giao dịch, categories và budgets.
     */
    private fun fetchAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Tải transactions và categories
                val transactionResponse = transactionRepo.getAllFromAuth()
                val categories = transactionRepo.getLocalCategories()
                categoryMap = categories.associateBy { it.categoryId }

                // 3. Tải budgets
                val uid = AuthStore.userId.orEmpty()
                val budgetResponse = budgetApi.getAllBudgets(uid)

                // 4. Kiểm tra cả hai response
                if (transactionResponse.isSuccessful && budgetResponse.isSuccessful) {
                    val dtos = transactionResponse.body()?.transactions ?: emptyList()
                    allTransactions = dtos.map { it.toTxUi(categoryMap) }

                    allBudgets = budgetResponse.body().orEmpty() // 5. Lưu budgets

                    recalculateCharts() // Tính toán cho period mặc định
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

    /**
     * Tính toán lại toàn bộ dữ liệu cho các biểu đồ dựa trên `currentPeriod`.
     */
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

        // 6. Cập nhật logic trong cả 3 case
        when (period) {
            // --- LOGIC CHO NĂM (Period 2) ---
            2 -> {
                val year = today.year
                val months = (1..12).map { Month.of(it) }
                val labels = months.map { "T${it.value}" }

                val groupedByMonth = allTransactions
                    .filter { it.dateTime.year == year }
                    .groupBy { it.dateTime.month }
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

                // Tính pie chart chi tiêu (như cũ)
                val pieTxs = allTransactions.filter { it.dateTime.year == year }
                val expensePie = calculateExpensePieData(pieTxs)

                // 7. Tính pie chart ngân sách
                val periodBudgets = allBudgets.filter {
                    try {
                        // **SỬA LỖI 2: Dùng LocalDateTime và .toLocalDate()**
                        LocalDateTime.parse(it.startDate, budgetDateFormatter).toLocalDate().year == year
                    }
                    catch (e: DateTimeParseException) { false }
                }
                val budgetPie = calculateBudgetPieData(periodBudgets)

                _uiState.update { it.copy(
                    overviewLabels = labels,
                    overviewIncome = incomes,
                    overviewExpense = expenses,
                    trendLabels = labels,
                    trendExpense = expenses,
                    expensePieData = expensePie, // Cập nhật
                    budgetPieData = budgetPie,   // Cập nhật
                    kpiIncome = totalIncome,
                    kpiExpense = totalExpense
                )}
            }

            // --- LOGIC CHO THÁNG (Period 1) ---
            1 -> {
                val currentMonth = today.month
                val currentYear = today.year
                val labels = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4")

                val firstDay = today.withDayOfMonth(1)
                val dayRanges = listOf(
                    firstDay..firstDay.plusDays(6),
                    firstDay.plusDays(7)..firstDay.plusDays(13),
                    firstDay.plusDays(14)..firstDay.plusDays(20),
                    firstDay.plusDays(21)..today.withDayOfMonth(today.lengthOfMonth())
                )
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

                // Tính pie chart chi tiêu
                val pieTxs = allTransactions.filter { it.dateTime.month == currentMonth && it.dateTime.year == currentYear }
                val expensePie = calculateExpensePieData(pieTxs)

                // 7. Tính pie chart ngân sách
                val periodBudgets = allBudgets.filter {
                    try {
                        // **SỬA LỖI 3: Dùng LocalDateTime và .toLocalDate()**
                        val d = LocalDateTime.parse(it.startDate, budgetDateFormatter).toLocalDate()
                        d.month == currentMonth && d.year == currentYear
                    } catch (e: DateTimeParseException) { false }
                }
                val budgetPie = calculateBudgetPieData(periodBudgets)

                _uiState.update { it.copy(
                    overviewLabels = labels,
                    overviewIncome = incomes,
                    overviewExpense = expenses,
                    trendLabels = labels,
                    trendExpense = expenses,
                    expensePieData = expensePie, // Cập nhật
                    budgetPieData = budgetPie,   // Cập nhật
                    kpiIncome = totalIncome,
                    kpiExpense = totalExpense
                )}
            }

            // --- LOGIC CHO TUẦN (Period 0) ---
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

                // Tính pie chart chi tiêu
                val pieTxs = allTransactions.filter { it.dateTime.toLocalDate() in weekDays }
                val expensePie = calculateExpensePieData(pieTxs)

                // 7. Tính pie chart ngân sách
                val periodBudgets = allBudgets.filter {
                    try {
                        // **SỬA LỖI 4: Dùng LocalDateTime và .toLocalDate()**
                        LocalDateTime.parse(it.startDate, budgetDateFormatter).toLocalDate() in weekDays
                    } catch (e: DateTimeParseException) { false }
                }
                val budgetPie = calculateBudgetPieData(periodBudgets)

                _uiState.update { it.copy(
                    overviewLabels = overviewLbls,
                    overviewIncome = incomes,
                    overviewExpense = expenses,
                    trendLabels = trendLbls,
                    trendExpense = trendData,
                    expensePieData = expensePie, // Cập nhật
                    budgetPieData = budgetPie,   // Cập nhật
                    kpiIncome = totalIncome,
                    kpiExpense = totalExpense
                )}
            }
        }
    }

    /**
     * Đổi tên: Tính toán dữ liệu cho biểu đồ tròn (chi tiêu theo danh mục).
     */
    private fun calculateExpensePieData(transactions: List<TxUi>): List<CategoryPieData> {
        val byCat = transactions
            .filter { it.type == TxType.EXPENSE }
            .groupBy { it.title } // TxUi.title chứa tên Category
            .mapValues { entry -> entry.value.sumOf { it.amount.toLongOrNull() ?: 0L } }

        return byCat.entries.sortedByDescending { it.value }
            .map { (label, sum) ->
                CategoryPieData(label, sum / 1_000_000f)
            }
    }

    /**
     * 8. Hàm mới: Tính toán dữ liệu pie chart từ Ngân sách.
     */
    private fun calculateBudgetPieData(budgets: List<BudgetDto>): List<CategoryPieData> {
        val byCat = budgets
            .groupBy { it.categoryId }
            .mapValues { entry ->
                // **THAY ĐỔI CHÍNH: Dùng currentAmount**
                entry.value.sumOf { toMoney(it.currentAmount) }
            }

        return byCat.entries.sortedByDescending { it.value }
            .map { (catId, sum) ->
                val label = categoryMap[catId]?.name ?: "Ngân sách khác"
                // Chuyển Double (từ toMoney) sang Float
                CategoryPieData(label, (sum / 1_000_000.0).toFloat())
            }
    }

    // 9. Thêm các hàm helper từ BudgetAllViewModel
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
}


/**
 * Hàm mở rộng để chuyển đổi TransactionDto sang TxUi.
 * (Giữ nguyên)
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun TransactionDto.toTxUi(categoryMap: Map<String, CategoryDto>): TxUi {
    val localDateTime = try {
        LocalDateTime.parse(this.createdDate, apiDateFormatter)
    } catch (e: Exception) {
        LocalDateTime.now()
    }

    val isIncome = this.type.equals("Income", ignoreCase = true)
    val category = categoryMap[this.categoryId]

    // Tên danh mục (vd: "Ăn uống")
    val title = category?.name ?: (if (isIncome) "Thu nhập" else "Chi tiêu")

    // Phụ đề (vd: "Ghi chú • 14:30")
    val noteText = this.note?.takeIf { it.isNotBlank() && it != "string" } ?: "Không có ghi chú"
    val timeString = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    val subtitle = "$noteText • $timeString"

    return TxUi(
        id = this.transactionId,
        title = title, // Tên danh mục
        category = subtitle, // Ghi chú + Thời gian
        emoji = category?.icon ?: "💰",
        dateTime = localDateTime,
        amount = this.amount,
        type = if (isIncome) TxType.INCOME else TxType.EXPENSE
    )
}