package com.example.test.ui.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong


enum class AmountStyle { VND_PLAIN, VND_PADDED, MILLION_1DP, MILLION_0DP }

object MoneyUiConfig {
    var DEFAULT_STYLE: AmountStyle = AmountStyle.VND_PLAIN
    var ROUND_DECIMALS_FOR_M: Int = 1
}

object NumberFmt {
    private val vi = Locale("vi","VN")
    private val sym = DecimalFormatSymbols(vi).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    fun round(value: Double, decimals: Int): Double =
        BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP).toDouble()

    fun vnd(amount: Long): String = NumberFormat.getCurrencyInstance(vi).format(amount) // "1.234.567 ₫"
    fun vndPlain(amount: Long): String = DecimalFormat("#,##0", sym).format(amount)     // "1.234.567"
    fun vndPadded(amount: Long): String = DecimalFormat("000,000,000", sym).format(amount) // "000.000.123"

    fun toM(amountVnd: Long): Double = amountVnd / 1_000_000.0

    fun millionLabel(valueM: Double, decimals: Int): String {
        val d = round(valueM, decimals)
        val pattern = if (decimals == 0) "0" else "0.${"0".repeat(decimals)}"
        val s = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(d)
        return "${s}M"
    }

    fun formatUsedTotal(usedM: Double, totalM: Double?, style: AmountStyle): String = when (style) {
        AmountStyle.VND_PLAIN -> {
            val u = (usedM * 1_000_000).roundToLong()
            val t = totalM?.let { (it * 1_000_000).roundToLong() }
            buildString {
                append(vndPlain(u)); t?.takeIf { it > 0 }?.let { append(" / ${vndPlain(it)}") }
            }
        }
        AmountStyle.VND_PADDED -> {
            val u = (usedM * 1_000_000).roundToLong()
            val t = totalM?.let { (it * 1_000_000).roundToLong() }
            buildString {
                append(vndPadded(u)); t?.takeIf { it > 0 }?.let { append(" / ${vndPadded(it)}") }
            }
        }
        AmountStyle.MILLION_1DP ->
            buildString {
                append(millionLabel(usedM, 1)); totalM?.takeIf { it > 0 }?.let { append(" / ${millionLabel(it, 1)}") }
            }
        AmountStyle.MILLION_0DP ->
            buildString {
                append(millionLabel(usedM, 0)); totalM?.takeIf { it > 0 }?.let { append(" / ${millionLabel(it, 0)}") }
            }
    }
}

fun parseUsedMFromMock(amount: String): Double =
    amount.split('/').firstOrNull().orEmpty().lowercase()
        .replace(",", ".").replace("[^0-9\\.]".toRegex(), "").toDoubleOrNull() ?: 0.0

fun parseTotalMFromMock(amount: String): Double? =
    amount.split('/').getOrNull(1)?.trim().orEmpty().lowercase()
        .replace(",", ".").replace("[^0-9\\.]".toRegex(), "").toDoubleOrNull()
