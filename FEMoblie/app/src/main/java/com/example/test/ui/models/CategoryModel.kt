package com.example.test.ui.models

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String
)
