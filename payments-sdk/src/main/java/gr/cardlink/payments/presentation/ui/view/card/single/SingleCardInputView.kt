package gr.cardlink.payments.presentation.ui.view.card.single

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.ViewSingleCardInputBinding
import gr.cardlink.payments.presentation.extension.setViewAndChildrenEnabled
import gr.cardlink.payments.presentation.model.SingleCardInputModel
import gr.cardlink.payments.presentation.ui.view.edittext.MaskedEditTextWrapper
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.utils.CreditCardUtils
import gr.cardlink.payments.presentation.extension.setHasError
import gr.cardlink.payments.presentation.extension.setMaxLength

internal class SingleCardInputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewSingleCardInputBinding.inflate(LayoutInflater.from(context), this, true)
    private var callbacks: Callbacks? = null
    private var type: Card.Type = Card.Type.UNKNOWN
    private var state: State = State.EMPTY

    fun setCallbacks(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    fun setupView(state: State, dataModel: SingleCardInputModel? = null) {
        this.state = state
        when (state) {
            State.EMPTY -> setupEmptyState()
            State.CVV_ONLY, State.CVV_CARDHOLDER -> setupPrefillState(dataModel)
            State.INPUT -> setupInputState()
        }
    }

    private fun setupEmptyState() {
        setViewAndChildrenEnabled(binding.root, false)
        binding.creditCardView.setMode(SingleCardView.ViewMode.STATIC)
        binding.clickableAreaView.apply {
            isVisible = true
            isEnabled = true
            setOnClickListener {
                callbacks?.onCardViewClicked()
            }
        }
    }

    private fun setupPrefillState(dataModel: SingleCardInputModel?) {
        setViewAndChildrenEnabled(binding.root, false)
        binding.run {
            if (dataModel?.cvv == null) {
                cvvEditText.isEnabled = true
                state = State.CVV_ONLY
            }
            if (dataModel?.cardholder == null) {
                cardholderEditText.isEnabled = true
                state = State.CVV_CARDHOLDER
            }
            clickableAreaView.isVisible = false
        }
        setupListeners()
        prefillViews(dataModel)
    }

    private fun prefillViews(dataModel: SingleCardInputModel?) {
        dataModel?.let { _dataModel ->
            this.type = _dataModel.type
            binding.run {
                if (_dataModel.pan == null || _dataModel.expirationDate == null) {
                    return
                }

                creditCardView.prefillCard(
                    visiblePan = CreditCardUtils.getVisiblePan(_dataModel.pan),
                    cardholder = _dataModel.cardholder ?: "",
                    expirationDate = _dataModel.expirationDate,
                    type = _dataModel.type
                )
                panEditText.setText(_dataModel.pan)
                expirationDateEditText.setText(_dataModel.expirationDate)
                cardholderEditText.setText(_dataModel.cardholder)
                setStepDescription(R.string.sdk_payment_proceed)
            }
        }
    }

    private fun setupInputState() {
        setupListeners()
        setViewAndChildrenEnabled(binding.root, true)
        binding.run {
            creditCardView.setMode(SingleCardView.ViewMode.EDIT)
            clickableAreaView.isVisible = false
        }
    }

    private fun setupListeners() {
        setupPanListener()
        setupExpirationDateListener()
        setupCvvListener()
        setupCardholderListener()
    }

    private fun setupPanListener() {
        binding.apply {
            panEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setStepDescription(R.string.sdk_card_step_card_number)
                }
            }
            panEditText.setCallbacks(
                object : MaskedEditTextWrapper.Callbacks {
                    override fun onDigitAdded(digit: Int, position: Int) {
                        creditCardView.setPanDigit(digit, position)
                        callbacks?.onAnyDigitAdded(getInputDataModel())
                    }

                    override fun onDigitRemoved(position: Int) {
                        creditCardView.removePanDigit(position)
                        callbacks?.onAnyDigitAdded(getInputDataModel())
                    }

                    override fun onCleared() {
                        creditCardView.clearPan()
                    }
                }
            )
        }
    }

    fun getInputDataModel(): SingleCardInputModel {
        return SingleCardInputModel(
            pan = binding.panEditText.getText(),
            expirationDate = binding.expirationDateEditText.getText(),
            cvv = binding.cvvEditText.text?.toString(),
            cardholder = binding.cardholderEditText.text?.toString(),
            type = type
        )
    }

    private fun setStepDescription(@StringRes descriptionRes: Int) {
        binding.stateTextView.setText(descriptionRes)
    }


    private fun setupExpirationDateListener() {
        binding.apply {
            expirationDateEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setStepDescription(R.string.sdk_card_step_expiration_date)
                }
            }
            expirationDateEditText.setCallbacks(
                object : MaskedEditTextWrapper.Callbacks {
                    override fun onDigitAdded(digit: Int, position: Int) {
                        creditCardView.setExpirationDateDigit(digit, position)
                        callbacks?.onAnyDigitAdded(getInputDataModel())
                    }

                    override fun onDigitRemoved(position: Int) {
                        creditCardView.removeExpirationDateDigit(position)
                        callbacks?.onAnyDigitAdded(getInputDataModel())
                    }

                    override fun onCleared() {
                        creditCardView.clearExpirationDate()
                        callbacks?.onAnyDigitAdded(getInputDataModel())
                    }
                }
            )
        }
    }

    private fun setupCvvListener() {
        binding.run {
            cvvEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setStepDescription(R.string.sdk_card_step_cvv)
                    creditCardView.flipCard(SingleCardView.ViewType.BACK)
                } else if (state != State.CVV_ONLY) {
                    creditCardView.flipCard(SingleCardView.ViewType.FRONT)
                }
            }
            cvvEditText.doOnTextChanged { text, start, before, _ ->
                text?.let {
                    callbacks?.onAnyDigitAdded(getInputDataModel())
                    if (before == 0) {
                        creditCardView.setCvvDigit(start)
                    } else {
                        creditCardView.removeCvvDigit(start)
                    }
                }
            }
        }
    }

    private fun setupCardholderListener() {
        binding.run {
            cardholderEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setStepDescription(R.string.sdk_card_step_cardholder)
                    callbacks?.onCardholderFocus()
                }
            }
            cardholderEditText.doAfterTextChanged {
                it?.toString()?.let { _text ->
                    creditCardView.setCardholderName(_text)
                    callbacks?.onAnyDigitAdded(getInputDataModel())
                }
            }
        }
    }

    fun handleValidationError(validation: CardValidation) {
        binding.run {
            stateTextView.text = resources.getString(R.string.sdk_card_step_valid_card)
            panEditText.setHasError(validation.errorPan != null)
            expirationDateEditText.setHasError(validation.errorExpirationMonth != null || validation.errorExpirationYear != null)
            cvvEditText.setHasError(validation.errorCvv != null)
            cardholderEditText.setHasError(validation.errorCardholder != null)
        }
    }

    fun clearErrors() {
        binding.run {
            panEditText.setHasError(false)
            expirationDateEditText.setHasError(false)
            cvvEditText.setHasError(false)
            cardholderEditText.setHasError(false)
        }
    }

    fun setCardType(type: Card.Type) {
        this.type = type
        binding.run {
            creditCardView.setType(type)
            cvvEditText.setMaxLength(type.cvvLength)
        }
    }

    fun setLoading(loading: Boolean) {
        when (state) {
            State.CVV_ONLY, State.CVV_CARDHOLDER -> handleCvvCardHolderState(loading, state)
            State.INPUT -> setViewAndChildrenEnabled(binding.root, !loading)
            State.EMPTY -> { /* no-op */ }
        }
    }

    private fun handleCvvCardHolderState(loading: Boolean, state: State) {
        setViewAndChildrenEnabled(binding.root, !loading)
        if (!loading) {
            binding.cvvEditText.isEnabled = true
            if (state == State.CVV_CARDHOLDER) {
                binding.cardholderEditText.isEnabled = true
            }
        }
    }

    interface Callbacks {
        fun onAnyDigitAdded(dataModel: SingleCardInputModel) { /* default no-op */ }
        fun onCardViewClicked() { /* default no-op */ }
        fun onCardholderFocus() { /* default no-op */ }
    }

    enum class State {
        EMPTY,
        CVV_ONLY,
        INPUT,
        CVV_CARDHOLDER
    }

}