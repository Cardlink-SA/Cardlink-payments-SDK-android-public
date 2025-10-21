package gr.cardlink.payments.api

import android.content.Context
import android.content.Intent
import gr.cardlink.payments.di.LibraryModule
import gr.cardlink.payments.presentation.ui.CardlinkPaymentsActivity

/**
 * Payment SDK with two public APIs.
 * The first one refers to SDK initialization which is mandatory, and the other is an optional
 * helper function which produces an [Intent] that launches payment flow.
 * */
object PaymentsSdk {

    internal const val KEY_PAYMENT_REQUEST = "key_payment_request"

    /**
     * [CardlinkPaymentsActivity] response key.
     * */
    const val KEY_PAYMENT_RESPONSE = "key_payment_response"

    /**
     * Initializes SDK. This is mandatory.
     * @param appContext application context.
     * */
    fun init(
        appContext: Context,
        baseUrl: String,
        headers: Map<String, String>? = null
    ) {
        with(LibraryModule) {
            initializeDi(appContext, baseUrl, headers)
        }
    }

    /**
     * Creates an intent that launches payment flow.
     * @param context context
     * @param paymentRequest [PaymentRequest]
     * @return SDK payment activity
     * */
    fun newIntent(
        context: Context,
        paymentRequest: PaymentRequest
    ): Intent {
        return Intent(context, CardlinkPaymentsActivity::class.java).apply {
            putExtra(KEY_PAYMENT_REQUEST, paymentRequest)
        }
    }

}