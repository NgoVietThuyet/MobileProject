package com.example.test.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.LocalCategoryDataSource
import com.example.test.ui.api.Api
import com.example.test.ui.api.AuthStore
import com.example.test.ui.util.LightBudgetPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeBudgetUiState(
    val isLoading: Boolean = false,
    val items: List<HomeBudgetItem> = emptyList(),
    val error: String? = null
)

data class HomeBudgetItem(
    val budgetId: String,
    val categoryId: String,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val initialAmount: String,
    val currentAmount: String
)

@HiltViewModel
class HomeBudgetViewModel @Inject constructor() : ViewModel() {
    private val budgetApi = Api.budgetService

    private val _ui = MutableStateFlow(HomeBudgetUiState(isLoading = true))
    val ui: StateFlow<HomeBudgetUiState> = _ui

    fun loadBudgets() {
        viewModelScope.launch {
            try {
                _ui.value = HomeBudgetUiState(isLoading = true)

                val userId = AuthStore.userId ?: run {
                    _ui.value = HomeBudgetUiState(isLoading = false, items = emptyList())
                    return@launch
                }

                val res = budgetApi.getAllBudgets(userId)
                if (!res.isSuccessful) {
                    _ui.value = HomeBudgetUiState(isLoading = false, error = "HTTP ${res.code()}")
                    return@launch
                }

                val mapped = res.body().orEmpty()
                    .sortedByDescending { it.startDate ?: "" }
                    .take(4)
                    .map { b ->
                        val cat = LocalCategoryDataSource.find(b.categoryId)
                        HomeBudgetItem(
                            budgetId = b.budgetId,
                            categoryId = b.categoryId,
                            categoryName = cat?.name,
                            categoryIcon = cat?.icon,
                            categoryColor = LightBudgetPalette.pickHex(
                                budgetId = b.budgetId,
                                categoryId = b.categoryId
                            ),
                            initialAmount = b.initialAmount ?: "0",
                            currentAmount = b.currentAmount ?: "0"
                        )
                    }

                _ui.value = HomeBudgetUiState(items = mapped, isLoading = false)
            } catch (e: Exception) {
                _ui.value = HomeBudgetUiState(isLoading = false, error = e.message)
            }
        }
    }
}
