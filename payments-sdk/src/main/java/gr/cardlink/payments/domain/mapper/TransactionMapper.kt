package gr.cardlink.payments.domain.mapper

import gr.cardlink.payments.data.model.payment.ApiPaymentContent
import gr.cardlink.payments.api.PaymentResponse
import gr.cardlink.payments.data.model.payment.ApiOnlinePaymentResponse
import gr.cardlink.payments.domain.model.PaymentTransaction

internal fun ApiPaymentContent.toPaymentTransaction(): PaymentTransaction {
    return PaymentTransaction(
        orderId = transaction?.orderId ?: "",
        status = PaymentTransaction.Status.values().firstOrNull { it.name == transaction?.status }
            ?: PaymentTransaction.Status.UNKNOWN,
        transactionId = transaction?.transactionId ?: "",
        errorCode = transaction?.errorCode ?: "$status",
        description = transaction?.description ?: message,
        orderAmount = transaction?.orderAmount,
        paymentTotal = transaction?.paymentTotal,
        currency = transaction?.currency,
        paymentReference = transaction?.paymentReference
    )
}

internal fun ApiOnlinePaymentResponse.toPaymentTransaction(): PaymentTransaction {
    return PaymentTransaction(
        orderId = data?.orderId ?: "",
        status = PaymentTransaction.Status.ERROR,
        transactionId = data?.transactionId ?: "",
        errorCode = null,
        description = data?.message,
        orderAmount = data?.orderAmount,
        currency = data?.currency,
        paymentReference = "",
        paymentTotal = data?.paymentTotal
    )
}

internal fun PaymentTransaction.toPaymentResponse(): PaymentResponse {
    return PaymentResponse(
        orderId = orderId,
        transactionId = transactionId,
        errorCode = errorCode,
        description = description,
        orderAmount = orderAmount,
        paymentTotal = paymentTotal,
        currency = currency,
        paymentReference = paymentReference
    )
}