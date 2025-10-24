package com.example.test.ui.models

import com.google.gson.annotations.SerializedName

data class SavingGoalDto(
    @SerializedName("goalId") val goalId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("title") val title: String,
    @SerializedName("targetAmount") val targetAmount: String,
    @SerializedName("currentAmount") val currentAmount: String,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("createdDate") val createdDate: String,
    @SerializedName("updatedDate") val updatedDate: String?
)

data class CreateSavingGoalRequest(
    @SerializedName("savingGoalDto") val savingGoalDto: SavingGoalDto
)

data class UpdateSavingGoalAmountRequest(
    @SerializedName("goalId") val goalId: String,
    @SerializedName("updateAmount") val updateAmount: String,
    @SerializedName("isAddAmount") val isAddAmount: Boolean
)
