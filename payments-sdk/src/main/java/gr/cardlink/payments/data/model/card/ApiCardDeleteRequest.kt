package gr.cardlink.payments.data.model.card

import com.google.gson.annotations.SerializedName

internal data class ApiCardDeleteRequest(
    @SerializedName("card_token")
    val cardToken: String? = null
)