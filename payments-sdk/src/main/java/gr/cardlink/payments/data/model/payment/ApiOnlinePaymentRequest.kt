package gr.cardlink.payments.data.model.payment

import com.google.gson.annotations.SerializedName

internal data class ApiOnlinePaymentRequest(
    @SerializedName("purchAmount")
    val totalAmountInCents: Long,

    @SerializedName("billAddress")
    val addressLine: String,

    @SerializedName("billCity")
    val city: String,

    @SerializedName("billZip")
    val postalCode: String
)