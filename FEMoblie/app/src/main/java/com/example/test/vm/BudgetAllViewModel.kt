package com.example.test.vm

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.LocalCategoryDataSource
import com.example.test.ui.api.AuthStore
import com.example.test.ui.api.BudgetApi
import com.example.test.ui.models.BudgetDto
import com.example.test.ui.util.LightBudgetPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.round

private const val TAG = "BudgetAllVM"

data class BudgetRowUi(
    val id: String,
    val icon: String,
    val title: String,
    val amountLabel: String,
    val progress: Float,
    val color: Color
)

data class BudgetAllUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<BudgetRowUi> = emptyList()
)

@HiltViewModel
class BudgetAllViewModel @Inject constructor(
    private val budgetApi: BudgetApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetAllUiState())
    val uiState: StateFlow<BudgetAllUiState> = _uiState

    private val allPaletteColors = listOf(
        "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9",
        "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9",
        "#DCEDC8", "#F0F4C3", "#FFF9C4", "#FFECB3", "#FFE0B2",
        "#FFCCBC", "#D7CCC8", "#CFD8DC", "#E6EE9C", "#DCE775"
    )

    fun load(userIdParam: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val uid = AuthStore.userId?.takeIf { it.isNotBlank() } ?: userIdParam.orEmpty()
                if (uid.isBlank()) {
                    _uiState.value = BudgetAllUiState(isLoading = false, error = "Thiếu userId")
                    return@launch
                }

                val res = budgetApi.getAllBudgets(uid)
                if (!res.isSuccessful) {
                    _uiState.value = BudgetAllUiState(
                        isLoading = false,
                        error = "HTTP ${res.code()} ${res.message()}"
                    )
                    return@launch
                }

                val list = res.body().orEmpty()

                val usedColorsHex = mutableSetOf<String>()

                val uiList = list.map { dto ->
                    var finalColorHex: String? = null

                    if (!dto.colorHex.isNullOrBlank()) {
                        finalColorHex = dto.colorHex
                    }

                    if (finalColorHex == null) {
                        finalColorHex = LightBudgetPalette.pickHex(dto.budgetId, dto.categoryId)
                    }

                    // Ưu tiên 3: Xử lý trùng lặp
                    if (usedColorsHex.contains(finalColorHex)) {
                        // Tìm màu đầu tiên trong palette mà CHƯA có trong 'usedColorsHex'
                        val availableColor = allPaletteColors.find { it !in usedColorsHex }

                        if (availableColor != null) {
                            finalColorHex = availableColor
                        }
                    }

                    usedColorsHex.add(finalColorHex!!)

                    mapToUi(dto, finalColorHex)
                }

                _uiState.value = BudgetAllUiState(isLoading = false, items = uiList)

            } catch (e: Exception) {
                Log.e(TAG, "load error", e)
                _uiState.value = BudgetAllUiState(isLoading = false, error = e.message)
            }
        }
    }

    private fun mapToUi(b: BudgetDto, assignedColorHex: String): BudgetRowUi {
        val cat = LocalCategoryDataSource.find(b.categoryId)
        val title = cat?.name ?: "Danh mục ${b.categoryId.take(6)}"
        val icon = cat?.icon ?: "💰"

        val color = assignedColorHex.toColor()

        val totalVnd = b.initialAmount.toMoney()
        val usedVnd = b.currentAmount.toMoney()
        val ratio = if (totalVnd > 0.0) (usedVnd / totalVnd).toFloat() else 0f

        val usedM = usedVnd / 1_000_000.0
        val totalM = totalVnd / 1_000_000.0
        val label = "${formatM(usedM)} / ${formatM(totalM)}"

        return BudgetRowUi(
            id = b.budgetId,
            icon = icon,
            title = title,
            amountLabel = label,
            progress = ratio.coerceIn(0f, 1f),
            color = color
        )
    }

    private fun String?.toMoney(): Double {
        if (this.isNullOrBlank()) return 0.0
        var s = this.trim()
        val lower = s.lowercase(Locale.US)
        val isMillionUnit = lower.contains("m") || lower.contains("triệu")
        s = s.replace("[₫đvnd\\s]".toRegex(RegexOption.IGNORE_CASE), "")
        s = s.replace("[^0-9,.]".toRegex(), "")
        s = normalizeDecimal(s)
        val value = s.toDoubleOrNull() ?: return 0.0
        return if (isMillionUnit) value * 1_000_000.0 else value
    }

    private fun normalizeDecimal(raw: String): String {
        val hasComma = raw.contains(',')
        val hasDot = raw.contains('.')
        if (hasComma && hasDot) {
            val lc = raw.lastIndexOf(',')
            val ld = raw.lastIndexOf('.')
            return if (lc > ld) raw.replace(".", "").replace(',', '.') else raw.replace(",", "")
        }
        if (hasComma) return if (raw.count { it == ',' } > 1) raw.replace(",", "") else raw.replace(',', '.')
        if (hasDot) return if (raw.count { it == '.' } > 1) raw.replace(".", "") else raw
        return raw
    }

    private fun formatM(v: Double): String {
        val r = round(v * 10.0) / 10.0
        return if (r % 1.0 == 0.0) "${r.toInt()}M" else String.format(Locale.US, "%.1fM", r)
    }

    private fun String.toColor(): Color {
        return try {
            Color(android.graphics.Color.parseColor(this))
        } catch (e: Exception) {
            Color(0xFF2196F3)
        }
    }

    private fun pickColor(i: Int): Color {
        val colors = listOf(
            Color(0xFFF44336), Color(0xFFFF9800),
            Color(0xFFFFEB3B), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFF9C27B0)
        )
        return colors[i % colors.size]
    }
}