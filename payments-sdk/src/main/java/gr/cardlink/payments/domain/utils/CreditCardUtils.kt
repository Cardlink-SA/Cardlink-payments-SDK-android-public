package gr.cardlink.payments.domain.utils

import gr.cardlink.payments.data.validator.Validator
import gr.cardlink.payments.domain.model.Card
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

internal object CreditCardUtils {

    fun getVisiblePan(pan: String): String {
        return when (pan.length) {
            16 -> pan.substring(pan.lastIndex - 3..pan.lastIndex)
            4 -> pan
            else -> ""
        }
    }

    /**
     * @param expirationMonth 12
     * @param expirationYear 2025
     * @return 1225
     */
    fun getExpirationDate(expirationMonth: Int, expirationYear: Int): String {
        val expirationMonthFormatted = expirationMonth.toString().padStart(2, '0')
        val expirationYearFormatted = Year.of(expirationYear).toString().takeLast(2)
        return "$expirationMonthFormatted$expirationYearFormatted"
    }

    fun parseExpirationDate(expirationDate: String?): Pair<Int, Int>? {
        if (expirationDate == null) {
            return null
        }

        val dateChunked = expirationDate.chunked(2)
        val month = dateChunked.firstOrNull()
        val year = dateChunked.lastOrNull()

        if (month?.length != 2 || year?.length != 2) {
            return null
        }

        return try {
            val formatter = DateTimeFormatter.ofPattern("yy")
            val fullYear = formatter.parse(year).get(ChronoField.YEAR).toString()
            month.toInt() to fullYear.toInt()
        } catch (ex: Exception) {
            null
        }
    }

    fun getCardType(rawType: String): Card.Type {
        return when (rawType) {
            Validator.VISA -> Card.Type.VISA
            Validator.VISA_ELECTRON -> Card.Type.VISA_ELECTRON
            Validator.DINERS -> Card.Type.DINERS
            Validator.MASTERCARD -> Card.Type.MASTERCARD
            Validator.AMEX -> Card.Type.AMEX
            Validator.DISCOVER -> Card.Type.DISCOVER
            Validator.MAESTRO -> Card.Type.MAESTRO
            else -> Card.Type.UNKNOWN
        }
    }

}