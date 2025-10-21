package gr.cardlink.payments.presentation.ui.checkout

import android.util.Log
import gr.cardlink.payments.presentation.extension.toMultipleCardInputModel
import gr.cardlink.payments.presentation.model.MultipleCardInputModel
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.Installment
import gr.cardlink.payments.domain.repository.SessionRepository
import gr.cardlink.payments.domain.usecase.CardUseCase
import gr.cardlink.payments.presentation.extension.toCard
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.PublishProcessor

internal class CheckoutViewModel(
    private val cardUseCase: CardUseCase,
    private val sessionRepository: SessionRepository
) : BaseViewModel() {

    companion object {
        private const val TAG = "CheckoutViewModel"
    }

    private val selectedCardEmitter = PublishProcessor.create<Card>()

    val acquirerRes: Int?
        get() = sessionRepository.get(SessionRepository.Key.ACQUIRER_RES)

    fun getStoredCardsSingle(): Single<List<MultipleCardInputModel>> {
        return cardUseCase
            .getStoredCards()
            .map {
                it.mapIndexed { index, card -> card.toMultipleCardInputModel(index == 0) }
            }
    }

    fun getInstallmentsSingle(cartTotal: Long): Single<List<Installment>> {
        return cardUseCase.getInstalments(cartTotal)
    }

    fun getSelectedCardFlowable(): Flowable<Card> = selectedCardEmitter

    fun deleteCard(token: String?, pan: String) {
        if (token == null) {
            Log.e(TAG, "Could not remove card $pan")
            return
        }

        cardUseCase
            .deleteCard(token)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { Log.d(TAG, "Card $pan is removed") },
                onError = { Log.e(TAG, "Could not remove card $pan", it) }
            )
            .addTo(disposables)
    }

    fun selectCard(selectedCard: MultipleCardInputModel) {
        val card = selectedCard.toCard()
        if (card != null && !card.token.isNullOrEmpty()) {
            selectedCardEmitter.onNext(card)
        }
    }

    fun cacheInstallments(installments: Int) {
        sessionRepository.set(SessionRepository.Key.INSTALLMENTS, installments)
    }

}