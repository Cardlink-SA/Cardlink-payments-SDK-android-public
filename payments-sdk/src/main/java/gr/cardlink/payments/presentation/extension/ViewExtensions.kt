package gr.cardlink.payments.presentation.extension

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import gr.cardlink.payments.R
import gr.cardlink.payments.presentation.ui.view.edittext.MaskedEditTextWrapper
import gr.cardlink.payments.presentation.ui.view.edittext.SelectionSpanWatcher

internal fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {

            val child = view.getChildAt(i)
            setViewAndChildrenEnabled(child, enabled)
        }
    }
}

internal fun Editable.setOnSelectionChangedListener(listener: SelectionSpanWatcher.OnChangeSelectionListener?) {
    getSpans(0, length, SelectionSpanWatcher::class.java)
        .forEach { span -> removeSpan(span) }

    if (listener != null) {
        setSpan(SelectionSpanWatcher(listener), 0, length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }
}

internal fun EditText.setHasError(hasError: Boolean) = setHasErrorInternal(hasError)

internal fun MaskedEditTextWrapper.setHasError(hasError: Boolean) {
    setHasErrorInternal(hasError)
    getMaskedEditText()?.setHasErrorInternal(hasError)
}

private fun View.setHasErrorInternal(hasError: Boolean) {
    val colorRes = if (hasError) {
        R.color.card_edit_text_error
    } else {
        R.color.card_edit_text
    }
    backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
}

internal fun View.changeLayerListPrimaryColor(@ColorInt colorInt: Int) {
    try {
        val layerDrawable = background as? LayerDrawable
        val gradientDrawable = layerDrawable?.findDrawableByLayerId(R.id.backgroundDrawable) as? GradientDrawable
        gradientDrawable?.setColor(colorInt)
    } catch (ex: Exception) {
        Log.e(null, "Could not change layer-list primary color from view")
    }
}

internal fun ProgressBar.setProgressColor(@ColorInt colorInt: Int) {
    try {
        indeterminateTintList = ColorStateList.valueOf(colorInt)
    } catch (ex: Exception) {
        Log.e(null, "Could not change progress bar color")
    }
}

internal fun AppCompatEditText.setMaxLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

internal fun AppCompatTextView.setColor(@ColorInt color: Int) {
    setTextColor(color)
    compoundDrawables.filterNotNull().firstOrNull()?.setTint(color)
}

internal fun CircularProgressIndicator.setColor(@ColorInt color: Int) {
    trackColor = Color.WHITE
    setIndicatorColor(color)
}