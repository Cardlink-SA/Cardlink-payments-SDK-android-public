package gr.cardlink.payments.presentation.ui.base

import gr.cardlink.payments.domain.model.ClientError

internal interface FragmentCallbacks {
    fun onDismiss()
    fun onError(error: ClientError)
}