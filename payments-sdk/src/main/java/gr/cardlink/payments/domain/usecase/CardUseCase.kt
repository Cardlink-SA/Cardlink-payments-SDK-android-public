package gr.cardlink.payments.domain.usecase

import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.model.Installment
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.presentation.model.SingleCardInputModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

internal interface CardUseCase {
    fun getStoredCards(): Single<List<Card>>
    fun isCardValid(dataModel: SingleCardInputModel): Boolean // UI validation only
    fun onPanEvent(pan: String?)
    fun onCardEvent(card: Card?)
    fun getCardType(): Flowable<Card.Type>
    fun getCardValidation(): Flowable<CardValidation>
    fun clear()
    fun deleteCard(token: String): Completable
    fun getInstalments(amountInCents: Long): Single<List<Installment>>

    fun calculateFinalInstallments(amountInCents: Long, settings: Settings): List<Installment>

    // not implemented yet
    fun cardExists(pan: String): Single<Boolean>
}