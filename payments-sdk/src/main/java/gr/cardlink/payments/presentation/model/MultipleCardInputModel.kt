package gr.cardlink.payments.presentation.model

import gr.cardlink.payments.domain.model.Card

internal data class MultipleCardInputModel(
    val pan: String,
    val expirationDate: String,
    val cardholder: String,
    val type: Card.Type,
    val token: String?,
    val selected: Boolean
)