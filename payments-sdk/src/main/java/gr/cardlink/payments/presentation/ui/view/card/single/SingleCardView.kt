package gr.cardlink.payments.presentation.ui.view.card.single

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isInvisible
import gr.cardlink.payments.R
import gr.cardlink.payments.config.Config.PAN_LENGTH
import gr.cardlink.payments.databinding.ViewSingleCardBinding
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.presentation.extension.toCardStyle

internal class SingleCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = ViewSingleCardBinding.inflate(LayoutInflater.from(context), this)

    private val panViews by lazy {
        binding.layoutPan.run {
            listOf(
                panDigit1View, panDigit2View, panDigit3View, panDigit4View, panDigit5View,
                panDigit6View, panDigit7View, panDigit8View, panDigit9View, panDigit10View,
                panDigit11View, panDigit12View
            )
        }
    }

    private val expirationDateViews by lazy {
        binding.layoutExpirationDate.run {
            listOf(
                expirationMonthDigit1View, expirationMonthDigit2View,
                expirationYearDigit1View, expirationYearDigit2View
            )
        }
    }

    private val cvvViews by lazy {
        binding.run {
            listOf(
                cvvDigit1View, cvvDigit2View, cvvDigit3View, cvvDigit4View, cvvDigit5View
            )
        }
    }

    init {
        setupCameraDistance()
        setMode(ViewMode.STATIC)
    }

    private fun setupCameraDistance() {
        val distance = context.resources.displayMetrics.density * CAMERA_DISTANCE_FACTOR

        binding.run {
            frontContainer.cameraDistance = distance
            backContainer.cameraDistance = distance
        }
    }

    fun setMode(mode: ViewMode) {
        when (mode) {
            ViewMode.STATIC -> setupStaticMode()
            ViewMode.EDIT -> setupEditMode()
            ViewMode.PREFILL -> setupPrefillMode()
        }
    }

    private fun setupStaticMode() {
        panViews.onEach { it.visibility = VISIBLE }
        expirationDateViews.onEach { it.visibility = INVISIBLE }
        binding.run {
            layoutPan.run {
                visiblePanGroup.visibility = VISIBLE
                visiblePanTextView.visibility = GONE
            }
            layoutExpirationDate.run {
                placeholderDigit1View.visibility = VISIBLE
                placeholderDigit2View.visibility = VISIBLE
                placeholderDigit3View.visibility = VISIBLE
                placeholderDigit4View.visibility = VISIBLE
            }
        }
    }

    private fun setupEditMode() {
        panViews.onEach { it.visibility = INVISIBLE }
        expirationDateViews.onEach { it.visibility = VISIBLE }
        binding.run {
            layoutPan.run {
                visiblePanGroup.visibility = INVISIBLE
                visiblePanTextView.visibility = VISIBLE
            }
            layoutExpirationDate.run {
                placeholderDigit1View.visibility = INVISIBLE
                placeholderDigit2View.visibility = INVISIBLE
                placeholderDigit3View.visibility = INVISIBLE
                placeholderDigit4View.visibility = INVISIBLE
            }
        }
    }

    private fun setupPrefillMode() {
        panViews.onEach { it.visibility = VISIBLE }
        expirationDateViews.onEach { it.visibility = VISIBLE }
        binding.run {
            layoutPan.run {
                visiblePanGroup.visibility = INVISIBLE
                visiblePanTextView.visibility = VISIBLE
            }
            layoutExpirationDate.run {
                placeholderDigit1View.visibility = INVISIBLE
                placeholderDigit2View.visibility = INVISIBLE
                placeholderDigit3View.visibility = INVISIBLE
                placeholderDigit4View.visibility = INVISIBLE
            }
        }
    }

    fun setPanDigit(digit: Int, position: Int) {
        if (position in panViews.indices) {
            panViews[position].visibility = VISIBLE

        } else if (position in panViews.size until PAN_LENGTH) {
            binding.layoutPan.visiblePanTextView.apply {
                text?.let { _text ->
                    val textInternal = _text.toString() + digit
                    text = textInternal
                }
            }
        }
    }

    fun removePanDigit(position: Int) {
        if (position in panViews.indices) {
            panViews[position].visibility = INVISIBLE
        } else if (position in panViews.size until PAN_LENGTH) {
            binding.layoutPan.visiblePanTextView.apply {
                text?.let { _text ->
                    text = _text.dropLast(1)
                }
            }
        }
    }

    fun setExpirationDateDigit(digit: Int, position: Int) {
        if (position in expirationDateViews.indices) {
            expirationDateViews[position].text = digit.toString()
        }
        handleExpirationDateSeparatorVisibility()
    }

    private fun handleExpirationDateSeparatorVisibility() {
        val invisible = binding.layoutExpirationDate.expirationMonthDigit2View.text.isEmpty()
        binding.layoutExpirationDate.expirationDateSeparatorView.isInvisible = invisible
    }

    fun removeExpirationDateDigit(position: Int) {
        if (position in expirationDateViews.indices) {
            expirationDateViews[position].apply {
                text = text.dropLast(1)
                binding.layoutExpirationDate.expirationDateSeparatorView.isInvisible = text.length == 2
            }
        }
        handleExpirationDateSeparatorVisibility()
    }

    fun clearExpirationDate() {
        expirationDateViews.forEach {
            it.text = ""
        }
        binding.layoutExpirationDate.expirationDateSeparatorView.isInvisible = true
    }

    fun setCardholderName(cardholderName: String?) {
        binding.cardholderTextView.text = cardholderName
    }

    fun setCvvDigit(position: Int) {
        if (position in cvvViews.indices) {
            cvvViews[position].visibility = VISIBLE
        }
    }

    fun removeCvvDigit(position: Int) {
        if (position in cvvViews.indices) {
            cvvViews[position].visibility = INVISIBLE
        }
    }

    fun setType(cardType: Card.Type) {
        val cardStyle = cardType.toCardStyle()
        cardStyle?.let {
            setLogo(it.first)
            setBackground(it.second)
        } ?: run {
            setupUnknown()
        }
    }

    fun clearPan() {
        panViews.onEach { it.isInvisible = true }
        binding.layoutPan.visiblePanTextView.text = ""
    }

    private fun setupUnknown() {
        binding.run {
            frontLogoImageView.visibility = View.GONE
            backLogoImageView.visibility = View.GONE
            setBackground(R.drawable.background_card_unknown)
        }
    }

    private fun setBackground(@DrawableRes backgroundRes: Int) {
        binding.run {
            frontInnerContainer.setBackgroundResource(backgroundRes)
            backInnerContainer.setBackgroundResource(backgroundRes)
        }
    }

    private fun setLogo(@DrawableRes logoRes: Int) {
        binding.run {
            frontLogoImageView.apply {
                setImageResource(logoRes)
                visibility = VISIBLE
            }
            backLogoImageView.apply {
                setImageResource(logoRes)
                visibility = VISIBLE
            }
        }
    }

    fun flipCard(to: ViewType) {
        if (to == ViewType.FRONT) {
            sendBackInBackground()
        } else {
            bringBackInForeground()
        }
    }

    private fun sendBackInBackground() {
        val toForegroundAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in).apply {
                setTarget(binding.frontContainer)
                doOnStart {
                    binding.frontContainer.visibility = VISIBLE
                }
            }

        val toBackgroundAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_out).apply {
                setTarget(binding.backContainer)
                doOnEnd {
                    binding.backContainer.visibility = INVISIBLE
                }
            }

        startFlipAnimation(toBackgroundAnimator, toForegroundAnimator)
    }

    private fun startFlipAnimation(toForegroundAnimator: Animator, toBackgroundAnimator: Animator) {
        AnimatorSet().apply {
            playTogether(toForegroundAnimator, toBackgroundAnimator)
        }.also {
            it.start()
        }
    }

    private fun bringBackInForeground() {
        val toForegroundAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_in).apply {
                setTarget(binding.backContainer)
                doOnStart {
                    binding.backContainer.visibility = VISIBLE
                }
            }

        val toBackgroundAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out).apply {
                setTarget(binding.frontContainer)
                doOnEnd {
                    binding.frontContainer.visibility = INVISIBLE
                }
            }

        startFlipAnimation(toForegroundAnimator, toBackgroundAnimator)
    }

    fun prefillCard(visiblePan: String, cardholder: String, expirationDate: String, type: Card.Type) {
        setMode(ViewMode.PREFILL)
        prefillPan(visiblePan)
        setCardholderName(cardholder)
        prefillExpirationDate(expirationDate)
        setType(type)
    }

    private fun prefillPan(visiblePan: String) {
        if (visiblePan.length == 4) {
            binding.layoutPan.visiblePanTextView.text = visiblePan
        }
    }

    private fun prefillExpirationDate(expirationDate: String) {
        expirationDate.onEachIndexed { index, char ->
            val digit = char.digitToIntOrNull()
            digit?.let {
                setExpirationDateDigit(it, index)
            }
        }
    }

    enum class ViewType {
        FRONT,
        BACK
    }

    enum class ViewMode {
        STATIC,
        EDIT,
        PREFILL
    }

    companion object {
        private const val CAMERA_DISTANCE_FACTOR = 8000
    }
}