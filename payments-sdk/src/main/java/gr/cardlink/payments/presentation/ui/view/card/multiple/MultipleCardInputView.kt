package gr.cardlink.payments.presentation.ui.view.card.multiple

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import gr.cardlink.payments.R
import gr.cardlink.payments.databinding.ViewMultipleCardBinding
import gr.cardlink.payments.presentation.extension.toCardRowModel
import gr.cardlink.payments.presentation.model.MultipleCardInputModel
import gr.cardlink.payments.domain.utils.CreditCardUtils

internal class MultipleCardInputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = ViewMultipleCardBinding.inflate(LayoutInflater.from(context), this)
    private var cardList = mutableListOf<MultipleCardInputModel>()
    private var callbacks: Callbacks? = null

    private val cardAdapter by lazy {
        MultipleCardAdapter(object :
            MultipleCardAdapter.Callbacks {
            override fun onCardRowClicked(position: Int) {
                if (position !in cardList.indices) return

                selectCard(position)
                callbacks?.onMultipleCardsCardSelected(cardList[position])
            }

            override fun onCardRowRemoved(position: Int) {
                if (position !in cardList.indices) return

                cardList[position].run {
                    callbacks?.onMultipleCardsCardDeleted(token, pan)
                }

                // remove card from local cache
                cardList.removeAt(position)

                // early exit if there aren't any cards
                if (cardList.isEmpty()) {
                    callbacks?.onMultipleCardsEmptied()
                    return
                }

                // update state text view
                setupStateTextView(cardList.size)

                // select 1st card till API support this feature
                // @also this#setDataModel()
                if (position == 0 && cardList.isNotEmpty()) {
                    setDataModel(cardList.toList())
                }
            }
        })
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(SwipeTouchHelper(context.resources, cardAdapter))
    }

    init {
        setupRecyclerView()
        setupAddCardListener()
    }

    private fun setupRecyclerView() {
        binding.cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cardAdapter
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun setupAddCardListener() {
        binding.addCardButton.setOnClickListener {
            callbacks?.onMultipleCardsAddCardClicked()
        }
    }

    fun setDataModel(cardList: List<MultipleCardInputModel>) {
        if (cardList.isEmpty()) {
            return
        }
        this.cardList.apply {
            clear()
            addAll(cardList)
        }

        // Keep it in case API support it in the future
        // and always select the first card
        // @also MultipleCardAdapter#onBindViewHolder()

        // val selectedIndex = cardList.indexOfFirst { it.selected }
        //        if (selectedIndex >= 0) {
        //            selectCard(selectedIndex)
        //        }
        selectCard(0)

        setupStateTextView(cardList.size)
        setupCardRowViews(cardList.toList())
    }

    private fun setupStateTextView(totalCards: Int) {
        binding.stateTextView.text = resources.getQuantityString(
            R.plurals.sdk_select_card_total,
            totalCards,
            totalCards
        )
    }

    private fun selectCard(position: Int) {
        if (position in cardList.indices) {
            val card = cardList[position]
            binding.selectedCardView.prefillCard(
                visiblePan = CreditCardUtils.getVisiblePan(card.pan),
                expirationDate = card.expirationDate,
                cardholder = card.cardholder,
                type = card.type
            )
        }
    }

    private fun setupCardRowViews(cardList: List<MultipleCardInputModel>) {
         cardList.map { it.toCardRowModel(it.selected) }.also {
             cardAdapter.setCreditCards(it)
         }
    }

    fun setCallbacks(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    interface Callbacks {
        fun onMultipleCardsAddCardClicked()
        fun onMultipleCardsCardSelected(selectedCard: MultipleCardInputModel)
        fun onMultipleCardsCardDeleted(token: String?, pan: String)
        fun onMultipleCardsEmptied()
    }

}