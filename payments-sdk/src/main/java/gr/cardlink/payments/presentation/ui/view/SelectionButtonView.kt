package gr.cardlink.payments.presentation.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.setPadding
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.ViewSelectionButtonBinding

internal class SelectionButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewSelectionButtonBinding.inflate(LayoutInflater.from(context), this)

    init {
        setupView()
        parseAttributes(attrs)
    }

    private fun setupView() {
        setPadding(resources.getDimensionPixelSize(R.dimen.spacing_4))
        clipToPadding = false
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let { _attrs ->
            val attributes = context.obtainStyledAttributes(_attrs, R.styleable.SelectionButtonView)
            try {
                attributes.getString(R.styleable.SelectionButtonView_android_text)?.also {
                    setTitle(it)
                }
                attributes.getResourceId(R.styleable.SelectionButtonView_android_src, -1).also {
                    if (it != -1) {
                        setImage(it)
                    }
                }
            } finally {
                attributes.recycle()
            }
        }
    }

    private fun setTitle(title: String) {
        binding.textView.text = title
    }

    private fun setImage(@DrawableRes imageRes: Int) {
        binding.imageView.setImageResource(imageRes)
    }

    fun setOnClickListener(callback: () -> Unit) {
        binding.buttonContainer.setOnClickListener {
            callback()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.buttonContainer.apply {
            isEnabled = enabled
            isClickable = enabled
        }
    }

    fun setColor(@ColorInt color: Int) {
        with(binding) {
            imageView.setColorFilter(color)
            textView.setTextColor(color)
        }
    }

}