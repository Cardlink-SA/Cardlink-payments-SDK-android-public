package gr.cardlink.payments.presentation.ui.view

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.ViewMainButtonBinding

internal class MainButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = ViewMainButtonBinding.inflate(LayoutInflater.from(context), this)

    init {
        parseAttributes(attrs)
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let { _attrs ->
            val attributes = context.obtainStyledAttributes(_attrs, R.styleable.MainButtonView)
            try {
                attributes.getString(R.styleable.MainButtonView_android_text)?.also {
                    binding.button.text = it
                }
                attributes.getBoolean(R.styleable.MainButtonView_android_enabled, true).also {
                    binding.button.isEnabled = it
                }
            } finally {
                attributes.recycle()
            }
        }
    }

    fun setOnClickListener(listener: () -> Unit) {
        binding.button.setOnClickListener {
            listener()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.button.isEnabled = enabled
    }

    fun setColor(@ColorInt colorInt: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            binding.button.background.setColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP)
        } else {
            binding.button.background.colorFilter = BlendModeColorFilter(colorInt, BlendMode.SRC_ATOP)
        }
    }

    fun setLoading(loading: Boolean) {
        binding.run {
            if (loading) {
                button.isEnabled = false
                button.textScaleX = 0f
                loader.isVisible = true
            } else {
                button.isEnabled = true
                button.textScaleX = 1f
                loader.isVisible = false
            }
        }
    }

}