package gr.cardlink.payments.data.model

import com.google.gson.annotations.SerializedName

internal open class ApiResponse(
    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("body")
    val body: String? = null
)