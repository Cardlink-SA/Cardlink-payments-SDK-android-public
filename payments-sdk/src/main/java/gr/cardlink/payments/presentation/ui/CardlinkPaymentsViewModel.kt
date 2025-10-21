package gr.cardlink.payments.presentation.ui

import android.annotation.SuppressLint
import gr.cardlink.payments.api.PaymentRequest
import gr.cardlink.payments.di.LibraryModule
import gr.cardlink.payments.domain.repository.SessionRepository
import gr.cardlink.payments.domain.usecase.PaymentUseCase
import gr.cardlink.payments.presentation.ui.base.BaseViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class CardlinkPaymentsViewModel(
    private val paymentUseCase: PaymentUseCase,
    private val sessionRepository: SessionRepository
) : BaseViewModel() {

    val colorRes: Int?
        get() = sessionRepository.get(SessionRepository.Key.COLOR_RES)

    init {
        LibraryModule.run {
            validateSdk()
            clear()
        }
        paymentUseCase.clearSession()
    }

    fun isPaymentRequestValid(paymentRequest: PaymentRequest?): Boolean {
        return paymentRequest?.let { _paymentInfo ->
            val rec = isRecurringValid(_paymentInfo.recurringFrequency, _paymentInfo.recurringEndDate)
            val isValid = isDescriptionValid(_paymentInfo.description)
                    && isAddressLineValid(_paymentInfo.addressLine) && isCityValid(_paymentInfo.city)
                    && isCurrencyCodeValid(_paymentInfo.currencyCode)
                    && isCountryCodeValid(_paymentInfo.countryCode)
                    && rec

            if (isValid) {
                cachePaymentInfo(_paymentInfo)
            }
            isValid
        } ?: false
    }

    private fun isDescriptionValid(description: String) = description.isNotBlank()

    private fun isAddressLineValid(addressLine: String) = addressLine.isNotBlank()

    private fun isCityValid(city: String) = city.isNotBlank()

    private fun isCurrencyCodeValid(currencyCode: String): Boolean {
        return Currency.getAvailableCurrencies().any { it.numericCode == currencyCode.toIntOrNull() }
    }

    private fun isCountryCodeValid(countryCode: String): Boolean {
        return countryCode.matches("\\d{3}".toRegex())
    }

    @SuppressLint("SimpleDateFormat")
    private fun isRecurringValid(recurringFrequency: Int?, recurringEndDate: String?): Boolean {
        return when {
            recurringFrequency == null && recurringEndDate.isNullOrBlank() -> true
            recurringFrequency == null -> false
            recurringEndDate.isNullOrBlank() || recurringFrequency !in 1 .. 30 -> false
            else -> {
                val dateFormat = SimpleDateFormat("yyyyMMdd").apply {
                    isLenient = false
                }
                try {
                    val date = dateFormat.parse(recurringEndDate)
                    val now = Date()
                    date != null && date.after(now)
                } catch (ex: ParseException) {
                    false
                }
            }
        }
    }

    private fun cachePaymentInfo(paymentRequest: PaymentRequest) {
        paymentUseCase.cachePaymentInfo(paymentRequest)
    }
}