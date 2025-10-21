package gr.cardlink.payments.presentation.ui.view.edittext

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.widget.FrameLayout
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import com.vicmikhailau.maskededittext.MaskedEditText
import gr.cardlink.payments.R
import gr.cardlink.payments.presentation.extension.removeWhiteSpaces
import gr.cardlink.payments.presentation.extension.setOnSelectionChangedListener

internal class MaskedEditTextWrapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var currentText: String = ""
    private var maskedEditText: MaskedEditText? = null
    private var callbacks: Callbacks? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.edit_text_masked, this)
        setPadding(resources.getDimensionPixelSize(R.dimen.spacing_16))
        parseAttributes(attrs)
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let { _attrs ->
            maskedEditText = MaskedEditText(context, _attrs).also {
                val attributes =
                    context.obtainStyledAttributes(_attrs, R.styleable.MaskedEditTextWrapper)
                it.nextFocusDownId = try {
                    attributes.getResourceIdOrThrow(R.styleable.MaskedEditTextWrapper_maskedEditTextNextId)
                } catch (ex: Exception) {
                    NO_ID
                } finally {
                    attributes.recycle()
                }
            }.apply {
                setupListeners(this)
                isLongClickable = false
                setTextIsSelectable(false)
            }.also {
                addView(it)
            }
        }
    }

    private fun setupListeners(maskedEditText: MaskedEditText) {
        setupOnClickListener(maskedEditText)
        setupOnKeyListener(maskedEditText)
        setupOnTextChangedListener(maskedEditText)
    }

    private fun setupOnTextChangedListener(maskedEditText: MaskedEditText) {
        maskedEditText.apply {
            addTextChangedListener {
                it?.toString()?.let { _text ->
                    handleTextChanged(_text)
                }
                it?.setOnSelectionChangedListener { _, _, _, _, _ ->
                    postOnAnimation {
                        setSelection(text?.length ?: 0)
                    }
                }
            }
        }
    }

    private fun setupOnClickListener(maskedEditText: MaskedEditText) {
        maskedEditText.apply {
            setOnClickListener {
                setSelection(text?.length ?: 0)
            }
        }
    }

    private fun setupOnKeyListener(maskedEditText: MaskedEditText) {
        maskedEditText.apply {
            setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KEYCODE_DPAD_LEFT || keyCode == KEYCODE_DPAD_UP) {
                    setSelection(text?.length ?: 0)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun handleTextChanged(text: String) {
        // filters multiple text events of the same value from maskedEditText
        if (text.filter { !it.isWhitespace() } == currentText) {
            return
        }

        // maskedEditText text removes any character that exceeds mask's length
        maskedEditText?.maskString?.let { _maskString ->
            if (text.length > _maskString.length) {
                return
            }
        }

        val textWithoutWhiteSpaces = text.removeWhiteSpaces()

        if (textWithoutWhiteSpaces.length < currentText.length) {
            handleDigitRemoval(textWithoutWhiteSpaces)
        } else {
            handleDigitAddition(textWithoutWhiteSpaces)
        }
        currentText = textWithoutWhiteSpaces
    }

    private fun handleDigitRemoval(text: String) {
        if (text.isEmpty()) {
            callbacks?.onCleared()
            return
        }
        val digit = currentText.removeRange(text.indices).toIntOrNull()
        digit?.let {
            val index = currentText.lastIndex
            callbacks?.onDigitRemoved(index)
        }
    }

    private fun handleDigitAddition(text: String) {
        val index = text.lastIndex
        val digit = text[index].digitToIntOrNull()
        digit?.let { _digit ->
            callbacks?.onDigitAdded(_digit, index)

        }
    }

    fun setCallbacks(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    fun getText() = maskedEditText?.unMaskedText

    fun getMaskedEditText() = maskedEditText

    fun setText(text: String?) {
        maskedEditText?.setText(text)
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        maskedEditText?.onFocusChangeListener = l
    }

    interface Callbacks {
        fun onDigitAdded(digit: Int, position: Int)
        fun onDigitRemoved(position: Int)
        fun onCleared() { /* default no-op */ }
    }

}