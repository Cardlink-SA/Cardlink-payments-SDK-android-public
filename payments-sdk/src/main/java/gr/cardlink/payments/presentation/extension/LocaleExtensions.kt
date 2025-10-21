package gr.cardlink.payments.presentation.extension

import java.text.NumberFormat
import java.util.*

internal fun Double.toCurrencyFromNumericCode(currencyNumericCode: String?): String {
    val currency = Currency.getAvailableCurrencies().firstOrNull { it.numericCode == currencyNumericCode?.toIntOrNull() }
    return formatPrice(this, currency)
}

private fun formatPrice(amount: Double, currency: Currency?): String {
    val numberFormat = NumberFormat.getCurrencyInstance().apply {
        currency?.let {
            setCurrency(it)
        }
        maximumFractionDigits = 2
    }
    return numberFormat.format(amount / 100.0)
}

internal fun Double.toCurrencyFromCode(currencyCode: String?): String {
    val currency = Currency.getAvailableCurrencies().firstOrNull { it.currencyCode == currencyCode }
    return formatPrice(this, currency)
}


