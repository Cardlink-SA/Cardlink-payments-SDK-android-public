package gr.cardlink.payments.domain.usecase

import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentService
import io.reactivex.rxjava3.core.Single

internal interface PaymentUseCase {
    fun cachePaymentInfo(paymentRequest: PaymentRequest)
    fun makePayment(token: String, cardType: String, cardholderName: String, shouldStoreCard: Boolean, existingCardFlow: Boolean): Single<String>
    fun makeOnlinePayment(onlinePaymentService: OnlinePaymentService): Single<String>

    fun clearSession()
}