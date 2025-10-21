package gr.cardlink.payments.data.repository

import gr.cardlink.payments.data.model.ApiResponse
import gr.cardlink.payments.data.model.payment.ApiPaymentRequest
import gr.cardlink.payments.data.service.ApiService
import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.data.model.payment.ApiOnlinePaymentRequest
import gr.cardlink.payments.domain.repository.PaymentRepository
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.HttpURLConnection.HTTP_OK

internal class PaymentRepositoryImpl(
    private val apiService: ApiService
) : PaymentRepository {

    override fun makePayment(
        token: String?,
        cardType: String,
        existingToken: String?,
        cardholderName: String?,
        paymentRequest: PaymentRequest,
        installments: Int?,
        options: String?
    ): Single<String> {
        val apiPaymentRequest = ApiPaymentRequest(
            cardEncData = token,
            totalAmountInCents = paymentRequest.paymentTotalCents,
            cardholderName = cardholderName,
            currency = paymentRequest.currencyCode,
            city = paymentRequest.city,
            countryCode = paymentRequest.countryCode,
            addressLine = paymentRequest.addressLine,
            postalCode = paymentRequest.postalCode,
            description = paymentRequest.description,
            existingToken = existingToken,
            options = options,
            installments = installments,
            cardType = cardType,
            recurringFrequency = paymentRequest.recurringFrequency,
            recurringEndDate = paymentRequest.recurringEndDate
        )

        return apiService
            .makePayment(apiPaymentRequest)
            .subscribeOn(Schedulers.io())
            .map { handlePaymentResponse(it) }
    }

    private fun handlePaymentResponse(apiResponse: ApiResponse): String {
        return if (apiResponse.status == HTTP_OK && !apiResponse.body.isNullOrEmpty()) {
            apiResponse.body
        } else {
            throw RuntimeException(apiResponse.message ?: "Could not make payment")
        }
    }

    override fun makeOnlinePayment(
        onlinePaymentService: OnlinePaymentService,
        paymentRequest: PaymentRequest
    ): Single<String> {
        val request = ApiOnlinePaymentRequest(
            totalAmountInCents = paymentRequest.paymentTotalCents,
            addressLine = paymentRequest.addressLine,
            city = paymentRequest.city,
            postalCode = paymentRequest.postalCode
        )

        return when(onlinePaymentService) {
            is OnlinePaymentService.Iris -> {
                apiService
                    .payWithIris(request)
                    .subscribeOn(Schedulers.io())
            }
            is OnlinePaymentService.Paypal -> {
                apiService
                    .payWithPaypal(request)
                    .subscribeOn(Schedulers.io())
            }
        }
    }

}