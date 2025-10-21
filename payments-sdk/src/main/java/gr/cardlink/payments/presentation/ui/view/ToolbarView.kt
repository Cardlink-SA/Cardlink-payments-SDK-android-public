package gr.cardlink.payments.presentation.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.ViewToolbarBinding

internal class ToolbarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        parseAttributes(attrs)
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let { _attrs ->
            val attributes = context.obtainStyledAttributes(_attrs, R.styleable.ToolbarView)
            try {
                attributes.getString(R.styleable.ToolbarView_android_text)?.also {
                    setTitle(it)
                }
            } finally {
                attributes.recycle()
            }
        }
    }

    private fun setTitle(title: String) {
        binding.titleTextView.text = title
    }

    fun setOnClickListener(callback: (() -> Unit)?) {
        binding.buttonView.apply {
            callback?.let { _callback ->
                visibility = View.VISIBLE
                setOnClickListener {
                    _callback()
                }
            } ?: run {
                visibility = View.INVISIBLE
            }
        }
    }

}