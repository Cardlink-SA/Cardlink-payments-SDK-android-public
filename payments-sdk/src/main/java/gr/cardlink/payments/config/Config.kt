package gr.cardlink.payments.config

internal object Config {
    // card settings
    const val PAN_LENGTH = 16
    const val EXPIRATION_DATE_LENGTH = 4

    // remote settings
    const val OPTIONS_EXISTING_CARD = "110"
    const val OPTIONS_STORE_CARD = "100"

    // endpoints
    lateinit var BASE_URL: String
    val ERROR_URL
        get() = "${BASE_URL}payment/fail/"
    val SUCCESS_URL
        get() = "${BASE_URL}payment/success/"
    val IRIS_URL
        get() = "${BASE_URL}payment/iris/response/"
    val PAYPAL_URL
        get() = "${BASE_URL}payment/paypal/response/"
    val JS_URL
        get() = "${BASE_URL}get-js"
}