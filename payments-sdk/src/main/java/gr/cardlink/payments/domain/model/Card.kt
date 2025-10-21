package gr.cardlink.payments.domain.model

import java.io.Serializable

internal data class Card(
    val pan: String,
    val expirationMonth: Int,
    val expirationYear: Int,
    val cvv: String?,
    val cardholder: String?,
    val type: Type,
    val token: String? = null,
    val shouldStore: Boolean = false
): Serializable {

    enum class Type(val value: String, val cvvLength: Int) {
        UNKNOWN("unknown", 4),
        VISA("Visa", 3),
        VISA_ELECTRON ("Visa Electron", 3),
        DISCOVER("Discover", 3),
        MAESTRO("Maestro", 3),
        AMEX("Amex", 4),
        MASTERCARD("Mastercard", 3),
        DINERS("Diners", 3)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        if (pan != other.pan) return false

        return true
    }

    override fun hashCode(): Int {
        return pan.hashCode()
    }

}