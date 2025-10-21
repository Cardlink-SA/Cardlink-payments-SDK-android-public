package gr.cardlink.payments.presentation.extension

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

internal fun Fragment.showSnackMessage(@StringRes messageRes: Int) {
    activity?.showSnackMessage(messageRes)
}