package com.example.test.ui.mock

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import kotlin.math.roundToLong

data class BudgetCategoryMock(
    val icon: String,
    val title: String,
    val amount: String,
    val progress: Float,
    val color: Color
)

data class TransactionMock(
    val icon: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val isPositive: Boolean,
    val createdAt: Long
)

/* ===== Savings ===== */
data class SavingGoalMock(
    val emoji: String,
    val title: String,
    val savedM: Float,
    val totalM: Float,
    val color: Color,
    val daysRemain: Int
)

/* ===== Notifications ===== */
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val iconEmoji: String,
    val iconBg: Color,
    val unread: Boolean
)

/* ====== AUTH MOCK ====== */
data class UserMock(
    val id: String,
    val name: String,
    val email: String
)

enum class TxType { INCOME, EXPENSE }
data class TxUi(
    val id: String,
    val title: String,
    val category: String,
    val dateTime: LocalDateTime,
    val amount: Long,
    val type: TxType,
    val iconRes: Int? = null,
    val emoji: String? = null
)

sealed interface AuthResult {
    data class Success(val user: UserMock) : AuthResult
    data class Error(val message: String) : AuthResult
}

object MockData {
    const val greetingName = "Tuấn"
    const val balance = "47.350.000 ₫"

    const val monthlyIncome = "+22M"
    const val monthlyExpense = "-5.6M"
    const val monthlySaving = "+16.4M"

    // ===== Budgets =====
    val budgetCategories = mutableStateListOf(
        BudgetCategoryMock("🍜", "Ăn uống",  "2.1M / 3.0M", 0.70f, Color(0xffFF7B54)),
        BudgetCategoryMock("🚌", "Di chuyển","0.9M / 1.5M", 0.60f, Color(0xff4DD0E1)),
        BudgetCategoryMock("🎮", "Giải trí",  "0.6M / 1.2M", 0.50f, Color(0xff7E57C2)),
        BudgetCategoryMock("🧺", "Mua sắm",  "1.1M / 1.5M", 0.73f, Color(0xff66BB6A))
    )

    // ===== Savings goals =====
    val savingGoals: List<SavingGoalMock> = listOf(
        SavingGoalMock("🛵", "Mua xe máy", 30f, 50f, Color(0xFF4F69FF), -107),
        SavingGoalMock("🗾", "Du lịch Nhật Bản", 12f, 25f, Color(0xFF8B5CF6), 36),
        SavingGoalMock("🆘", "Quỹ khẩn cấp", 60f, 100f, Color(0xFFFF4D4F), 107)
    )

    private val rawTotalsVnd = mutableMapOf<Int, Long>()

    private const val MIN: Long = 60_000L
    private const val HOUR: Long = 60 * MIN
    private const val DAY: Long = 24 * HOUR
    private val now: Long = System.currentTimeMillis()

    // ===== Transactions =====
    val recentTransactions = listOf(
        TransactionMock("☕", "Cà phê sáng",     "Ăn uống • 08:10", "-35,000₫",   false, now - 8 * HOUR + 10 * MIN),
        TransactionMock("🍜", "Bún bò Huế",      "Ăn uống • 11:45", "-65,000₫",   false, now - 50 * MIN),
        TransactionMock("🧾", "Hoàn tiền MOMO",  "Ví điện tử • 10:10", "+30,000₫", true,  now - 2 * HOUR),
        TransactionMock("🏦", "Gửi tiết kiệm",   "Tiết kiệm • 10:05", "-2,000,000₫", false, now - 2 * HOUR - 5 * MIN),
        TransactionMock("🚌", "Bus tuyến 08",    "Di chuyển • 08:05", "-7,000₫",   false, now - 4 * HOUR),
        TransactionMock("🥗", "Cơm trưa văn phòng","Ăn uống • 12:35", "-62,000₫", false, now - 25 * MIN),
        TransactionMock("🛒", "Mua đồ tạp hoá",  "Mua sắm • 19:10", "-185,000₫",  false, now - 5 * MIN),
        TransactionMock("👟", "Giày sneaker",    "Mua sắm • Hôm qua", "-1,250,000₫", false, now - 1 * DAY - 2 * HOUR),
        TransactionMock("🚗", "Grab đi làm",     "Di chuyển • 08:20", "-45,000₫",   false, now - 1 * DAY - 16 * HOUR),
        TransactionMock("🍚", "Bữa trưa",        "Ăn uống • 12:15", "-55,000₫",    false, now - 1 * DAY - 12 * HOUR - 45 * MIN),
        TransactionMock("🧾", "Freelance",       "Thu nhập khác • 20:05", "+1,500,000₫", true, now - 1 * DAY - 3 * HOUR - 55 * MIN),
        TransactionMock("🏠", "Tiền nhà",        "Nhà ở • 09:00", "-4,000,000₫",   false, now - 2 * DAY - 15 * HOUR),
        TransactionMock("💡", "Điện tháng này",  "Điện nước • 10:30", "-420,000₫", false, now - 2 * DAY - 13 * HOUR - 30 * MIN),
        TransactionMock("🚿", "Nước sinh hoạt",  "Điện nước • 10:45", "-120,000₫", false, now - 2 * DAY - 13 * HOUR - 15 * MIN),
        TransactionMock("🌐", "Internet",        "Hạ tầng • 11:00", "-250,000₫",   false, now - 2 * DAY - 13 * HOUR),
        TransactionMock("📺", "Netflix",         "Giải trí • 21:10", "-180,000₫",  false, now - 2 * DAY - 3 * HOUR - 50 * MIN),
        TransactionMock("🎉", "Trúng thưởng nhỏ","Thu nhập khác • 18:40", "+50,000₫", true, now - 2 * DAY - 5 * HOUR - 20 * MIN),
        TransactionMock("📘", "Sách kỹ năng",    "Giáo dục • 19:20", "-150,000₫",  false, now - 3 * DAY - 4 * HOUR - 40 * MIN),
        TransactionMock("📱", "Nạp điện thoại",  "Liên lạc • 14:05", "-100,000₫",  false, now - 3 * DAY - 9 * HOUR - 55 * MIN),
        TransactionMock("🏆", "Thưởng quý",      "Lương • 16:30", "+5,000,000₫",   true,  now - 3 * DAY - 7 * HOUR - 30 * MIN),
        TransactionMock("🍣", "Ăn tối",          "Ăn uống • 20:00", "-95,000₫",    false, now - 3 * DAY - 3 * HOUR),
        TransactionMock("⛽", "Đổ xăng",         "Di chuyển • 09:50", "-120,000₫",  false, now - 3 * DAY - 14 * HOUR - 10 * MIN),
        TransactionMock("🎁", "Quà sinh nhật",   "Quà tặng • 17:25", "-300,000₫",  false, now - 3 * DAY - 6 * HOUR - 35 * MIN),
        TransactionMock("💼", "Thưởng Q3",       "Thu nhập • Tuần trước", "+5,000,000₫", true, now - 7 * DAY)
    )

    const val unreadChats = 3

    private const val ALLOWED_EMAIL = "user@example.com"
    private const val ALLOWED_PASSWORD = "123456"

    private val allowedUser = UserMock(
        id = "u_001",
        name = greetingName,
        email = ALLOWED_EMAIL
    )
    private fun looksLikeEmail(e: String): Boolean {
        val s = e.trim()
        if (s.isEmpty()) return false
        if (!s.contains('@')) return false
        val parts = s.split('@')
        if (parts.size != 2) return false
        if (parts[0].isEmpty() || parts[1].isEmpty()) return false
        if (!parts[1].contains('.')) return false
        return true
    }

    fun loginMock(email: String, password: String): AuthResult {
        val e = email.trim()
        val p = password
        if (e.isEmpty() || p.isEmpty()) return AuthResult.Error("Vui lòng nhập email và mật khẩu")
        if (!looksLikeEmail(e)) return AuthResult.Error("Email không hợp lệ")
        return if (e.equals(ALLOWED_EMAIL, true) && p == ALLOWED_PASSWORD) {
            AuthResult.Success(allowedUser)
        } else {
            AuthResult.Error("Email hoặc mật khẩu không đúng")
        }
    }

    private fun parseFirstM(text: String): Double {
        val first = text.split('/').firstOrNull().orEmpty()
        val cleaned = first.lowercase()
            .replace(",", ".")
            .replace("[^0-9\\.]".toRegex(), "")
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun formatM(v: Double): String {
        val r = kotlin.math.round(v * 10.0) / 10.0
        val s = if (r % 1.0 == 0.0) r.toInt().toString()
        else String.format(java.util.Locale.US, "%.1f", r)
        return "${s}M"
    }

    fun updateBudgetTotalM(index: Int, newTotalM: Double) {
        val item = budgetCategories.getOrNull(index) ?: return
        val usedM = parseFirstM(item.amount)
        val safeTotal = if (newTotalM > 0.0) newTotalM else 0.0
        val newProgress = if (safeTotal > 0.0) (usedM / safeTotal).toFloat().coerceIn(0f, 1f) else 0f
        val newAmount = if (safeTotal > 0.0) {
            "${formatM(usedM)} / ${formatM(safeTotal)}"
        } else {
            formatM(usedM)
        }
        budgetCategories[index] = item.copy(amount = newAmount, progress = newProgress)
        rawTotalsVnd[index] = (safeTotal * 1_000_000).roundToLong()
    }

    fun deleteBudget(index: Int) {
        if (index !in budgetCategories.indices) return
        budgetCategories.removeAt(index)

        val old = rawTotalsVnd.toMap()
        rawTotalsVnd.clear()
        for (i in budgetCategories.indices) {
            val oldKey = if (i >= index) i + 1 else i
            old[oldKey]?.let { rawTotalsVnd[i] = it }
        }
    }

    fun addBudgetTotalM(name: String, totalM: Double, icon: String, color: Color) {
        val safeTotal = if (totalM > 0.0) totalM else 0.0
        val amountStr = if (safeTotal > 0.0) "0M / ${formatM(safeTotal)}" else "0M"
        budgetCategories.add(
            BudgetCategoryMock(
                icon = icon,
                title = name,
                amount = amountStr,
                progress = 0f,
                color = color
            )
        )
        rawTotalsVnd[budgetCategories.lastIndex] = (safeTotal * 1_000_000).roundToLong()
    }

    fun getBudgetTotalVnd(index: Int): Long? = rawTotalsVnd[index]

    fun addBudgetVnd(name: String, totalVnd: Long, icon: String, color: Color) {
        val m = (totalVnd / 1_000_000.0)
        addBudgetTotalM(name = name, totalM = m, icon = icon, color = color)
        rawTotalsVnd[budgetCategories.lastIndex] = if (totalVnd >= 0) totalVnd else 0L
    }

    val items = listOf(
        NotificationItem(
            id = "1",
            title = "Ngân sách ăn uống sắp hết",
            message = "Bạn đã sử dụng 85% ngân sách ăn uống tháng này (1.7M/2M).",
            time = "2 giờ trước",
            iconEmoji = "🍽️",
            iconBg = Color(0xFFFFF3CD),
            unread = true
        ),
        NotificationItem(
            id = "2",
            title = "Báo cáo tháng đã sẵn sàng",
            message = "Báo cáo chi tiêu tháng 12 của bạn đã được tạo. Xem ngay để phân...",
            time = "1 ngày trước",
            iconEmoji = "📊",
            iconBg = Color(0xFFE8ECFF),
            unread = false
        )
    )
}
