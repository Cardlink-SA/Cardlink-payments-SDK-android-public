package gr.cardlink.payments.domain.model

internal data class ClientError(
    val type: String?,
    val message: String?
) {
    enum class ErrorType(val value: String) {
        CANCELLED("cancelled"),
        NETWORK("error_network"),
        INVALID_INPUT("error_invalid_input"),
        ENCRYPTION_SCRIPT("error_remote_encryption"),
        SERVER_ERROR("error_server"),
        REMOTE_ENCRYPTION("error_remote_validation"),
        ERROR_PAYMENT_METHODS("error_accepted_payment_methods"),
        ERROR_CARD_TYPES("error_accepted_card_types")
    }
}