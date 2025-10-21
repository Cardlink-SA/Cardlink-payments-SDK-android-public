package gr.cardlink.payments.presentation.ui.method

import androidx.annotation.DrawableRes
import gr.cardlink.payments.domain.model.ClientError
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.domain.repository.SessionRepository
import gr.cardlink.payments.domain.repository.SettingsRepository
import gr.cardlink.payments.domain.usecase.CardUseCase
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import io.reactivex.rxjava3.core.Single

internal class PaymentMethodViewModel(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val cardUseCase: CardUseCase
): BaseViewModel() {

    private val acquirerRes: Int?
        get() = sessionRepository.get(SessionRepository.Key.ACQUIRER_RES)

    fun getStateSingle(cartTotal: Long): Single<State> {
        return settingsRepository
            .getSettings()
            .flatMap {
                if (it.acceptedPaymentMethods.isEmpty()) {
                    Single.error(Throwable(ClientError.ErrorType.ERROR_PAYMENT_METHODS.value))
                } else if (it.acceptedCardTypes.isEmpty()) {
                    Single.error(Throwable(ClientError.ErrorType.ERROR_CARD_TYPES.value))
                } else {
                    cacheInstallmentsList(cartTotal, it)
                    val state = State(
                        acceptedCardTypes = it.acceptedCardTypes,
                        acceptedPaymentMethods = it.acceptedPaymentMethods,
                        acquirerRes = acquirerRes
                    )
                    Single.just(state)
                }
            }.doOnError { sessionRepository.set(SessionRepository.Key.INSTALLMENTS_LIST, null) }
    }

    private fun cacheInstallmentsList(cartTotal: Long, settings: Settings) {
        val installments = cardUseCase.calculateFinalInstallments(cartTotal, settings)
        sessionRepository.set(SessionRepository.Key.INSTALLMENTS_LIST, installments)
    }

    data class State(
        val acceptedCardTypes: List<Settings.AcceptedCardType>,
        val acceptedPaymentMethods: List<Settings.AcceptedPaymentMethod>,
        @DrawableRes val acquirerRes: Int?
    )

}