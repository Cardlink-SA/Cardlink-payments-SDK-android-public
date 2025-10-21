package gr.cardlink.payments.presentation.ui.checkout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.FragmentCheckoutBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.model.ClientError
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import gr.cardlink.payments.presentation.extension.fadeInView
import gr.cardlink.payments.presentation.extension.setProgressColor
import gr.cardlink.payments.presentation.extension.toCurrencyFromCode
import gr.cardlink.payments.presentation.extension.toCurrencyFromNumericCode
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks
import gr.cardlink.payments.presentation.ui.view.card.multiple.MultipleCardInputView
import gr.cardlink.payments.presentation.ui.view.card.single.SingleCardInputView
import gr.cardlink.payments.presentation.model.MultipleCardInputModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class CheckoutFragment: BaseFragment<FragmentCheckoutBinding>() {

    private val viewModel = ViewModelModule.checkoutViewModel
    private var cartTotal: Long? = null
    private var currencyNumericCode: String? = null

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCheckoutBinding {
        return FragmentCheckoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        parseArguments()
        setupLogo()
        setupListeners()
        setupObservers()
    }

    private fun parseArguments() {
        val error = ClientError(
            type = ClientError.ErrorType.INVALID_INPUT.value,
            message = "Cart total and/or currency numeric code are invalid"
        )

        arguments?.run {
            cartTotal = getLong(ARG_CART_TOTAL)
            currencyNumericCode = getString(ARG_CURRENCY_NUMERIC_CODE)

            if (cartTotal == null || currencyNumericCode == null) {
                callbacks?.onError(error)
            } else {
                setupCartTotal(cartTotal, currencyNumericCode)
            }

            setupCartTotal(cartTotal, currencyNumericCode)
        } ?: callbacks?.onError(error)
    }

    private fun setupCartTotal(cartTotal: Long?, currencyCode: String?) {
        binding.cartTotalValueTextView.text = cartTotal?.toDouble()?.toCurrencyFromNumericCode(currencyCode)
    }

    private fun setupLogo() {
        viewModel.acquirerRes?.let {
            binding.logoImageView.setImageResource(it)
        }
    }

    private fun setupListeners() {
        setupCloseButtonListener()
        setupSingleCardViewListener()
        setupMultipleCardsViewListener()
        setupQuickAddButtonListener()
    }

    private fun setupCloseButtonListener() {
        binding.toolbarView.setOnClickListener {
            callbacks?.onDismiss()
        }
    }

    private fun setupSingleCardViewListener() {
        binding.singleCardInputView.setCallbacks(object : SingleCardInputView.Callbacks {
            override fun onCardViewClicked() {
                (callbacks as? Callbacks)?.onAddCardClicked()
            }
        })
    }

    private fun setupMultipleCardsViewListener() {
        binding.multipleCardView.setCallbacks(object : MultipleCardInputView.Callbacks {
            override fun onMultipleCardsAddCardClicked() {
                (callbacks as? Callbacks)?.onAddCardClicked()
            }

            override fun onMultipleCardsCardSelected(selectedCard: MultipleCardInputModel) {
                viewModel.selectCard(selectedCard)
            }

            override fun onMultipleCardsCardDeleted(token: String?, pan: String) {
                viewModel.deleteCard(token, pan)
            }

            override fun onMultipleCardsEmptied() {
                handleNoUserCards()
            }
        })
    }

    private fun setupQuickAddButtonListener() {
        binding.cardQuickAddButton.setOnClickListener {
            (callbacks as? Callbacks)?.onQuickAddClicked()
        }
    }

    private fun setupObservers() {
        observeUserCards()
        observeSelectedCard()
        observeInstallments()
    }

    private fun hideLoading() {
        binding.loaderView.isVisible = false
    }

    private fun observeUserCards() {
        val delay = resources.getInteger(R.integer.screen_slide_animation_duration) / 2L
        viewModel
            .getStoredCardsSingle()
            .delay(delay, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate { hideLoading() }
            .subscribeBy(
                onSuccess = {
                   if (it.isEmpty()) {
                       handleNoUserCards()
                   } else {
                       handleMultipleUserCards(it)
                   }
                },
                onError = {
                    handleUserCardsError(it)
                }
            )
            .addTo(disposables)
    }

    private fun handleNoUserCards() {
        binding.run {
            singleCardInputView.apply {
                fadeInView(duration = CARDS_ANIMATION_DURATION)
                setupView(SingleCardInputView.State.EMPTY)
            }
            multipleCardView.isVisible = false
        }
    }

    private fun handleMultipleUserCards(dataModel: List<MultipleCardInputModel>) {
        binding.run {
            singleCardInputView.isVisible = false
            multipleCardView.apply {
                fadeInView(duration = CARDS_ANIMATION_DURATION)
                setDataModel(dataModel)
            }
        }
    }

    private fun handleUserCardsError(throwable: Throwable) {
        val error = if (throwable is IOException) {
            ClientError(
                type = ClientError.ErrorType.NETWORK.value,
                message = throwable.localizedMessage
            )
        }  else {
            ClientError(
                type = ClientError.ErrorType.NETWORK.value,
                message = "Error while fetching user stores cards"
            )
        }
        callbacks?.onError(error)
    }

    private fun observeSelectedCard() {
        viewModel
            .getSelectedCardFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { (callbacks as? Callbacks)?.onCardSelected(it) },
                onError = { Log.e(TAG, "Could not get selected card", it) }
            )
            .addTo(disposables)
    }

    private fun observeInstallments() {
        val cartTotal = cartTotal ?: return
        
        viewModel
            .getInstallmentsSingle(cartTotal)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { binding.installmentsContainerView.isVisible = false }
            .subscribeBy(
                onSuccess = { installments ->
                    if (installments.isEmpty()) {
                        binding.installmentsContainerView.isVisible = false
                        return@subscribeBy
                    }

                    installments.map {
                        getViewInstallment(
                            amount = it.installmentAmount,
                            total = it.count,
                            currencyCode = it.currencyCode
                        )
                    }.also {
                       setupInstallmentsView(it)
                    }
                },
                onError = {
                    binding.installmentsContainerView.isVisible = false
                }
            )
            .addTo(disposables)
    }

    private fun setupInstallmentsView(viewInstallments: List<InstallmentsAdapter.ViewInstallment>) {
        val context = context ?: return

        binding.run {
            installmentsDropdownView.apply {
                setAdapter(InstallmentsAdapter(context, viewInstallments))
                setOnItemClickListener { parent, _, position, _ ->
                    val selectedInstallments = (parent.getItemAtPosition(position) as? InstallmentsAdapter.ViewInstallment)?.count
                    selectedInstallments?.let { _installments ->
                        viewModel.cacheInstallments(_installments)
                    }
                }
                setOnDismissListener {
                    post { clearFocus() }
                }
            }
            installmentsContainerView.isVisible = true
        }
    }

    private fun getViewInstallment(amount: Double, total: Int, currencyCode: String): InstallmentsAdapter.ViewInstallment {
        return if (total == 1) {
           val withoutInstallments = resources.getString(R.string.sdk_installments_without)
           InstallmentsAdapter.ViewInstallment(total, withoutInstallments, "")
        } else {
            val span = resources.getString(R.string.sdk_instalments_span)
            val countFormatted = resources.getQuantityString(R.plurals.sdk_instalments_total, total, total)
            val analysis = "${amount.toCurrencyFromCode(currencyCode)}/$span"
            InstallmentsAdapter.ViewInstallment(total, countFormatted, analysis)
        }
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            root.setBackgroundColor(colorInt)
            toolbarView.setBackgroundColor(colorInt)
            backgroundView.changeLayerListPrimaryColor(colorInt)
            loaderView.setProgressColor(colorInt)
            cardQuickAddButton.setColorFilter(colorInt)
        }
    }

    companion object {
        private const val TAG = "CheckoutFragment"
        private const val ARG_CART_TOTAL = "arg_cart_total"
        private const val ARG_CURRENCY_NUMERIC_CODE = "arg_currency_code"
        private const val CARDS_ANIMATION_DURATION: Long = 800

        fun newInstance(cartTotal: Long, currencyNumericCode: String?): CheckoutFragment {
            return CheckoutFragment().apply {
                arguments = bundleOf(
                    ARG_CART_TOTAL to cartTotal,
                    ARG_CURRENCY_NUMERIC_CODE to currencyNumericCode
                )
            }
        }
    }

    interface Callbacks : FragmentCallbacks {
        fun onAddCardClicked()
        fun onQuickAddClicked()
        fun onCardSelected(card: Card)
    }

}