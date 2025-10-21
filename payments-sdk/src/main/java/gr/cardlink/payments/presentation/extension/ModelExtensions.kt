package gr.cardlink.payments.presentation.extension

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import gr.cardlink.payments.R
import gr.cardlink.payments.presentation.model.CardRowModel
import gr.cardlink.payments.presentation.model.MultipleCardInputModel
import gr.cardlink.payments.presentation.model.SingleCardInputModel
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.domain.utils.CreditCardUtils
import gr.cardlink.payments.domain.utils.CreditCardUtils.parseExpirationDate

internal fun MultipleCardInputModel.toCardRowModel(selected: Boolean): CardRowModel {
    return CardRowModel(
        pan = pan,
        selected = selected,
        logoRes = type.toLogoRes(),
        typeName = type.value
    )
}

@DrawableRes
private fun Card.Type.toLogoRes(): Int? {
    return when (this) {
        Card.Type.VISA, Card.Type.VISA_ELECTRON -> R.drawable.ic_logo_visa_small
        Card.Type.MASTERCARD -> R.drawable.ic_logo_mastercard_small
        Card.Type.AMEX -> R.drawable.ic_logo_amex_small
        Card.Type.DISCOVER -> R.drawable.ic_logo_discover_small
        Card.Type.MAESTRO -> R.drawable.ic_logo_maestro_small
        Card.Type.DINERS -> R.drawable.ic_logo_diners_small
        else -> null
    }
}

internal fun Card.Type.toCardStyle(): Pair<Int, Int>? {
    return when (this) {
        Card.Type.VISA, Card.Type.VISA_ELECTRON -> R.drawable.ic_logo_visa to R.drawable.background_card_visa
        Card.Type.MASTERCARD -> R.drawable.ic_logo_mastercard to R.drawable.background_card_mastercard
        Card.Type.AMEX -> R.drawable.ic_logo_amex to R.drawable.background_card_amex
        Card.Type.DISCOVER -> R.drawable.ic_logo_discover to R.drawable.background_card_unknown
        Card.Type.MAESTRO -> R.drawable.ic_logo_maestro to R.drawable.background_card_unknown
        Card.Type.DINERS -> R.drawable.ic_logo_diners to R.drawable.background_card_diners
        else -> null
    }
}

internal fun SingleCardInputModel.toCard(): Card? {
    val parsedExpirationDate = parseExpirationDate(expirationDate) ?: return null
    val (month, year) = parsedExpirationDate

    safeLet(pan, cvv, cardholder) { (_pan, _cvv, _cardHolder) ->
        return try {
            Card(
                pan = _pan,
                cvv = _cvv,
                expirationMonth = month,
                expirationYear = year,
                cardholder = _cardHolder,
                type = type
            )
        } catch (ex: Exception) {
            null
        }
    }
    return null
}

internal fun Card.toSingleCardInputModel(): SingleCardInputModel {
    return SingleCardInputModel(
        pan = pan,
        cardholder = cardholder,
        cvv = cvv,
        type = type,
        expirationDate = CreditCardUtils.getExpirationDate(expirationMonth, expirationYear)
    )
}

internal fun Card.toMultipleCardInputModel(selected: Boolean): MultipleCardInputModel {
    return MultipleCardInputModel(
        pan = pan,
        expirationDate = CreditCardUtils.getExpirationDate(expirationMonth, expirationYear),
        cardholder = cardholder ?: "",
        type = type,
        selected = selected,
        token = token ?: ""
    )
}

internal fun MultipleCardInputModel.toCard(): Card? {
    val parsedExpirationDate = parseExpirationDate(expirationDate) ?: return null
    val (month, year) = parsedExpirationDate

    return if (token == null) {
        null
    } else {
        Card(
            pan = pan,
            cardholder = cardholder,
            cvv = null,
            type = type,
            token = token,
            expirationMonth = month,
            expirationYear = year
        )
    }
}

@DrawableRes
internal fun Settings.Acquirer.toDrawableRes(): Int {
    return when(this) {
        Settings.Acquirer.WORLDLINE -> R.drawable.ic_logo_worldline
        Settings.Acquirer.NEXI -> R.drawable.ic_logo_nexi
        else -> R.drawable.ic_logo_cardlink
    }
}

@ColorRes
internal fun Settings.Acquirer.toColorRes(): Int? {
    return if (this != Settings.Acquirer.WORLDLINE) {
        R.color.blue
    } else {
        null
    }
}