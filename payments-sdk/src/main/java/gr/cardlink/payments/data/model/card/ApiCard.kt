package gr.cardlink.payments.data.model.card

import com.google.gson.annotations.SerializedName

internal data class ApiCard(
    @SerializedName("card_type")
    val type: String? = null,

    @SerializedName("last4")
    val visiblePan: String? = null,

    @SerializedName("expiry_month")
    val expirationMonth: String? = null,

    @SerializedName("expiry_year")
    val expirationYear: String? = null,

    @SerializedName("token")
    val token: String? = null
)