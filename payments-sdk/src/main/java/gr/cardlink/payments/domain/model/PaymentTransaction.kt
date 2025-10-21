package gr.cardlink.payments.domain.model

internal data class PaymentTransaction(
    val orderId: String,
    val status: Status,
    val transactionId: String,
    val errorCode: String?,
    val description: String?,
    val orderAmount: String?,
    val paymentTotal: String?,
    val currency: String?,
    val paymentReference: String?
) {

    val isTransactionSuccessful
        get() = status == Status.AUTHORIZED || status == Status.CAPTURED

    internal enum class Status {
        CAPTURED,
        ERROR,
        AUTHORIZED,
        UNKNOWN;
    }
}