package gr.cardlink.payments.data.usecase

import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.config.Config
import gr.cardlink.payments.domain.repository.PaymentRepository
import gr.cardlink.payments.domain.repository.SessionRepository
import gr.cardlink.payments.domain.usecase.PaymentUseCase
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentService
import io.reactivex.rxjava3.core.Single

internal class PaymentUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : PaymentUseCase {

    override fun cachePaymentInfo(paymentRequest: PaymentRequest) {
        sessionRepository.set(SessionRepository.Key.PAYMENT_INFO, paymentRequest)
    }

    override fun makePayment(token: String, cardType: String, cardholderName: String, shouldStoreCard: Boolean, existingCardFlow: Boolean): Single<String> {
        val paymentInfo = getCachedPaymentInfo()

       return paymentInfo?.let {
            paymentRepository.makePayment(
                token = if (existingCardFlow) null else token,
                cardType = cardType,
                existingToken = if (existingCardFlow) token else null,
                paymentRequest = paymentInfo,
                cardholderName = cardholderName.ifBlank { null },
                installments = getInstallments(),
                options = getCardOptions(existingCardFlow, shouldStoreCard)
            )
        } ?: throw IllegalArgumentException("No cached payment info.")
    }

    override fun makeOnlinePayment(onlinePaymentService: OnlinePaymentService): Single<String> {
        return getCachedPaymentInfo()?.let {
            paymentRepository.makeOnlinePayment(
                onlinePaymentService = onlinePaymentService,
                paymentRequest = it
            )
        } ?: throw IllegalArgumentException("No cached payment info.")
    }

    private fun getInstallments(): Int? {
        val cachedInstallments = sessionRepository.get(SessionRepository.Key.INSTALLMENTS) ?: 1
        return if (cachedInstallments > 1) cachedInstallments else null
    }

    private fun getCardOptions(existingCardFlow: Boolean, shouldStoreCard: Boolean): String? {
        return if (shouldStoreCard && !existingCardFlow) {
            Config.OPTIONS_STORE_CARD
        } else if (existingCardFlow) {
            Config.OPTIONS_EXISTING_CARD
        } else {
            null
        }
    }

    private fun getCachedPaymentInfo() = sessionRepository.get<PaymentRequest>(SessionRepository.Key.PAYMENT_INFO)

    override fun clearSession() = sessionRepository.clear()

}