package gr.cardlink.payments.presentation.ui.payment

import com.google.gson.Gson
import gr.cardlink.payments.data.model.payment.ApiPaymentContent
import gr.cardlink.payments.domain.mapper.toPaymentTransaction
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.PaymentTransaction
import gr.cardlink.payments.domain.usecase.PaymentUseCase
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.SingleSubject

internal class PaymentViewModel(
    private val paymentUseCase: PaymentUseCase
) : BaseViewModel() {

    private val paymentTransactionEmitter = SingleSubject.create<PaymentTransaction>()

    fun makePayment(card: Card, existingCardFlow: Boolean): Single<String> {
        return if (card.cardholder != null && card.token != null) {
            paymentUseCase.makePayment(
                token = card.token,
                cardType = card.type.value.lowercase(),
                cardholderName = card.cardholder,
                shouldStoreCard = card.shouldStore,
                existingCardFlow = existingCardFlow
            )
        } else {
            throw IllegalArgumentException("Cardholder name should not be null")
        }
    }

    fun parseHtmlContent(htmlContent: String) {
        try {
            val htmlContentWithoutEscapingChars = htmlContent.replace("\\", "")
            val htmlContentCleared = htmlContentWithoutEscapingChars.slice(1 until htmlContentWithoutEscapingChars.lastIndex)
            val gson = Gson()
            val transaction = gson.fromJson(htmlContentCleared, ApiPaymentContent::class.java).toPaymentTransaction()
            paymentTransactionEmitter.onSuccess(transaction)
        } catch (ex: Exception) {
            paymentTransactionEmitter.onError(ex)
        }
    }

    fun clearSession() = paymentUseCase.clearSession()

    fun getTransactionSingle() : Single<PaymentTransaction> = paymentTransactionEmitter

}