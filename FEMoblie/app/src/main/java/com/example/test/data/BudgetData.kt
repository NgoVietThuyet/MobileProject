package com.example.test.data

import com.example.test.ui.api.BudgetApi
import com.example.test.ui.models.CreateBudgetReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val budgetApi: BudgetApi
) {
    suspend fun createBudgetWithExistingCategory(
        userId: String,
        categoryId: String,
        initialAmountVnd: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val today = LocalDate.now()
            val start = startDate ?: today.withDayOfMonth(1).format(fmt)
            val end = endDate ?: today.withDayOfMonth(today.lengthOfMonth()).format(fmt)

            val req = CreateBudgetReq(
                userId = userId,
                categoryId = categoryId,
                initialAmount = initialAmountVnd,
                currentAmount = "0",
                startDate = start,
                endDate = end
            )
            val res = budgetApi.createBudget(req)
            if (!res.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException("Tạo Budget thất bại (HTTP ${res.code()} ${res.message()})")
                )
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
