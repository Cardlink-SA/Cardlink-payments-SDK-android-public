package gr.cardlink.payments.presentation.model

import gr.cardlink.payments.domain.model.Card

internal data class SingleCardInputModel(
    val pan: String?,
    val expirationDate: String?,
    val cvv: String?,
    val cardholder: String?,
    val type: Card.Type = Card.Type.UNKNOWN
)