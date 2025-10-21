package gr.cardlink.payments.data.validator

import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.model.Card
import io.reactivex.rxjava3.core.Flowable

internal interface Validator {
    companion object {
        const val VISA = "visa"
        const val VISA_ELECTRON = "visaElectron"
        const val DISCOVER = "discover"
        const val MAESTRO = "maestro"
        const val AMEX = "amex"
        const val MASTERCARD = "mastercard"
        const val DINERS = "diners"
    }

    fun onCartTypeEvent(jsScript: String, pan: String): String
    fun getCardType(): Flowable<Card.Type>
    fun onEncryptCardEvent(jsScript: String, card: Card): String
    fun getCardValidation(): Flowable<CardValidation>
    fun clear() { /* default empty implementation */ }
}