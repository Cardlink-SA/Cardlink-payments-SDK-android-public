package gr.cardlink.payments.data.service

import gr.cardlink.payments.data.model.ApiResponse
import gr.cardlink.payments.data.model.card.ApiCardDeleteRequest
import gr.cardlink.payments.data.model.card.ApiCardsResponse
import gr.cardlink.payments.data.model.payment.ApiOnlinePaymentRequest
import gr.cardlink.payments.data.model.payment.ApiPaymentRequest
import gr.cardlink.payments.data.model.settings.ApiSettingsResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

internal interface ApiService {

    companion object {
        private const val CONTENT_TYPE = "content-type: application/json; charset=UTF-8"
        private const val ENDPOINT_PAYMENT = "payment/"
        private const val ENDPOINT_USER_CARDS = "user-cards/"
        private const val ENDPOINT_USER_CARDS_DELETE = "user-cards/delete/"
        private const val ENDPOINT_SETTINGS = "settings/"
        private const val ENDPOINT_PAYMENT_IRIS = ENDPOINT_PAYMENT + "iris/"
        private const val ENDPOINT_PAYMENT_PAYPAL = ENDPOINT_PAYMENT + "paypal/"
    }

    @Streaming
    @POST
    fun downloadFile(@Url fileUrl: String): Single<ApiResponse>

    @Headers(CONTENT_TYPE)
    @POST(ENDPOINT_PAYMENT)
    fun makePayment(@Body apiPaymentRequest: ApiPaymentRequest): Single<ApiResponse>

    @Headers(CONTENT_TYPE)
    @POST(ENDPOINT_USER_CARDS)
    fun getUserCards(): Single<ApiCardsResponse>

    @Headers(CONTENT_TYPE)
    @POST(ENDPOINT_USER_CARDS_DELETE)
    fun deleteCard(@Body apiCardDeleteRequest: ApiCardDeleteRequest): Single<ApiResponse>

    @Headers(CONTENT_TYPE)
    @POST(ENDPOINT_SETTINGS)
    fun getSettings(): Single<ApiSettingsResponse>

    @Headers(CONTENT_TYPE)
    @POST(ENDPOINT_PAYMENT_IRIS)
    fun payWithIris(@Body request: ApiOnlinePaymentRequest): Single<String>

    @Headers(CONTENT_TYPE)
    @POST(ENDPOINT_PAYMENT_PAYPAL)
    fun payWithPaypal(@Body request: ApiOnlinePaymentRequest): Single<String>

}