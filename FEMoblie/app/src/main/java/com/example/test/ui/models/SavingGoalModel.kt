package com.example.test.ui.models

import com.google.gson.annotations.SerializedName
data class SavingGoalDto(
    @SerializedName("goalId")
    val savingGoalId: String?,
    val userId: String?,
    val categoryId: String?,
    val title: String?,
    val targetAmount: String?,
    val currentAmount: String?,
    val deadline: String?,
    val createdDate: String?,
    val updateDate: String?,
)

data class SavingGoalCreateReq(
    @SerializedName("goalId")
    val savingGoalId: String,
    val userId: String,
    val categoryId: String,
    val title: String,
    val targetAmount: String,
    val currentAmount: String,
    val deadline: String,
)

data class SavingGoalCreateResponse(
    val success: Boolean?,
    val message: String?,
    val data: SavingGoalDto?
)

data class SavingGoalUpdateAmountReq(
    @SerializedName("goalId")
    val goalId: String,
    val updateAmount: String,
    @SerializedName("isAddAmount")
    val isAddAmount: Boolean
)

data class SavingGoalDeleteReq(
    @SerializedName("id")
    val id: String
)

data class SimpleSuccessResponse(
    val success: Boolean?,
    val message: String?
)

data class SavingGoalCreatePayload(
    val savingGoalDto: SavingGoalCreateReq
)