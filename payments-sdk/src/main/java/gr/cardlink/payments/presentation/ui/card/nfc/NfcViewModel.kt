package gr.cardlink.payments.presentation.ui.card.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.github.devnied.emvnfccard.model.EmvCard
import com.github.devnied.emvnfccard.parser.EmvTemplate
import gr.cardlink.payments.R
import gr.cardlink.payments.presentation.extension.toCard
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.usecase.CardUseCase
import gr.cardlink.payments.presentation.ui.card.nfc.provider.NfcProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.subjects.SingleSubject
import java.util.concurrent.TimeUnit

internal class NfcViewModel(
    private val cardUseCase: CardUseCase
) : BaseViewModel() {

    private var emvCard = EmvCard()
    private val cardUnreadableEmitter = PublishProcessor.create<Int>()
    private val cardSuccessReadEmitter = SingleSubject.create<Card>()
    private val nfcErrorEmitter = PublishProcessor.create<Int>()

    private val unreadableCardRes = R.string.sdk_quick_add_tap_error_card_unreadable
    private val existingCardRes = R.string.sdk_quick_add_tap_error_existing_card

    private val isoDepConfig by lazy {
        EmvTemplate.Config()
            .setContactLess(true)
            .setReadAllAids(true)
            .setReadTransactions(false)
            .setRemoveDefaultParsers(false)
            .setReadAt(true)
    }

    fun onNfcTagDiscovered(tag: Tag?) {
        tag?.let { _tag ->
            val isoDep = IsoDep.get(_tag)
            isoDep?.let { _isoDep ->
                try {
                    _isoDep.connect()
                    val parser = getEmvParser(_isoDep)
                    val card = parser.readEmvCard()
                    handleEmvCardResponse(card)
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message ?: "on NFC tag error")
                } finally {
                    _isoDep.close()
                }
            }
        }
    }

    private fun handleEmvCardResponse(emvCard: EmvCard) {
        this.emvCard = emvCard
        if (emvCard.cardNumber == null || emvCard.expireDate == null || emvCard.type == null) {
            cardUnreadableEmitter.onNext(unreadableCardRes)
        } else {
            val card = emvCard.toCard()
            card?.let { _card ->
                observeExistingCard(_card)
            }
        }
    }

    private fun observeExistingCard(card: Card) {
        cardUseCase
            .cardExists(card.pan)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { cardExists ->
                    if (cardExists) {
                        cardUnreadableEmitter.onNext(existingCardRes)
                    } else {
                        cardSuccessReadEmitter.onSuccess(card)
                    }
                },
                onError = {
                    cardUnreadableEmitter.onNext(unreadableCardRes)
                }
            )
            .addTo(disposables)
    }

    private fun getEmvParser(isoDep: IsoDep): EmvTemplate {
        return EmvTemplate.Builder()
            .setProvider(NfcProvider(isoDep))
            .setConfig(isoDepConfig)
            .build()
    }

    fun onTimerFinished() {
        handleEmvCardResponse(EmvCard())
    }

    fun getCounterFlowable(): Flowable<Long> {
        return Flowable
            .intervalRange(COUNTER_START, COUNTER_TOTAL, COUNTER_START, COUNTER_TICK_PERIOD, TimeUnit.SECONDS)
            .map { (COUNTER_TOTAL) - (it + 1) }
    }

    fun getCardUnreadableFlowable(): Flowable<Int> = cardUnreadableEmitter

    fun getCardReadSuccessFlowable(): Single<Card> = cardSuccessReadEmitter

    fun getNfcErrorFlowable(): Flowable<Int> = nfcErrorEmitter
        .debounce(NFC_ERRORS_DEBOUNCE, TimeUnit.MILLISECONDS)

    companion object {
        private const val TAG = "NfcViewModel"

        private const val NFC_ERRORS_DEBOUNCE = 200L
        private const val COUNTER_TOTAL = 30L
        private const val COUNTER_START = 0L
        private const val COUNTER_TICK_PERIOD = 1L
    }

}