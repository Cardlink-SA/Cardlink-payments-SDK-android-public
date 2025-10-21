package gr.cardlink.payments.domain.model

import com.google.gson.annotations.SerializedName

// annotation seems redundant, however is required by GSON for internal obfuscation handling
internal data class CardValidation(
    @SerializedName("card")
    val card: Card?,

    @SerializedName("cardEncData")
    val cardEncData: String?,

    @SerializedName("valid")
    val valid: Boolean,

    @SerializedName("errorPan")
    val errorPan: String?,

    @SerializedName("errorExpiryMonth")
    val errorExpirationMonth: String?,

    @SerializedName("errorExpiryYear")
    val errorExpirationYear: String?,

    @SerializedName("errorCvv")
    val errorCvv: String?,

    @SerializedName("errorCardholder")
    val errorCardholder: String?
)