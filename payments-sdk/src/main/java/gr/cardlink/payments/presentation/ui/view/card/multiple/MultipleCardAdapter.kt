package gr.cardlink.payments.presentation.ui.view.card.multiple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import gr.cardlink.payments.databinding.ListItemCardRowBinding
import gr.cardlink.payments.presentation.model.CardRowModel
import gr.cardlink.payments.domain.utils.CreditCardUtils

internal class MultipleCardAdapter(
    private val callbacks: Callbacks
) : RecyclerView.Adapter<MultipleCardAdapter.ViewHolder>(), Swipeable {

    private val creditCardList = mutableListOf<CardRowModel>()
    private var selectedItemView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(creditCardList[position])
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        // keep it in case API support it in the future
        // @also MultipleCardInputView#onCardRowRemoved() and MultipleCardInputView#setDataModel()
        // val selected = payloads.firstOrNull() as? Boolean
        holder.bind(creditCardList[position], position == 0)
    }

    override fun getItemCount() = creditCardList.size

    override fun getItemId(position: Int) = creditCardList[position].pan.toLong()

    fun setCreditCards(cards: List<CardRowModel>) {
        creditCardList.apply {
            clear()
            addAll(cards)
            notifyItemRangeChanged(0, itemCount)
        }
    }

    inner class ViewHolder(private val binding: ListItemCardRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: CardRowModel, selected: Boolean? = null) {
            // reset swipe
            resetSwipe()

            // set data model
            val modelInternal = if (selected != null) {
                model.copy(selected = selected)
            } else {
                model
            }

            updateDataModel(modelInternal)
            setDataModel(modelInternal)

            // click listeners
            setupOnClickListener()
            setupOnDeleteListener()
        }

        private fun resetSwipe() {
            binding.root.apply {
                if (scrollX != 0) {
                    scrollTo(0, 0)
                }
            }
        }

        private fun updateDataModel(model: CardRowModel) {
            val index = creditCardList.indexOfFirst { it.pan == model.pan }
            if (index >= 0) {
                creditCardList[index] = model
            }
        }

        private fun setDataModel(model: CardRowModel) {
            binding.run {
                visiblePanTextView.text = CreditCardUtils.getVisiblePan(model.pan)
                model.logoRes?.let {
                    logoImageView.setImageResource(it)
                }
                selectedImageView.visibility = if (model.selected) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                cardTextView.text = model.typeName
            }
        }

        private fun setupOnClickListener() {
            binding.root.apply {
                setOnClickListener {
                    val position = adapterPosition
                    val selectedCardPosition = getSelectedCardPosition()
                    if (selectedCardPosition != position) {
                        notifyItemChanged(position, true)
                        notifyItemChanged(selectedCardPosition, false)
                    }
                    callbacks.onCardRowClicked(position)

                    // reset view position
                    resetSwipe()
                }
            }
        }

        private fun getSelectedCardPosition() = creditCardList.indexOfFirst { it.selected }

        private fun setupOnDeleteListener() {
            binding.deleteView.setOnClickListener {
                val position = adapterPosition
                creditCardList.removeAt(position)
                notifyItemRemoved(position)
                callbacks.onCardRowRemoved(position)

                // reset swipe for next item that will use this view holder
                resetSwipe()
            }
        }
    }

    override fun onSwipe(itemView: View) {
        if (itemView != selectedItemView ) {
            selectedItemView?.scrollTo(0, 0)
            selectedItemView = itemView
        }
    }

    override fun shouldSwipe(position: Int) = true //!creditCardList[position].selected

    interface Callbacks {
        fun onCardRowClicked(position: Int)
        fun onCardRowRemoved(position: Int)
    }
}