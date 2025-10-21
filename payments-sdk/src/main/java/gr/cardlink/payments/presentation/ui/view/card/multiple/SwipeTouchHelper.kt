package gr.cardlink.payments.presentation.ui.view.card.multiple

import android.content.res.Resources
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import gr.cardlink.payments.R

internal class SwipeTouchHelper(
    resources: Resources,
    private val swipeable: Swipeable
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val scrollXLimit by lazy {
        resources.getDimensionPixelSize(R.dimen.card_row_menu_width)
    }

    private var currentScrollX = 0
    private var currentScrollXWhenInactive = 0
    private var initScrollXWhenInactive = 0f
    private var inactive = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = 0
        val swipeFlags = getSwipeFlags(viewHolder.adapterPosition)
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    private fun getSwipeFlags(position: Int): Int {
        return if (swipeable.shouldSwipe(position)) {
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        } else {
            0
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { /* no-op */ }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return Integer.MAX_VALUE.toFloat()
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Integer.MAX_VALUE.toFloat()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX == 0f) {
                currentScrollX = viewHolder.itemView.scrollX
                inactive = true
            }
            if (isCurrentlyActive) {
                handleCurrentlyActive(viewHolder.itemView, dX)
                swipeable.onSwipe(viewHolder.itemView)
            } else {
                handleInactive(viewHolder.itemView, dX)
            }
        }
    }

    private fun handleCurrentlyActive(itemView: View, dX: Float) {
        var scrollOffset = currentScrollX - dX.toInt()
        if (scrollOffset > scrollXLimit) {
            scrollOffset = scrollXLimit
        } else if (scrollOffset < 0) {
            scrollOffset = 0
        }
        itemView.scrollTo(scrollOffset, 0)
    }

    private fun handleInactive(itemView: View, dX: Float) {
        if (inactive) {
            inactive = false
            currentScrollXWhenInactive = itemView.scrollX
            initScrollXWhenInactive = dX
        }
        if (itemView.scrollX < scrollXLimit) {
            val scrollX = (currentScrollXWhenInactive * dX / initScrollXWhenInactive).toInt()
            itemView.scrollTo(scrollX, 0)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.apply {
            if (scrollX > scrollXLimit) {
                scrollTo(scrollXLimit, 0)
            } else if (scrollX < 0) {
                scrollTo(0, 0)
            }
        }
    }
}