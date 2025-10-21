package gr.cardlink.payments.domain.mapper

import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation

internal fun CardValidation.toCard(): Card? {
    return if (cardEncData == null || !valid || card == null) {
        null
    } else {
        card.copy(cvv = null, token = cardEncData)
    }
}