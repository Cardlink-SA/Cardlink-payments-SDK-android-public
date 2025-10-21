package gr.cardlink.payments.presentation.ui.card.quickadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import gr.cardlink.payments.databinding.FragmentQuickAddBinding
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks

internal class QuickAddFragment : BaseFragment<FragmentQuickAddBinding>() {

    private var hasNfcService = true

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQuickAddBinding {
        return FragmentQuickAddBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasNfcService = arguments?.getBoolean(KEY_HAS_NFC_SERVICE, true) ?: true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.nfcButtonView.isEnabled = hasNfcService
        if (hasNfcService) {
            setupListeners()
        }
    }

    private fun setupListeners() {
        setupCancelButtonListener()
        setupScanButtonListener()
        setupNfcButtonListener()
    }

    private fun setupCancelButtonListener() {
        binding.toolbarView.setOnClickListener {
            callbacks?.onDismiss()
        }
    }

    private fun setupScanButtonListener() {
        binding.scanButtonView.apply {
            setOnClickListener {
                (callbacks as? Callbacks)?.onScanButtonClicked()
            }
        }
    }

    private fun setupNfcButtonListener() {
        binding.nfcButtonView.setOnClickListener {
            (callbacks as? Callbacks)?.onNfcButtonClicked()
        }
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            toolbarView.setBackgroundColor(colorInt)
            backgroundView.changeLayerListPrimaryColor(colorInt)
            nfcButtonView.setColor(colorInt)
            scanButtonView.setColor(colorInt)
        }
    }

    companion object {
        private const val KEY_HAS_NFC_SERVICE = "key_has_nfc_service"
        fun newInstance(hasNfcService: Boolean): QuickAddFragment {
            return QuickAddFragment().apply {
                arguments = bundleOf(
                    KEY_HAS_NFC_SERVICE to hasNfcService
                )
            }
        }
    }

    interface Callbacks : FragmentCallbacks {
        fun onNfcButtonClicked()
        fun onScanButtonClicked()
    }

}