package com.example.test.ui.util

import kotlin.math.abs

object LightBudgetPalette {
    private val COLORS = listOf(
        "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9",
        "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9",
        "#DCEDC8", "#F0F4C3", "#FFF9C4", "#FFECB3", "#FFE0B2",
        "#FFCCBC", "#D7CCC8", "#CFD8DC", "#E6EE9C", "#DCE775"
    )

    fun pickHex(budgetId: String?, categoryId: String?): String {
        val key = budgetId ?: categoryId ?: "fallback"
        val idx = abs(key.hashCode()) % COLORS.size
        return COLORS[idx]
    }
}
