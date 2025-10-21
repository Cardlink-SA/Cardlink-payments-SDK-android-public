package gr.cardlink.payments.presentation.ui.online

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import gr.cardlink.payments.R
import gr.cardlink.payments.api.PaymentResponse
import gr.cardlink.payments.databinding.FragmentOnlinePaymentBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.mapper.toPaymentResponse
import gr.cardlink.payments.domain.model.ClientError
import gr.cardlink.payments.domain.model.PaymentTransaction
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers

internal class OnlinePaymentFragment : BaseFragment<FragmentOnlinePaymentBinding>() {

    private val viewModel = ViewModelModule.onlinePaymentViewModel
    private var onlinePaymentService: OnlinePaymentService? = null

    private val webClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (url == onlinePaymentService?.responseUrl) {
                setWebViewVisibility(false)
                view?.evaluateJavascript(JS) { content ->
                    handlePaymentHtmlResponse(content)
                }
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            setWebViewVisibility(true)
            if (url == onlinePaymentService?.responseUrl) {
                setWebViewVisibility(false)
            }
        }
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnlinePaymentBinding {
        return FragmentOnlinePaymentBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseExtras()
    }

    private fun parseExtras() {
        onlinePaymentService = arguments?.getSerializable(KEY_ONLINE_PAYMENTS_SERVICE) as? OnlinePaymentService
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setupPaymentTitle()
        setupWebView()
        observePayment()
        observeTransactionStatus()
    }

    private fun setupPaymentTitle() {
        val titleRes = when (onlinePaymentService) {
            is OnlinePaymentService.Iris -> R.string.sdk_payment_iris
            is OnlinePaymentService.Paypal -> R.string.sdk_payment_paypal
            else -> R.string.sdk_payment_generic
        }
        binding.stateTextView.text = resources.getString(titleRes)
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
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
        }
    }

    private fun observePayment() {
        val service = onlinePaymentService ?: return

        viewModel
            .getPaymentObservable(service)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { response ->
                    if (response.isNullOrBlank()) {
                        handlePaymentError(
                            errorCode = ClientError.ErrorType.SERVER_ERROR.value,
                            errorMessage = null
                        )
                    } else {
                        renderHtmlPage(response)
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

    private fun updateUi(success: Boolean, response: PaymentResponse) {
        viewModel.clearSession()
        setWebViewVisibility(false)

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

    private fun hideLoading() {
        binding.loaderGroup.isVisible = false
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
                            errorCode = null,
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
        Log.e(TAG, "errorMessage: $errorMessage, description: ${transaction?.description}")
        updateUi(success = false, response = response)
    }

    private fun updatePaymentState(@StringRes stateRes: Int) {
        binding.stateTextView.setText(stateRes)
    }

    private fun renderHtmlPage(htmlPage: String) {
        binding.webView.loadDataWithBaseURL(null, htmlPage, "text/html", "UTF-8", null)
    }

    private fun handlePaymentHtmlResponse(htmlContent: String) {
        viewModel.parseHtmlContent(htmlContent)
    }

    private fun setWebViewVisibility(visible: Boolean) {
        binding.webView.isVisible = visible
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            toolbarView.setBackgroundColor(colorInt)
            contentContainer.changeLayerListPrimaryColor(colorInt)
            backButtonView.setColor(colorInt)
        }
    }

    companion object {
        private const val KEY_ONLINE_PAYMENTS_SERVICE = "key_online_payments_service"
        private const val TAG = "OnlinePayment"
        private const val JS = "(function() { return (document.body.firstChild.innerHTML); })();"

        fun newInstance(onlinePaymentService: OnlinePaymentService): OnlinePaymentFragment {
            return OnlinePaymentFragment().apply {
                arguments = bundleOf(KEY_ONLINE_PAYMENTS_SERVICE to onlinePaymentService)
            }
        }
    }

    interface Callbacks : FragmentCallbacks {
        fun onPaymentCompleted(success: Boolean, paymentResponse: PaymentResponse)
    }

}