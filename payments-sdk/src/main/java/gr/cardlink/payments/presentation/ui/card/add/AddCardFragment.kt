package gr.cardlink.payments.presentation.ui.card.add

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import gr.cardlink.payments.databinding.FragmentAddCardBinding
import gr.cardlink.payments.di.ViewModelModule
import gr.cardlink.payments.domain.mapper.toCard
import gr.cardlink.payments.presentation.extension.toSingleCardInputModel
import gr.cardlink.payments.presentation.model.SingleCardInputModel
import gr.cardlink.payments.presentation.ui.base.BaseFragment
import gr.cardlink.payments.presentation.ui.base.FragmentCallbacks
import gr.cardlink.payments.presentation.ui.view.card.single.SingleCardInputView
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.model.ClientError
import gr.cardlink.payments.presentation.extension.changeLayerListPrimaryColor
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy

internal class AddCardFragment : BaseFragment<FragmentAddCardBinding>() {

    private val viewModel = ViewModelModule.addCardViewModel

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAddCardBinding {
        return FragmentAddCardBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setupCancelButtonListener()
        setupSingleCardView()
        setupProceedButtonListener()
        parseArguments()
    }

    private fun prefillInputView(card: Card) {
        binding.singleCardInputView.setupView(
            state = SingleCardInputView.State.CVV_ONLY,
            dataModel = card.toSingleCardInputModel()
        )
    }

    private fun setupCancelButtonListener() {
        binding.toolbarView.setOnClickListener {
            callbacks?.onDismiss()
        }
    }

    private fun setupSingleCardView() {
        binding.singleCardInputView.apply {
            setupView(SingleCardInputView.State.INPUT)
            setCallbacks(object :
                SingleCardInputView.Callbacks {
                override fun onAnyDigitAdded(dataModel: SingleCardInputModel) {
                    viewModel.validateCard(dataModel)
                }

                override fun onCardholderFocus() {
                    binding.nestedScrollView.apply {
                        postDelayed(
                            { smoothScrollTo(0, binding.root.bottom) }, SCROLL_DELAY)
                    }
                }
            })
        }
    }

    private fun setupProceedButtonListener() {
        binding.run {
            proceedButton.setOnClickListener {
                setLoading(true)
                singleCardInputView.clearErrors()
                val dataModel = singleCardInputView.getInputDataModel()
                val shouldStoreCard = binding.storeCardCheckBox.isChecked
                viewModel.onCardEvent(dataModel, shouldStoreCard)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.run {
            proceedButton.setLoading(loading)
            singleCardInputView.setLoading(loading)
        }
    }

    private fun parseArguments() {
        arguments?.run {
            val card = getSerializable(ARG_SELECTED_CARD) as? Card
            card?.let {
                prefillInputView(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupObservers()
    }

    private fun setupObservers() {
        observeProceedButtonState()
        observeCreditCardType()
        observeCardValidation()
    }

    private fun observeProceedButtonState() {
        viewModel
            .getProceedCardButtonObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    binding.proceedButton.isEnabled = it
                },
                onError = {
                    binding.proceedButton.isEnabled = false
                },
            )
            .addTo(disposables)
    }

    private fun observeCreditCardType() {
        viewModel
            .getCreditCardTypeObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { binding.singleCardInputView.setCardType(it) },
                onError = {
                    val error = ClientError(
                        type = ClientError.ErrorType.ENCRYPTION_SCRIPT.value,
                        message = it.message
                    )
                    callbacks?.onError(error)
                }
            )
            .addTo(disposables)
    }

    private fun observeCardValidation() {
        viewModel
            .getCreditCardValidationObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    handleValidation(it)
                },
                onError = {
                    setLoading(false)
                    val error = ClientError(
                        type = ClientError.ErrorType.ENCRYPTION_SCRIPT.value,
                        message = it.message
                    )
                    callbacks?.onError(error)
                }
            )
            .addTo(disposables)
    }

    private fun handleValidation(validation: CardValidation) {
        val validatedCard = validation.toCard()
        validatedCard?.let { _validatedCard ->
            (callbacks as? Callbacks)?.onCardAdded(_validatedCard)
        } ?: run {
            setLoading(false)
            binding.singleCardInputView.apply {
                setLoading(false)
                handleValidationError(validation)
            }
        }
    }

    override fun updateColors(colorInt: Int) {
        with(binding) {
            toolbarView.setBackgroundColor(colorInt)
            contentContainer.changeLayerListPrimaryColor(colorInt)
            proceedButton.setColor(colorInt)
            storeCardCheckBox.buttonTintList = ColorStateList.valueOf(colorInt)
        }
    }

    interface Callbacks : FragmentCallbacks {
        fun onCardAdded(card: Card)
    }

    companion object {
        private const val ARG_SELECTED_CARD = "arg_selected_card"
        private const val SCROLL_DELAY: Long = 100

        fun newInstance(selectedCard: Card? = null): AddCardFragment {
            return AddCardFragment().apply {
                arguments = bundleOf(
                    ARG_SELECTED_CARD to selectedCard,
                )
            }
        }
    }

}