package gr.cardlink.payments.presentation.ui.view.edittext

import android.text.Selection.SELECTION_END
import android.text.Selection.SELECTION_START
import android.text.SpanWatcher
import android.text.Spannable

internal class SelectionSpanWatcher(
    private val listener: OnChangeSelectionListener
) : SpanWatcher {

    override fun onSpanAdded(text: Spannable?, what: Any?, start: Int, end: Int) { /* no-op */ }

    override fun onSpanRemoved(text: Spannable?, what: Any?, start: Int, end: Int) { /* no-op */ }

    override fun onSpanChanged(text: Spannable?, what: Any?, oStart: Int, oEnd: Int, nStart: Int, nEnd: Int) {
        when (what) {
            SELECTION_START -> listener.onSelectionChanged(Selection.START, oStart, oEnd, nStart, nEnd)
            SELECTION_END -> listener.onSelectionChanged(Selection.END, oStart, oEnd, nStart, nEnd)
        }
    }

    enum class Selection {
        START,
        END
    }

    fun interface OnChangeSelectionListener {
        fun onSelectionChanged(selection: Selection, oStart: Int, oEnd: Int, nStart: Int, nEnd: Int)
    }
}