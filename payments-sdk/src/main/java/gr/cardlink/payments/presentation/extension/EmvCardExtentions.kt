package gr.cardlink.payments.presentation.extension

import com.github.devnied.emvnfccard.enums.EmvCardScheme
import com.github.devnied.emvnfccard.model.EmvCard
import gr.cardlink.payments.domain.model.Card
import java.time.ZoneId

internal fun EmvCard.toCard(): Card? {
    val cardholderInternal = if (holderFirstname != null && holderLastname != null) {
        "$holderFirstname $holderLastname"
    } else {
        null
    }

    val localDate = expireDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return try {
        Card(
            pan = cardNumber,
            cardholder = cardholderInternal,
            type = type.toCardType(),
            cvv = null,
            expirationMonth = localDate.monthValue,
            expirationYear = localDate.year
        )
    } catch (ex: Exception) {
        null
    }
}

private fun EmvCardScheme.toCardType(): Card.Type {
    return when (this) {
        EmvCardScheme.VISA -> Card.Type.VISA
        EmvCardScheme.AMERICAN_EXPRESS -> Card.Type.AMEX
        EmvCardScheme.MASTER_CARD -> Card.Type.MASTERCARD
        // TODO: Remaining cards
        else -> Card.Type.UNKNOWN
    }
}