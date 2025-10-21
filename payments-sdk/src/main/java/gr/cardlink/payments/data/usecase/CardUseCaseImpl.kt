package gr.cardlink.payments.data.usecase

import android.util.Log
import gr.cardlink.payments.config.Config
import gr.cardlink.payments.data.validator.Validator
import gr.cardlink.payments.domain.model.*
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.model.Installment
import gr.cardlink.payments.domain.repository.CardRepository
import gr.cardlink.payments.domain.repository.FileRepository
import gr.cardlink.payments.domain.repository.SessionRepository
import gr.cardlink.payments.domain.repository.SettingsRepository
import gr.cardlink.payments.domain.usecase.CardUseCase
import gr.cardlink.payments.presentation.model.SingleCardInputModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.PublishProcessor

internal class CardUseCaseImpl(
    private val cardRepository: CardRepository,
    private val fileRepository: FileRepository,
    private val validator: Validator,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository
) : CardUseCase {

    companion object {
        private const val TAG = "CardUseCase"
    }

    private var validatorJs: String = ""
        @Synchronized set

    private val panEventEmitter = PublishProcessor.create<String>()
    private val disposables = CompositeDisposable()
    private val cardEventEmitter = PublishProcessor.create<Card>()

    init {
        observeRemoteValidator()
        observePanEvents()
        observeCardValidation()
    }

    private fun observeRemoteValidator() {
        fileRepository
            .getRemoteValidator()
            .onErrorReturn { "" }
            .subscribeBy(
                onSuccess = { validatorJs = it },
                onError = { Log.e(TAG, it.localizedMessage, it) }
            ).addTo(disposables)
    }

    private fun observePanEvents() {
        panEventEmitter
            .onErrorResumeWith(Flowable.just(""))
            .filter { it.isNotEmpty() }
            .filter { it.length in 1..5 }
            .switchMap { pan ->
                getValidatorFlowable()
                    .map { validator.onCartTypeEvent(it, pan) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { /* no-op */ },
                onError = { Log.e(TAG, it.localizedMessage, it) }
            ).addTo(disposables)
    }

    private fun getValidatorFlowable(): Flowable<String> {
        return if (validatorJs.isNotEmpty()) {
            Flowable.just(validatorJs)
        } else {
            fileRepository
                .getRemoteValidator()
                .doOnSuccess { validatorJs = it }
                .toFlowable()
        }
    }

    private fun observeCardValidation() {
        cardEventEmitter
            .switchMap { card ->
                getValidatorFlowable()
                    .map { validator.onEncryptCardEvent(it, card) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { /* no-op */ },
                onError = { Log.e(TAG, it.localizedMessage, it) }
            ).addTo(disposables)
    }

    override fun getStoredCards(): Single<List<Card>> = cardRepository.getUserCards()

    override fun isCardValid(dataModel: SingleCardInputModel): Boolean {
        return isPanLengthValid(dataModel.pan)
                && isCvvLengthValid(dataModel.cvv)
                && isExpirationDateLengthValid(dataModel.expirationDate)
                && isCardholderValid(dataModel.cardholder)
    }

    private fun isPanLengthValid(pan: String?) = pan?.length == Config.PAN_LENGTH

    private fun isCvvLengthValid(cvv: String?) = cvv?.length in 2..4

    private fun isExpirationDateLengthValid(expirationDate: String?): Boolean {
        return expirationDate?.length == Config.EXPIRATION_DATE_LENGTH
    }

    private fun isCardholderValid(cardholder: String?) = !cardholder.isNullOrBlank()

    override fun onPanEvent(pan: String?) {
        pan?.let {
            panEventEmitter.onNext(it)
        }
    }

    override fun onCardEvent(card: Card?) {
        card?.let {
            cardEventEmitter.onNext(it)
        }
    }

    override fun getCardType(): Flowable<Card.Type> = validator.getCardType()

    override fun getCardValidation(): Flowable<CardValidation> = validator.getCardValidation()

    override fun clear() {
        validatorJs = ""
        validator.clear()
        disposables.dispose()
    }

    override fun deleteCard(token: String): Completable = cardRepository.deleteCard(token)

    override fun cardExists(pan: String): Single<Boolean> {
        // TODO: cardExists > not yet implemented
        return Single.just(false)
    }

    override fun getInstalments(amountInCents: Long): Single<List<Installment>> {
        val installments = sessionRepository.get<List<Installment>>(SessionRepository.Key.INSTALLMENTS_LIST)
        return installments?.let {
            Single.just(it)
        } ?: settingsRepository
            .getSettings()
            .map { calculateFinalInstallments(amountInCents, it) }
    }

     override fun calculateFinalInstallments(amountInCents: Long, settings: Settings): List<Installment> {
        // Δεν υποστηρίζονται δόσεις όταν:
        // 1. το currency code (πχ. EUR) είναι null ή δεν υπάρχει config για δόσεις
        // 2. Δεν υπάρχουν variations και ο μέγιστος αριθμός δόσεων είναι <= 1
        if (settings.currencyCode == null || settings.instalmentsConfig == null) {
            return emptyList()
        }

        val config = settings.instalmentsConfig
        if (config.variations.isEmpty() && config.maxInstallments <= 1) {
            return emptyList()
        }

        // Αν υπάρχουν variations, αγνοούμε το maxInstallments
        if (config.variations.isNotEmpty()) {
            var totalInstallments = 1
            config.variations.forEach {
                val variationAmountInCents = it.amount * 100
                if (amountInCents >= variationAmountInCents) {
                    totalInstallments = it.total
                }
            }
            return if (totalInstallments > 1) {
                generateInstallments(totalInstallments, amountInCents, settings.currencyCode)
            } else {
                emptyList()
            }
        }

        // Αν υπάρχουν maxInstallments, τότε δημιουργούμε δόσεις ανάλογα με το ποσό πληρωμής
        return generateInstallments(config.maxInstallments, amountInCents, settings.currencyCode)
    }

    private fun generateInstallments(totalInstallments: Int, amountInCents: Long, currencyCode: String): List<Installment> {
        val installments = mutableListOf<Installment>()
        for (installment in 1..totalInstallments) {
            val installmentAmount = (amountInCents / installment.toDouble())
            Installment(
                currencyCode = currencyCode,
                count = installment,
                installmentAmount = installmentAmount
            ).also {
                installments.add(it)
            }
        }
        return installments
    }

}