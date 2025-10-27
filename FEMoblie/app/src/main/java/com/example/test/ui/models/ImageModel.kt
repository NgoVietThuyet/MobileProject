package com.example.test.ui.models
data class ImageUploadResp(
    val success: Boolean,
    val message: String?,
    val transactions: List<ImageUploadTx>?
)

data class ImageUploadTx(
    val transactionId: String?,
    val userId: String?,
    val categoryId: String?,
    val type: String?,
    val amount: String?,
    val note: String?,
    val createdDate: String?,
    val updatedDate: String?
)