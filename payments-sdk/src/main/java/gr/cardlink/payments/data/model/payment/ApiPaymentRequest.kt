package gr.cardlink.payments.data.model.payment

import com.google.gson.annotations.SerializedName

internal data class ApiPaymentRequest(
    @SerializedName("extToken")
    val existingToken: String? = null,

    @SerializedName("extTokenOptions")
    val options: String? = null,

    @SerializedName("purchAmount")
    val totalAmountInCents: Long,

    @SerializedName("TDS2CardholderName")
    val cardholderName: String? = null,

    @SerializedName("TDS2BillAddrCity")
    val city: String,

    @SerializedName("TDS2BillAddrLine1")
    val addressLine: String,

    @SerializedName("TDS2BillAddrCountry")
    val countryCode: String,

    @SerializedName("TDS2BillAddrPostCode")
    val postalCode: String,

    @SerializedName("cardType")
    val cardType: String,

    @SerializedName("cardEncData")
    val cardEncData: String? = null,

    @SerializedName("description")
    val description: String,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("installments")
    val installments: Int? = null,

    @SerializedName("recurFreq")
    val recurringFrequency: Int? = null,

    @SerializedName("recurEnd")
    val recurringEndDate: String? = null
)