package gr.cardlink.payments.di

import gr.cardlink.payments.presentation.ui.CardlinkPaymentsViewModel
import gr.cardlink.payments.presentation.ui.card.add.AddCardViewModel
import gr.cardlink.payments.presentation.ui.card.nfc.NfcViewModel
import gr.cardlink.payments.presentation.ui.checkout.CheckoutViewModel
import gr.cardlink.payments.presentation.ui.method.PaymentMethodViewModel
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentViewModel
import gr.cardlink.payments.presentation.ui.payment.PaymentViewModel

internal object ViewModelModule {

    val checkoutViewModel
        get() = CheckoutViewModel(
            cardUseCase = UseCaseModule.cardUseCase,
            sessionRepository = DataModule.sessionRepository
        )

    val addCardViewModel
        get() = AddCardViewModel(UseCaseModule.cardUseCase)

    val nfcViewModel
        get() = NfcViewModel(UseCaseModule.cardUseCase)

    val paymentViewModel
        get() = PaymentViewModel(UseCaseModule.paymentUseCase)

    val cardlinkPaymentsViewModel
        get() = CardlinkPaymentsViewModel(
            paymentUseCase = UseCaseModule.paymentUseCase,
            sessionRepository = DataModule.sessionRepository
        )

    val onlinePaymentViewModel
        get() = OnlinePaymentViewModel(UseCaseModule.paymentUseCase)

    val paymentMethodViewModel
        get() = PaymentMethodViewModel(
            sessionRepository = DataModule.sessionRepository,
            settingsRepository = DataModule.settingsRepository,
            cardUseCase = UseCaseModule.cardUseCase
        )

}
