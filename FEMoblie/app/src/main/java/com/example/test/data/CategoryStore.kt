package com.example.test.data

import com.example.test.ui.models.CategoryDto
import java.util.Locale

object LocalCategoryDataSource {
    val allCategories = listOf(
        CategoryDto("1", "Khác", "📦"),
        CategoryDto("2", "Việc tự do", "💻"),
        CategoryDto("3", "Điện nước", "💧"),
        CategoryDto("4", "Đầu tư", "📈"),
        CategoryDto("5", "Giáo dục", "📚"),
        CategoryDto("6", "Y tế", "🩺"),
        CategoryDto("7", "Lương", "💼"),
        CategoryDto("8", "Ăn uống", "🍜"),
        CategoryDto("9", "Mua sắm", "🛍️"),
        CategoryDto("10", "Thưởng", "🎁"),
        CategoryDto("11", "Đi lại", "🚗"),
        CategoryDto("12", "Giải trí", "🎬"),
        CategoryDto("13", "Bán hàng", "🛒"),
        CategoryDto("14", "Thu nhập khác", "💰"),
        CategoryDto("15", "Nhà ở", "🏠")
    )

    private val incomeIds = setOf(
        "2",
        "4",
        "7",
        "10",
        "13",
        "14"
    ).map { it.uppercase(Locale.US) }.toSet()

    fun expenseOnly(): List<CategoryDto> =
        allCategories.filter { it.categoryId.uppercase(Locale.US) !in incomeIds }

    fun find(id: String): CategoryDto? =
        allCategories.firstOrNull { it.categoryId.equals(id, ignoreCase = true) }
}

object SavingGoalCategories {
    val categories = listOf(
        CategoryDto("16", "saving_goal_category", "🏠"),
        CategoryDto("17", "saving_goal_category", "🚗"),
        CategoryDto("18", "saving_goal_category", "✈️"),
        CategoryDto("19", "saving_goal_category", "🍜"),
        CategoryDto("20", "saving_goal_category", "📅"),
        CategoryDto("21", "saving_goal_category", "💻"),
        CategoryDto("22", "saving_goal_category", "💍"),
        CategoryDto("23", "saving_goal_category", "🎓"),
        CategoryDto("24", "saving_goal_category", "🆘"),
        CategoryDto("25", "saving_goal_category", "🌮"),
        CategoryDto("26", "saving_goal_category", "☕"),
        CategoryDto("27", "saving_goal_category", "💰"),
        CategoryDto("28", "saving_goal_category", "🎮"),
        CategoryDto("29", "saving_goal_category", "🐶"),
        CategoryDto("30", "saving_goal_category", "🍕"),
        CategoryDto("31", "saving_goal_category", "📷"),
        CategoryDto("32", "saving_goal_category", "🎵"),
        CategoryDto("33", "saving_goal_category", "🎯")
    )

    // Optional helper function to find icon by ID
    fun findIconById(id: String?): String {
        return categories.firstOrNull { it.categoryId.equals(id, ignoreCase = true) }?.icon ?: "💰" // Fallback icon
    }
}