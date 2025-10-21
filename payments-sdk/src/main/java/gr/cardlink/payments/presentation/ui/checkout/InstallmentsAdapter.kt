package gr.cardlink.payments.presentation.ui.checkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import gr.cardlink.payments.R

internal class InstallmentsAdapter(
    context: Context,
    private val data: List<ViewInstallment>
) : ArrayAdapter<InstallmentsAdapter.ViewInstallment>(context, R.layout.list_item_installment, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val holder: ViewHolder

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.list_item_installment, parent, false)
            holder = ViewHolder()
            holder.countTextView = row.findViewById(R.id.installmentCountView)
            holder.analysisTextView = row.findViewById(R.id.installmentAnalysisView)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val instalment = data[position]

        holder.setInstalment(instalment)
        return row!!
    }

    private inner class ViewHolder {
        var countTextView: TextView? = null
        var analysisTextView: TextView? = null

        fun setInstalment(viewInstallment: ViewInstallment) {
            countTextView?.text = viewInstallment.countDescription
            analysisTextView?.text = viewInstallment.analysis
        }
    }

    internal class ViewInstallment(
        val count: Int,
        val countDescription: String,
        val analysis: String
    ) {
        override fun toString(): String {
            val separator  = if (analysis.isEmpty()) "" else " - "
            return "$countDescription$separator$analysis"
        }
    }

}