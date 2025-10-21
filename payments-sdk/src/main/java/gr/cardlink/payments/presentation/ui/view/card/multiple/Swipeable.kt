package gr.cardlink.payments.presentation.ui.view.card.multiple

import android.view.View

internal interface Swipeable {
    fun onSwipe(itemView: View)
    fun shouldSwipe(position: Int): Boolean
}