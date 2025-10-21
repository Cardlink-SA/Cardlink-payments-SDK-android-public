package gr.cardlink.payments.presentation.ui.method

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import gr.cardlink.payments.databinding.FragmentPaymentMethodBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.model.ClientError
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import gr.cardlink.payments.presentation.extension.fadeInView
import gr.cardlink.payments.presentation.extension.setColor
import gr.cardlink.payments.presentation.extension.setProgressColor
import gr.cardlink.payments.presentation.extension.toCurrencyFromNumericCode
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy

internal class PaymentMethodFragment: BaseFragment<FragmentPaymentMethodBinding>() {

    private val viewModel = ViewModelModule.paymentMethodViewModel

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPaymentMethodBinding {
        return FragmentPaymentMethodBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        parseArguments()
    }

    private fun parseArguments() {
        arguments?.run {
            val cartTotal = getLong(ARG_CART_TOTAL)
            val currencyCode = getString(ARG_CURRENCY_CODE) ?: ""
            setupCartTotal(cartTotal, currencyCode)
            setupCloseButtonListener()
            observeState(cartTotal, currencyCode)
        } ?: callbacks?.onError(
            ClientError(
                type = ClientError.ErrorType.INVALID_INPUT.value,
                message ="Cart total and/or currency code are invalid"
            )
        )
    }

    private fun setupCartTotal(cartTotal: Long, currencyCode: String) {
        binding.cartTotalValueTextView.text = cartTotal.toDouble().toCurrencyFromNumericCode(currencyCode)
    }

    private fun setupCloseButtonListener() {
        binding.toolbarView.setOnClickListener {
            callbacks?.onDismiss()
        }
    }

    private fun setupCardListener(cartTotal: Long, currencyCode: String) {
        binding.cardContainerView.setOnClickListener {
            (callbacks as? Callbacks)?.onCardMethodClicked(
                paymentTotal = cartTotal,
                currencyCode = currencyCode,
                cardOnlyFlow = false
            )
        }
    }

    private fun setupIrisListener() {
        binding.irisContainerView.setOnClickListener {
            (callbacks as? Callbacks)?.onIrisMethodClicked()
        }
    }

    private fun setupPaypalListener() {
        binding.paypalContainerView.setOnClickListener {
            (callbacks as? Callbacks)?.onPaypalMethodClicked()
        }
    }

    private fun observeState(cartTotal: Long, currencyCode: String) {
        viewModel
            .getStateSingle(cartTotal)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { state ->
                    if (state.acceptedPaymentMethods.size == 1 && state.acceptedPaymentMethods.contains(Settings.AcceptedPaymentMethod.CARD)) {
                        (callbacks as? Callbacks)?.onCardMethodClicked(
                            paymentTotal = cartTotal,
                            currencyCode = currencyCode,
                            cardOnlyFlow = true
                        )
                    } else {
                        hideLoading()
                        handleAcceptedPaymentMethods(state.acceptedPaymentMethods, cartTotal, currencyCode)
                        handleAcceptedCardTypes(state.acceptedCardTypes)
                        handleAcquirerLogo(state.acquirerRes)
                    }
                },
                onError = {
                    hideLoading()
                    handleError(it.message, it)
                }
            ).addTo(disposables)
    }

    private fun handleAcceptedPaymentMethods(paymentsMethods: List<Settings.AcceptedPaymentMethod>, cartTotal: Long, currencyCode: String) {
        binding.run {
            // card payment method
            val hasCardPayment = paymentsMethods.find { it.value == Settings.AcceptedPaymentMethod.CARD.value } != null
            if (hasCardPayment) {
                setupCardListener(cartTotal, currencyCode)
                cardContainerView.fadeInView(FADE_IN_DURATION)
            }

            // iris payment method
            val hasIrisPayment = paymentsMethods.find { it.value == Settings.AcceptedPaymentMethod.IRIS.value } != null
            if (hasIrisPayment) {
                setupIrisListener()
                irisContainerView.fadeInView(FADE_IN_DURATION)
            }

            // paypal payment method
            val hasPaypalPayment = paymentsMethods.find { it.value == Settings.AcceptedPaymentMethod.PAYPAL.value } != null
            if (hasPaypalPayment) {
                setupPaypalListener()
                paypalContainerView.fadeInView(FADE_IN_DURATION)
            }
        }
    }

    private fun handleAcceptedCardTypes(cardTypes: List<Settings.AcceptedCardType>) {
        binding.run {
            cardTypes.forEach { type ->
                when (type) {
                    Settings.AcceptedCardType.VISA -> logoVisaImageView.isVisible = true
                    Settings.AcceptedCardType.MASTERCARD -> logoMasterCardImageView.isVisible = true
                    Settings.AcceptedCardType.MAESTRO -> logoMaestroImageView.isVisible = true
                    Settings.AcceptedCardType.DINERS -> logoDinersImageView.isVisible = true
                    Settings.AcceptedCardType.DISCOVER -> logoDiscoverImageView.isVisible = true
                    Settings.AcceptedCardType.AMEX -> logoAmexImageView.isVisible = true
                }
            }
        }
    }

    private fun handleError(errorTypeText: String?, throwable: Throwable? = null) {
        Log.e(TAG, errorTypeText, throwable)
        callbacks?.onError(ClientError(type = errorTypeText, message = throwable?.localizedMessage))
    }

    private fun handleAcquirerLogo(@DrawableRes logoRes: Int?) {
        binding.run {
            logosLayout.fadeInView(FADE_IN_DURATION)
            logosImageView.fadeInView(FADE_IN_DURATION)
            logoImageView.apply {
                logoRes?.let {
                    setImageResource(it)
                    fadeInView(FADE_IN_DURATION)
                }
            }
        }
    }

    private fun hideLoading() {
        binding.loaderView.isVisible = false
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            root.setBackgroundColor(colorInt)
            toolbarView.setBackgroundColor(colorInt)
            contentContainer.changeLayerListPrimaryColor(colorInt)
            loaderView.setProgressColor(colorInt)
            cardContentView.setColor(colorInt)
        }
    }

    companion object {
        private const val TAG = "PaymentMethodFragment"
        private const val ARG_CART_TOTAL = "arg_cart_total"
        private const val ARG_CURRENCY_CODE = "arg_currency_code"
        private const val FADE_IN_DURATION = 300L

        fun newInstance(cartTotal: Long, currencyCode: String?): PaymentMethodFragment {
            return PaymentMethodFragment().apply {
                arguments = bundleOf(
                    ARG_CART_TOTAL to cartTotal,
                    ARG_CURRENCY_CODE to currencyCode
                )
            }
        }
    }

    interface Callbacks: FragmentCallbacks {
        fun onCardMethodClicked(paymentTotal: Long, currencyCode: String, cardOnlyFlow: Boolean)
        fun onIrisMethodClicked()
        fun onPaypalMethodClicked()
    }
}