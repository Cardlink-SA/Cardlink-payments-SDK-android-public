package gr.cardlink.payments.domain.model

internal data class Installment(
    val currencyCode: String,
    val count: Int,
    val installmentAmount: Double
)