package gr.cardlink.payments.presentation.ui.card.add

import gr.cardlink.payments.presentation.model.SingleCardInputModel
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.usecase.CardUseCase
import gr.cardlink.payments.presentation.extension.toCard
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

internal class AddCardViewModel(
    private val cardUseCase: CardUseCase
) : BaseViewModel() {

    private val addCardStateEmitter = PublishProcessor.create<Boolean>()

    fun validateCard(dataModel: SingleCardInputModel) {
        val valid = cardUseCase.isCardValid(dataModel)
        cardUseCase.onPanEvent(dataModel.pan)
        addCardStateEmitter.onNext(valid)
    }

    fun onCardEvent(inputModel: SingleCardInputModel, shouldStoreCard: Boolean) {
        val card = inputModel.toCard()?.copy(shouldStore = shouldStoreCard)
        cardUseCase.onCardEvent(card)
    }

    fun getProceedCardButtonObservable(): Flowable<Boolean> = addCardStateEmitter

    fun getCreditCardTypeObservable(): Flowable<Card.Type> = cardUseCase.getCardType()

    fun getCreditCardValidationObservable(): Flowable<CardValidation> = cardUseCase.getCardValidation()

    override fun onCleared() {
        super.onCleared()
        cardUseCase.clear()
    }

}