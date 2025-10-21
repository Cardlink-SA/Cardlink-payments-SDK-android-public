package gr.cardlink.payments.data.model.payment

import com.google.gson.annotations.SerializedName
import gr.cardlink.payments.data.model.ApiResponse

internal data class ApiPaymentContent(
   val transaction: ApiTransaction? = null
): ApiResponse()  {

    internal data class ApiTransaction(
        @SerializedName("OrderId")
        val orderId: String? = null,

        @SerializedName("Status")
        val status: String? = null,

        @SerializedName("OrderAmount")
        val orderAmount: String? = null,

        @SerializedName("Currency")
        val currency: String? = null,

        @SerializedName("PaymentTotal")
        val paymentTotal: String? = null,

        @SerializedName("PaymentRef")
        val paymentReference: String? = null,

        @SerializedName("TxId")
        val transactionId: String? = null,

        @SerializedName("ErrorCode")
        val errorCode: String? = null,

        @SerializedName("Description")
        val description: String? = null
    )
}