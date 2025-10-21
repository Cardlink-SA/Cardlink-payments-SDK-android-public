package gr.cardlink.payments.di

import gr.cardlink.payments.data.usecase.CardUseCaseImpl
import gr.cardlink.payments.data.usecase.PaymentUseCaseImpl
import gr.cardlink.payments.domain.usecase.CardUseCase
import gr.cardlink.payments.domain.usecase.PaymentUseCase

internal object UseCaseModule {

    val cardUseCase: CardUseCase
        get() = provideCardUseCase()

    private fun provideCardUseCase(): CardUseCase {
        return CardUseCaseImpl(
            cardRepository = DataModule.cardRepository,
            fileRepository = DataModule.fileRepository,
            validator = DataModule.validator,
            sessionRepository = DataModule.sessionRepository,
            settingsRepository = DataModule.settingsRepository
        )
    }

    val paymentUseCase: PaymentUseCase
        get() = providePaymentUseCase()

    private fun providePaymentUseCase(): PaymentUseCase {
        return PaymentUseCaseImpl(
            sessionRepository = DataModule.sessionRepository,
            paymentRepository = DataModule.paymentRepository
        )
    }

}