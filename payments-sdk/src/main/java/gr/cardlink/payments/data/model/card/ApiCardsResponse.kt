package gr.cardlink.payments.data.model.card

import com.google.gson.annotations.SerializedName
import gr.cardlink.payments.data.model.ApiResponse

internal data class ApiCardsResponse(
    @SerializedName("cards")
    val cards: List<ApiCard>
) : ApiResponse()