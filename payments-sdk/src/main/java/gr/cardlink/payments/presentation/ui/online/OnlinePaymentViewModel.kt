package gr.cardlink.payments.presentation.ui.online

import com.google.gson.Gson
import gr.cardlink.payments.data.model.payment.ApiOnlinePaymentResponse
import gr.cardlink.payments.domain.mapper.toPaymentTransaction
import gr.cardlink.payments.domain.model.PaymentTransaction
import gr.cardlink.payments.domain.usecase.PaymentUseCase
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.SingleSubject

internal class OnlinePaymentViewModel(
    private val paymentUseCase: PaymentUseCase
) : BaseViewModel() {

    private val paymentTransactionEmitter = SingleSubject.create<PaymentTransaction>()

    fun getPaymentObservable(onlinePaymentService: OnlinePaymentService) = paymentUseCase.makeOnlinePayment(onlinePaymentService)

    fun clearSession() = paymentUseCase.clearSession()

    fun parseHtmlContent(content: String) {
        try {
            val htmlContentWithoutEscapingChars = content.replace("\\", "")
            val htmlContentCleared = htmlContentWithoutEscapingChars.slice(1 until htmlContentWithoutEscapingChars.lastIndex)
            val gson = Gson()
            val transaction = gson.fromJson(htmlContentCleared, ApiOnlinePaymentResponse::class.java).toPaymentTransaction()
            paymentTransactionEmitter.onSuccess(transaction)
        } catch (ex: Exception) {
            paymentTransactionEmitter.onError(ex)
        }
    }

    fun getTransactionSingle() : Single<PaymentTransaction> = paymentTransactionEmitter

}