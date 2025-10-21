package gr.cardlink.payments.domain.repository

import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentService
import io.reactivex.rxjava3.core.Single

internal interface PaymentRepository {

    fun makePayment(
        token: String?,
        cardType: String,
        existingToken: String?,
        cardholderName: String?,
        paymentRequest: PaymentRequest,
        installments: Int?,
        options: String?
    ): Single<String>

    fun makeOnlinePayment(
        onlinePaymentService: OnlinePaymentService,
        paymentRequest: PaymentRequest
    ): Single<String>

}