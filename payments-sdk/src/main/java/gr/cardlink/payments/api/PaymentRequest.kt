package gr.cardlink.payments.api

import java.io.Serializable

/**
 * Required parameters for payment transactions.
 * */
data class PaymentRequest(

    /**
     * Total amount in the smallest currency unit (eg. cents)
     * */
    val paymentTotalCents: Long,

    /**
     * Currency code (eg. EUR), or number (eg. 971)
     * */
    val currencyCode: String,

    /**
     * Purchase description
     * */
    val description: String,

    /**
     * Address line
     * */
    val addressLine: String,

    /**
     * City
     * */
    val city: String,

    /**
     * Country code (eg. GR) or number (eg. 370)
     * */
    val countryCode: String,

    /**
     * Zip/postal code
     * */
    val postalCode: String,

    /**
     * Indicates the frequency of recurring payments and defines the minimum number of days
     * between two subsequent payments.
     * 28 is special value indicating that transactions are to be initiated on monthly basis.
     * Max value = 30
     * */
    val recurringFrequency: Int? = null,

    /**
     * Recurring end date with format YYYYMMDD. Mandatory if [recurringFrequency] is present.
     * */
    val recurringEndDate: String? = null

) : Serializable