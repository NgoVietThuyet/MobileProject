package com.example.test.ui.models

import com.google.gson.annotations.SerializedName


data class BudgetDto(
    @SerializedName("budgetId") val budgetId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("initial_Amount") val initialAmount: String?,
    @SerializedName("current_Amount") val currentAmount: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("createdDate") val createdDate: String?,
    @SerializedName("updatedDate") val updatedDate: String?,

    val colorHex: String? = null
)


data class UpdateBudgetAmountReq(
    @SerializedName("userId") val userId: String?,
    @SerializedName("budgetId") val budgetId: String,
    @SerializedName("updateAmount") val updateAmount: String,
    @SerializedName("isAddAmount") val isAddAmount: Boolean
)

data class CreateBudgetReq(
    @SerializedName("userId") val userId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("initial_Amount") val initialAmount: String,
    @SerializedName("current_Amount") val currentAmount: String,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,

    val colorHex: String? = null
)


data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("budget") val budget: T? = null
)

data class BudgetCreateResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("budget") val budget: BudgetDto? = null
)

data class BudgetUpdateResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("budget") val budget: BudgetDto? = null
)

data class BudgetDeleteResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class CategoryCreateResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("category") val category: CategoryCreateDto? = null
)
