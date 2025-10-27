package com.example.test.ui.models

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
)

data class CategoryCreateDto(
    @SerializedName("id")   val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("createdDate") val createdDate: String? = null,
    @SerializedName("updatedDate") val updatedDate: String? = null
)

data class CreateCategoryReq(
    @SerializedName("categoryDto") val category: CreateCategoryInner
)

data class CreateCategoryInner(
    @SerializedName("id")   val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("createdDate") val createdDate: String? = null,
    @SerializedName("updatedDate") val updatedDate: String? = null
)