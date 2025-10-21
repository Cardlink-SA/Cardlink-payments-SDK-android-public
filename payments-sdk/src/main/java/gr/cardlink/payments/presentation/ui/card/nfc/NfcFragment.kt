package gr.cardlink.payments.presentation.ui.card.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.FragmentNfcBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import gr.cardlink.payments.presentation.extension.setColor
import gr.cardlink.payments.presentation.extension.showSnackMessage
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.reactivestreams.Subscription

internal class NfcFragment : BaseFragment<FragmentNfcBinding>(), NfcAdapter.ReaderCallback {

    private val viewModel = ViewModelModule.nfcViewModel

    private var timerSubscription: Subscription? = null

    private val nfcFlags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NFC_BARCODE or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

    private var nfcAdapter: NfcAdapter? = null

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNfcBinding {
        return FragmentNfcBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        setupCancelButtonListener()
    }

    private fun setupObservers() {
        observeTimer()
        observeCardReadSuccess()
        observeCardUnreadable()
        observeNfcErrors()
    }

    private fun observeTimer() {
        viewModel
            .getCounterFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                clearTimerSubscription()
                timerSubscription = it
                resetErrorViews()
                showTimerViews()
                enableNfcReader()
            }
            .map { it.toString() }
            .subscribeBy(
                onNext = { _tick ->
                    binding.timerTextView.text = _tick
                },
                onError = {
                    Log.e(TAG, "NFC counter error. Returning to quick add menu", it)
                    clearResources()
                    callbacks?.onDismiss()
                },
                onComplete = {
                    clearResources()
                    viewModel.onTimerFinished()
                }
            ).addTo(disposables)
    }

    private fun setupCancelButtonListener() {
        binding.toolbarView.setOnClickListener {
            callbacks?.onDismiss()
        }
    }

    private fun enableNfcReader() {
        val context = activity ?: return

        // Work around for some broken Nfc firmware implementations that poll the card too fast
        val extras = bundleOf(
            NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY to READER_READ_DELAY
        )

        nfcAdapter = NfcAdapter.getDefaultAdapter(context).apply {
            enableReaderMode(context, this@NfcFragment, nfcFlags, extras)
        }
    }

    private fun disableNfcReader() {
        activity?.let {
            nfcAdapter?.disableReaderMode(it)
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        viewModel.onNfcTagDiscovered(tag)
    }

    private fun observeCardUnreadable() {
        viewModel
            .getCardUnreadableFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext { clearTimerSubscription() }
            .subscribeBy(
                onNext = { handleEmvCardUnreadable(it) },
                onError = { Log.e(TAG, "NFC card read error", it) }
            )
            .addTo(disposables)
    }

    private fun handleEmvCardUnreadable(@StringRes messageRes: Int) {
        hideTimerViews()
        resetErrorViews()
        showErrorTextView(messageRes)
        setupRetryButton()
    }

    private fun showErrorTextView(@StringRes messageRes: Int) {
        binding.errorTextView.apply {
            setText(messageRes)
            isVisible = true
        }
    }

    private fun resetErrorViews() {
        binding.run {
            errorTextView.isVisible = false
            retryButton.isVisible = false
        }
    }

    private fun setupRetryButton() {
        binding.retryButton.apply {
            isVisible = true
            setOnClickListener {
                observeTimer()
            }
        }
    }

    private fun showTimerViews() {
        binding.run {
            loaderView.isVisible = true
            timerTextView.apply {
                text = ""
                isVisible = true
            }
        }
    }

    private fun hideTimerViews() {
        binding.run {
            timerTextView.apply {
                text = ""
                isVisible = false
            }
            loaderView.isVisible = false
        }
    }

    private fun observeCardReadSuccess() {
        viewModel
            .getCardReadSuccessFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { clearTimerSubscription() }
            .subscribeBy(
                onSuccess = {
                    hideTimerViews()
                    resetErrorViews()
                    (callbacks as? Callbacks)?.onCardQuickAdd(it)
                },
                onError = {
                    resetErrorViews()
                    hideTimerViews()
                    handleEmvCardUnreadable(R.string.sdk_quick_add_tap_error_could_not_add_card)
                    Log.e(TAG, "Card success read unrecoverable error")
                }
            )
            .addTo(disposables)
    }

    private fun observeNfcErrors() {
        viewModel
            .getNfcErrorFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { showSnackMessage(it) },
                onError = {
                    Log.e(TAG, "Unrecoverable NFC error")
                    callbacks?.onDismiss()
                }
            )
            .addTo(disposables)
    }

    override fun onDestroy() {
        clearResources()
        super.onDestroy()
    }

    private fun clearResources() {
        disableNfcReader()
        clearTimerSubscription()
    }

    private fun clearTimerSubscription() {
        timerSubscription?.cancel()
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            toolbarView.setBackgroundColor(colorInt)
            contentContainer.changeLayerListPrimaryColor(colorInt)
            retryButton.setColor(colorInt)
            timerTextView.setTextColor(colorInt)
            loaderView.setColor(colorInt)
            imageView.setColorFilter(colorInt)
        }
    }

    companion object {
        private const val TAG = "NfcFragment"
        private const val READER_READ_DELAY = 250

        fun newInstance() = NfcFragment()
    }

    interface Callbacks : FragmentCallbacks {
        fun onCardQuickAdd(card: Card)
    }

}