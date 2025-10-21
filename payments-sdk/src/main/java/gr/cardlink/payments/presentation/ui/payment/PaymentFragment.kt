package gr.cardlink.payments.presentation.ui.payment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebSettings.LOAD_NO_CACHE
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import gr.cardlink.payments.R
import gr.cardlink.payments.config.Config
import gr.cardlink.payments.databinding.FragmentPaymentBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.mapper.toPaymentResponse
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.ClientError
import gr.cardlink.payments.api.PaymentResponse
import gr.cardlink.payments.domain.model.PaymentTransaction
import gr.cardlink.payments.domain.utils.CreditCardUtils
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import gr.cardlink.payments.presentation.extension.fadeInView
import gr.cardlink.payments.presentation.extension.fadeOutView
import gr.cardlink.payments.presentation.extension.setColor
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

internal class PaymentFragment : BaseFragment<FragmentPaymentBinding>() {
    private val viewModel = ViewModelModule.paymentViewModel
    private var card: Card? = null
    private var existingCardFlow: Boolean? = null
    private var contentIsRead: Boolean = false

    private val webClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (url == Config.SUCCESS_URL || url == Config.ERROR_URL) {
                readContent(view, 0)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "onPageStarted() > $url")
            if (url == Config.SUCCESS_URL || url == Config.ERROR_URL) {
                readContent(view, READ_DELAY)
            }
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            handleInterceptedUrl(request?.url?.toString())
            return super.shouldInterceptRequest(view, request)
        }
    }

    private fun handleInterceptedUrl(url: String?) {
        if (url == Config.SUCCESS_URL || url == Config.ERROR_URL) {
            setWebViewVisibility(false)
        }
    }

    private fun readContent(view: WebView?, delay: Long = 0) {
        if (contentIsRead) return
        Handler(Looper.getMainLooper()).postDelayed({
            view?.evaluateJavascript(JS) { htmlContent ->
                contentIsRead = true
                handlePaymentHtmlResponse(htmlContent)
            }
        }, delay)
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentBinding {
        return FragmentPaymentBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    private fun parseArguments() {
        arguments?.run {
            card = getSerializable(ARG_VALIDATED_CARD) as? Card
            existingCardFlow = getBoolean(ARG_EXISTING_CARD_FLOW)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        if (card != null && existingCardFlow != null) {
            setupPaymentState(existingCardFlow!!)
            setupWebView()
            prefillCard(card!!)
            observePayment(card!!, existingCardFlow!!)
            observeTransactionStatus()
        } else {
            handlePaymentError(
                errorCode = ClientError.ErrorType.INVALID_INPUT.value,
                errorMessage = "Input data should not be null"
            )
        }
    }

    private fun setupPaymentState(existingCardFlow: Boolean) {
        val stateRes = if (existingCardFlow) {
            R.string.sdk_payment_proceed
        } else {
            R.string.sdk_payment_card_info_stored
        }
        updatePaymentState(stateRes)
    }

    private fun updatePaymentState(@StringRes stateRes: Int) {
        binding.stateTextView.setText(stateRes)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = webClient
            settings.apply {
                javaScriptEnabled = true
                setSupportMultipleWindows(false)
                setSupportZoom(false)
                isHapticFeedbackEnabled = false
                displayZoomControls = false
                cacheMode = LOAD_NO_CACHE
            }
        }
    }

    private fun prefillCard(card: Card) {
        binding.singleCardInputView.apply {
            prefillCard(
                visiblePan = CreditCardUtils.getVisiblePan(card.pan),
                cardholder = card.cardholder ?: "",
                expirationDate = CreditCardUtils.getExpirationDate(card.expirationMonth, card.expirationYear),
                type = card.type
            )
        }
    }

    private fun observePayment(card: Card, existingCardFlow: Boolean) {
        viewModel
            .makePayment(card, existingCardFlow)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { htmlPage -> renderHtmlPage(htmlPage) },
                onError = { handlePaymentError(errorCode = ClientError.ErrorType.REMOTE_ENCRYPTION.value, errorMessage = it.localizedMessage) }
            )
            .addTo(disposables)
    }

    private fun hideLoading() {
        binding.loaderGroup.isVisible = false
    }

    private fun renderHtmlPage(htmlPage: String) {
        binding.webView.loadDataWithBaseURL(null, htmlPage, "text/html", "UTF-8", null)
        setWebViewVisibility(true)
    }

    private fun setWebViewVisibility(visible: Boolean) {
        binding.webView.run {
            post {
                if (visible) {
                    fadeInView()
                } else {
                    fadeOutView()
                }
            }
        }
    }

    private fun handlePaymentHtmlResponse(htmlContent: String) {
        viewModel.parseHtmlContent(htmlContent)
    }

    private fun observeTransactionStatus() {
        viewModel
            .getTransactionSingle()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    if (it.isTransactionSuccessful) {
                        val response = it.toPaymentResponse().copy(description = null)
                        updateUi(response = response, success = true)
                    } else {
                        handlePaymentError(
                            transaction = it,
                            errorCode = it.errorCode,
                            errorMessage = null
                        )
                    }
                },
                onError = {
                    handlePaymentError(
                        errorCode = ClientError.ErrorType.SERVER_ERROR.value,
                        errorMessage = it.localizedMessage
                    )
                }
            ).addTo(disposables)
    }

    private fun handlePaymentError(errorCode: String?, errorMessage: String?, transaction: PaymentTransaction? = null) {
        val response = transaction?.toPaymentResponse() ?: PaymentResponse(
            errorCode = errorCode,
            description = errorMessage
        )
        Log.e(TAG, "errorCode: $errorCode, errorMessage: ${errorMessage ?: transaction?.description}")
        updateUi(success = false, response = response)
    }

    private fun updateUi(success: Boolean, response: PaymentResponse) {
        // clear payment session and web view content
        viewModel.clearSession()

        binding.run {
            root.post {
                // hide loader
                hideLoading()

                // resources based on transaction result
                val (stateRes, animationRes) = if (success) {
                    R.string.sdk_payment_successful to R.raw.success
                } else {
                    R.string.sdk_payment_unsuccessful to R.raw.error
                }

                // update UI
                updatePaymentState(stateRes)
                binding.run {
                    completedView.apply {
                        isVisible = true
                        setAnimation(animationRes)
                        playAnimation()
                    }
                    backButtonView.apply {
                        isVisible = true
                        setOnClickListener {
                            (callbacks as? Callbacks)?.onPaymentCompleted(success = success, paymentResponse = response)
                        }
                    }
                }
            }
        }
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            root.setBackgroundColor(colorInt)
            toolbarView.setBackgroundColor(colorInt)
            contentContainer.changeLayerListPrimaryColor(colorInt)
            backButtonView.setColor(colorInt)
            loaderView.setColor(colorInt)
            loaderLock.setColorFilter(colorInt)
        }
    }

    companion object {
        private const val TAG = "PaymentFragment"
        private const val ARG_VALIDATED_CARD = "arg_validated_card"
        private const val ARG_EXISTING_CARD_FLOW = "arg_existing_card_flow"
        private const val JS = "(function() { return (document.body.firstChild.innerHTML); })();"
        private const val READ_DELAY = 1500L

        fun newInstance(card: Card, existingCardFlow: Boolean): PaymentFragment {
            return PaymentFragment().apply {
                arguments = bundleOf(
                    ARG_VALIDATED_CARD to card,
                    ARG_EXISTING_CARD_FLOW to existingCardFlow
                )
            }
        }
    }

    interface Callbacks : FragmentCallbacks {
        fun onPaymentCompleted(success: Boolean, paymentResponse: PaymentResponse)
    }

}