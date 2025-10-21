package gr.cardlink.payments.data.model.settings

import com.google.gson.annotations.SerializedName
import gr.cardlink.payments.data.model.ApiResponse

internal data class ApiSettingsResponse(
    @SerializedName("settings")
    val settings: ApiSettings? = null
) : ApiResponse() {

    data class ApiSettings(
        @SerializedName("currency")
        val currency: String? = null,

        @SerializedName("tokenization")
        val tokenization: Boolean? = null,

        @SerializedName("acquirer")
        val acquirer: String? = null,

        @SerializedName("installments")
        val installments: Boolean? = null,

        @SerializedName("max_installments")
        val maxInstallments: Int? = null,

        @SerializedName("installments_variations")
        val installmentsVariations: List<ApiInstalmentVariation>? = null,

        @SerializedName("accepted_card_types")
        val acceptedCardTypes: List<String>? = null,

        @SerializedName("accepted_payment_methods")
        val acceptedPaymentMethods: List<String>? = null
    )

    data class ApiInstalmentVariation(
        @SerializedName("amount")
        val installmentAmount: Long? = null,

        @SerializedName("installments")
        val totalInstallments: Int? = null
    )

}