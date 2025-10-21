package gr.cardlink.payments.domain.model

internal data class Settings(
    val currencyCode: String?,
    val instalmentsConfig: InstalmentsConfig?,
    val acceptedCardTypes: List<AcceptedCardType>,
    val acceptedPaymentMethods: List<AcceptedPaymentMethod>,
    val acquirer: Acquirer
) {
    enum class AcceptedCardType(val value: String) {
        VISA("visa"),
        MASTERCARD("mastercard"),
        MAESTRO("maestro"),
        DINERS("diners"),
        DISCOVER("discover"),
        AMEX("amex")
    }

    enum class AcceptedPaymentMethod(val value: String) {
        IRIS("iris"),
        PAYPAL("paypal"),
        CARD("card")
    }

    enum class Acquirer(val value: String) {
        CARDLINK("cardlink"),
        WORLDLINE("worldline"),
        NEXI("nexi"),
        UNKNOWN("unknown")
    }
}