package gr.cardlink.payments.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import gr.cardlink.payments.R
import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.api.PaymentResponse
import gr.cardlink.payments.api.PaymentsSdk.KEY_PAYMENT_REQUEST
import gr.cardlink.payments.api.PaymentsSdk.KEY_PAYMENT_RESPONSE
import gr.cardlink.payments.databinding.ActivityPaymentBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.model.*
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.presentation.ui.card.add.AddCardFragment
import gr.cardlink.payments.presentation.ui.card.nfc.NfcFragment
import gr.cardlink.payments.presentation.ui.card.quickadd.QuickAddFragment
import gr.cardlink.payments.presentation.ui.checkout.CheckoutFragment
import gr.cardlink.payments.presentation.ui.payment.PaymentFragment
import gr.cardlink.payments.presentation.extension.*
import gr.cardlink.payments.presentation.extension.hasRearCamera
import gr.cardlink.payments.presentation.extension.showSnackMessage
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentFragment
import gr.cardlink.payments.presentation.ui.method.PaymentMethodFragment
import gr.cardlink.payments.presentation.ui.online.OnlinePaymentService

internal class CardlinkPaymentsActivity : AppCompatActivity(),
    CheckoutFragment.Callbacks,
    AddCardFragment.Callbacks,
    QuickAddFragment.Callbacks,
    NfcFragment.Callbacks,
    PaymentFragment.Callbacks,
    PaymentMethodFragment.Callbacks,
    OnlinePaymentFragment.Callbacks {

    private lateinit var binding: ActivityPaymentBinding
    private val viewModel = ViewModelModule.cardlinkPaymentsViewModel

    private var backState = BackState.DEFAULT
    private var paymentRequest: PaymentRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideKeyboard()
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        parseExtras()
        checkColor()
    }

    private fun parseExtras() {
        intent?.extras?.let { _extras ->
            paymentRequest = _extras.getSerializable(KEY_PAYMENT_REQUEST) as? PaymentRequest
            validatePaymentInput(paymentRequest)
        }
    }

    private fun validatePaymentInput(paymentRequest: PaymentRequest?) {
        if (paymentRequest != null && viewModel.isPaymentRequestValid(paymentRequest)) {
            goToPaymentMethod(paymentRequest.paymentTotalCents, paymentRequest.currencyCode)
        } else {
            finishWithError()
        }
    }

    private fun finishWithError() {
        onError(
            ClientError(
                type = ClientError.ErrorType.INVALID_INPUT.value,
                message = "Input values should not be null or empty"
            )
        )
    }

    private fun checkColor() {
        try {
            viewModel.colorRes?.let {
                val color = ContextCompat.getColor(this, it)
                binding.fragmentContainerView.setBackgroundColor(color)
                window.statusBarColor = color
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Could not update app color and status bar", ex)
        }

    }

    private fun showFragment(fragment: Fragment, withAnimation: Boolean = true, addToBackStack: Boolean = true, tag: String) {
        supportFragmentManager.beginTransaction().apply {
            if (withAnimation) {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            }
            setReorderingAllowed(true)
            replace(R.id.fragmentContainerView, fragment, tag)
            if (addToBackStack) {
                addToBackStack(tag)
            }
        }.also {
            it.commit()
        }
    }

    override fun onDismiss() {
        onBackPressed()
    }

    override fun onError(error: ClientError) {
        Log.e(TAG, "type: ${error.type}, message: ${error.message}")
        val response = PaymentResponse(
            errorCode = error.type,
            description = error.message
        )
        onPaymentCompleted(
            success = false,
            paymentResponse = response
        )
    }

    override fun onAddCardClicked() {
        goToAddCard()
    }

    override fun onCardAdded(card: Card) {
        goToPayment(card = card, existingCardFlow = false)
    }

    private fun goToPayment(card: Card, existingCardFlow: Boolean) {
        backState = BackState.PAYMENT
        supportFragmentManager.popBackStack(TAG_FRAGMENT_CHECKOUT, 1)
        PaymentFragment.newInstance(card, existingCardFlow).also {
            showFragment(fragment = it, tag = TAG_FRAGMENT_PAYMENT)
        }
    }

    private fun goToPaymentMethod(paymentTotal: Long, currencyCode: String) {
        backState = BackState.DEFAULT
        PaymentMethodFragment.newInstance(paymentTotal, currencyCode).also {
            showFragment(
                fragment = it,
                withAnimation = false,
                addToBackStack = false,
                tag = TAG_FRAGMENT_METHOD
            )
        }
    }

    private fun goToCheckout(paymentTotal: Long, currencyCode: String, backState: BackState = BackState.DEFAULT) {
        this.backState = backState
        CheckoutFragment.newInstance(paymentTotal, currencyCode).also {
            showFragment(
                fragment = it,
                withAnimation = true,
                addToBackStack = true,
                tag = TAG_FRAGMENT_CHECKOUT
            )
        }
    }

    private fun goToAddCard(card: Card? = null) {
        backState = BackState.DEFAULT
        AddCardFragment.newInstance(card).also {
            showFragment(fragment = it, tag = TAG_FRAGMENT_ADD_CARD)
        }
    }

    override fun onCardMethodClicked(paymentTotal: Long, currencyCode: String, cardOnlyFlow: Boolean) {
        val backState = if (cardOnlyFlow) BackState.SINGLE_CARD_FLOW else BackState.DEFAULT
        goToCheckout(paymentTotal, currencyCode, backState)
    }

    override fun onIrisMethodClicked() {
        OnlinePaymentFragment.newInstance(OnlinePaymentService.Iris()).also {
            showFragment(fragment = it, tag = TAG_FRAGMENT_ONLINE)
        }
    }

    override fun onPaypalMethodClicked() {
        OnlinePaymentFragment.newInstance(OnlinePaymentService.Paypal()).also {
            showFragment(fragment = it, tag = TAG_FRAGMENT_ONLINE)
        }
    }

    override fun onQuickAddClicked() {
        backState = BackState.DEFAULT
        QuickAddFragment.newInstance(hasNfc()).also {
            showFragment(fragment = it, tag = TAG_FRAGMENT_QUICK_ADD)
        }
    }

    override fun onCardSelected(card: Card) {
        goToPayment(card = card, existingCardFlow = true)
    }

    override fun onNfcButtonClicked() {
        if (hasNfc()) {
            if (isNfcEnabled()) {
                NfcFragment.newInstance().also {
                    showFragment(fragment = it, tag = TAG_FRAGMENT_NFC)
                }
            } else {
                showSnackMessage(R.string.sdk_quick_add_tap_error_nfc_disabled)
            }
        } else {
            showSnackMessage(R.string.sdk_quick_add_tap_error_no_nfc)
        }
    }

    override fun onCardQuickAdd(card: Card) {
        supportFragmentManager.popBackStack(TAG_FRAGMENT_QUICK_ADD, 0)
        AddCardFragment.newInstance(selectedCard = card).also {
            showFragment(fragment = it, tag = TAG_FRAGMENT_ADD_CARD)
        }
    }

    override fun onScanButtonClicked() {
        if (hasRearCamera()) {
            showSnackMessage(R.string.sdk_feature_not_available_yet)
        } else {
            showSnackMessage(R.string.sdk_quick_add_scan_error_no_camera)
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        if (backState == BackState.SINGLE_CARD_FLOW) {
            onError(ClientError(type = ClientError.ErrorType.CANCELLED.value, message = null))
        } else if (backState == BackState.DEFAULT) {
            super.onBackPressed()
        }
    }

    override fun onPaymentCompleted(success: Boolean, paymentResponse: PaymentResponse) {
        val resultCode = if (success) RESULT_OK else RESULT_CANCELED
        val intent = Intent().apply {
            putExtra(KEY_PAYMENT_RESPONSE, paymentResponse)
        }
        setResult(resultCode, intent)
        finish()
    }

    companion object {
        private const val TAG = "CheckoutActivity"
        private const val TAG_FRAGMENT_METHOD = "tab_fragment_method"
        private const val TAG_FRAGMENT_CHECKOUT = "tag_fragment_checkout"
        private const val TAG_FRAGMENT_ADD_CARD = "tag_fragment_add_card"
        private const val TAG_FRAGMENT_QUICK_ADD = "tag_fragment_quick_add"
        private const val TAG_FRAGMENT_NFC = "tag_fragment_nfc"
        private const val TAG_FRAGMENT_PAYMENT = "tag_fragment_payment"
        private const val TAG_FRAGMENT_ONLINE = "tag_fragment_online"
    }

    enum class BackState {
        PAYMENT,
        DEFAULT,
        SINGLE_CARD_FLOW
    }

}