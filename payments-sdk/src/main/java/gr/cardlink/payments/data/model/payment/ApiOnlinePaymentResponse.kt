package gr.cardlink.payments.data.model.payment

import com.google.gson.annotations.SerializedName
import gr.cardlink.payments.data.model.ApiResponse

internal data class ApiOnlinePaymentResponse(
    @SerializedName("data")
    val data: Data? = null
) : ApiResponse() {
    internal data class Data(
        @SerializedName("orderid")
        val orderId: String? = null,

        @SerializedName("txId")
        val transactionId: String? = null,

        @SerializedName("version")
        val version: String? = null,

        @SerializedName("mid")
        val mid: String? = null,

        @SerializedName("status")
        val status: String? = null,

        @SerializedName("currency")
        val currency: String? = null,

        @SerializedName("paymentTotal")
        val paymentTotal: String? = null,

        @SerializedName("orderAmount")
        val orderAmount: String? = null,

        @SerializedName("message")
        val message: String? = null,

        @SerializedName("riskScore")
        val riskScore: String? = null,

        @SerializedName("digest")
        val digest: String? = null
    )
}

