package gr.cardlink.payments.api

import java.io.Serializable

data class PaymentResponse(
    /**
     * Order id
     * */
    val orderId: String? = null,

    /**
     * Transaction id
     * */
    val transactionId: String? = null,

    /**
     * Common error codes
     * error_network: network error
     * error_invalid_input: when request values are invalid
     * error_remote_encryption: remote encryption script failure
     * error_payment_methods: no valid payment methods available
     * error_accepted_card_types: no valid card types available
     * */
    val errorCode: String? = null,

    /**
     * Transaction description
     * */
    val description: String? = null,

    /**
     * Transaction order amount
     * */
    val orderAmount: String? = null,

    /**
     * Transaction payment in total
     * */
    val paymentTotal: String? = null,

    /**
     * Transaction currency
     * */
    val currency: String? = null,

    /**
     * Transaction reference code
     * */
    val paymentReference: String? = null
) : Serializable