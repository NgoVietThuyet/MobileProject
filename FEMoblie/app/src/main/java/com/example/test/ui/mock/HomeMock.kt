package com.example.test.ui.mock

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
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
    val isPositive: Boolean
)

/* ====== AUTH MOCK ====== */
data class UserMock(
    val id: String,
    val name: String,
    val email: String
)

sealed interface AuthResult {
    data class Success(val user: UserMock) : AuthResult
    data class Error(val message: String) : AuthResult
}

object MockData {
    // ===== Header =====
    const val greetingName = "Tuất"
    const val balance = "47.350.000 ₫"

    // ===== Monthly summary =====
    const val monthlyIncome = "+22M"
    const val monthlyExpense = "-5.6M"
    const val monthlySaving = "+16.4M"

    // ===== Budgets=====
    val budgetCategories = mutableStateListOf(
        BudgetCategoryMock("🍜", "Ăn uống",  "2.1M / 3.0M", 0.70f, Color(0xffFF7B54)),
        BudgetCategoryMock("🚌", "Di chuyển","0.9M / 1.5M", 0.60f, Color(0xff4DD0E1)),
        BudgetCategoryMock("🎮", "Giải trí",  "0.6M / 1.2M", 0.50f, Color(0xff7E57C2)),
        BudgetCategoryMock("🧺", "Mua sắm",  "1.1M / 1.5M", 0.73f, Color(0xff66BB6A))
    )

    private val rawTotalsVnd = mutableMapOf<Int, Long>()

    // ===== transactions =====
    val recentTransactions = listOf(
        TransactionMock("🍜", "Bún bò Huế",   "Ăn uống • 11:45", "-65,000₫", isPositive = false),
        TransactionMock("🧾", "Hoàn tiền MOMO","Ví điện tử • 10:10", "+30,000₫", isPositive = true),
        TransactionMock("🚌", "Bus tuyến 08",  "Di chuyển • 08:05", "-7,000₫", isPositive = false),
        TransactionMock("👟", "Giày sneaker",  "Mua sắm • Hôm qua", "-1,250,000₫", isPositive = false),
        TransactionMock("💼", "Thưởng Q3",     "Thu nhập • Tuần trước", "+5,000,000₫", isPositive = true)
    )

    const val unreadChats = 3

    /* ===================== */
    /* ===== AUTH MOCK ===== */
    /* ===================== */

    // Chỉ cho phép đúng cặp này
    private const val ALLOWED_EMAIL = "user@example.com"
    private const val ALLOWED_PASSWORD = "123456"

    // User giả lập trả về khi đăng nhập đúng
    private val allowedUser = UserMock(
        id = "u_001",
        name = greetingName,
        email = ALLOWED_EMAIL
    )

    // check format email đơn giản (tránh phụ thuộc android.util.Patterns)
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

    /** Gọi hàm này trong onLogin để xác thực mock */
    fun loginMock(email: String, password: String): AuthResult {
        val e = email.trim()
        val p = password

        if (e.isEmpty() || p.isEmpty()) {
            return AuthResult.Error("Vui lòng nhập email và mật khẩu")
        }
        if (!looksLikeEmail(e)) {
            return AuthResult.Error("Email không hợp lệ")
        }
        return if (e.equals(ALLOWED_EMAIL, ignoreCase = true) && p == ALLOWED_PASSWORD) {
            AuthResult.Success(allowedUser)
        } else {
            AuthResult.Error("Email hoặc mật khẩu không đúng")
        }
    }

    /* ======================== */
    /* ===== Budget utils ===== */
    /* ======================== */

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
            formatM(usedM) // total = 0 → chỉ hiển thị used
        }
        budgetCategories[index] = item.copy(amount = newAmount, progress = newProgress)
        rawTotalsVnd[index] = (safeTotal * 1_000_000).roundToLong()
    }

    fun deleteBudget(index: Int) {
        if (index !in budgetCategories.indices) return
        budgetCategories.removeAt(index)

        // Re-index map
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

    /*  Nếu cần cập nhật theo VND, bật lại block này
    fun updateBudgetTotalVnd(index: Int, totalVnd: Long) {
        val safeVnd = if (totalVnd >= 0) totalVnd else 0L
        rawTotalsVnd[index] = safeVnd

        val item = budgetCategories.getOrNull(index) ?: return
        val usedM = parseFirstM(item.amount)
        val totalM = safeVnd / 1_000_000.0
        val safeTotalM = if (totalM > 0.0) totalM else 0.0
        val newProgress = if (safeTotalM > 0.0) (usedM / safeTotalM).toFloat().coerceIn(0f, 1f) else 0f
        val newAmount = if (safeTotalM > 0.0) {
            "${formatM(usedM)} / ${formatM(safeTotalM)}"
        } else {
            formatM(usedM)
        }
        budgetCategories[index] = item.copy(amount = newAmount, progress = newProgress)
    }
    */

    fun addBudgetVnd(name: String, totalVnd: Long, icon: String, color: Color) {
        val m = (totalVnd / 1_000_000.0)
        addBudgetTotalM(name = name, totalM = m, icon = icon, color = color)
        rawTotalsVnd[budgetCategories.lastIndex] = if (totalVnd >= 0) totalVnd else 0L
    }
}
