package gr.cardlink.payments.domain.model

internal data class InstalmentsConfig(
    val maxInstallments: Int,
    val variations: List<Variation>
) {
    data class Variation(
        val amount: Long,
        val total: Int
    )
}