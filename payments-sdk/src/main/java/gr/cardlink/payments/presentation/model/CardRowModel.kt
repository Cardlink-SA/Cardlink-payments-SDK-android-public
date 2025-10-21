package gr.cardlink.payments.presentation.model

import androidx.annotation.DrawableRes

internal data class CardRowModel(
    val pan: String,
    val typeName: String,
    @DrawableRes val logoRes: Int?,
    val selected: Boolean
)