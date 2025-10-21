package gr.cardlink.payments.data.mapper

import gr.cardlink.payments.data.model.card.ApiCard
import gr.cardlink.payments.data.model.settings.ApiSettingsResponse
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.InstalmentsConfig
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.domain.utils.CreditCardUtils

internal fun ApiCard.toCard(): Card? {
    val expirationMonthInternal = expirationMonth?.toIntOrNull()
    val expirationYearInternal = expirationYear?.toIntOrNull()
    val typeInternal = CreditCardUtils.getCardType(type ?: "")

    return if (visiblePan == null || expirationMonthInternal == null || expirationYearInternal == null
        || typeInternal == Card.Type.UNKNOWN || token == null
    ) {
        null
    } else {
        Card(
            pan = visiblePan,
            expirationMonth = expirationMonthInternal,
            expirationYear = expirationYearInternal,
            cvv = null,
            cardholder = null,
            type = typeInternal,
            token = token
        )
    }
}

internal fun ApiSettingsResponse.toSettings(): Settings {
    return settings?.let { _settings ->
        Settings(
            currencyCode = _settings.currency,
            instalmentsConfig = getInstallmentsConfig(_settings),
            acceptedCardTypes = _settings.acceptedCardTypes?.mapNotNull { _type ->
                Settings.AcceptedCardType.values().find { it.value == _type }
            } ?: emptyList(),
            acceptedPaymentMethods = _settings.acceptedPaymentMethods?.mapNotNull { _method ->
                Settings.AcceptedPaymentMethod.values().find { it.value == _method }
            } ?: emptyList(),
            acquirer = Settings.Acquirer.values().firstOrNull { it.value == _settings.acquirer } ?: Settings.Acquirer.UNKNOWN
        )
    } ?: Settings(null, null, emptyList(), emptyList(), Settings.Acquirer.UNKNOWN)
}

private fun getInstallmentsConfig(apiSettings: ApiSettingsResponse.ApiSettings): InstalmentsConfig? {
    return if (apiSettings.installments == false || apiSettings.maxInstallments == null || apiSettings.currency == null) {
        null
    } else {
        InstalmentsConfig(
            maxInstallments = apiSettings.maxInstallments,
            variations = apiSettings.installmentsVariations?.mapNotNull { it.toInstalment() } ?: emptyList()
        )
    }
}

internal fun ApiSettingsResponse.ApiInstalmentVariation.toInstalment(): InstalmentsConfig.Variation? {
    return if (totalInstallments == null || installmentAmount == null) {
        null
    } else {
        InstalmentsConfig.Variation(
            amount = installmentAmount,
            total = totalInstallments
        )
    }

}