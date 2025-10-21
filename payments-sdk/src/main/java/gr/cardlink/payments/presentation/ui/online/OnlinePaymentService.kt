package gr.cardlink.payments.presentation.ui.online

import gr.cardlink.payments.config.Config
import java.io.Serializable

internal sealed class OnlinePaymentService(
    open val responseUrl: String
) :
    Serializable {
    class Iris(responseUrl: String = Config.IRIS_URL) : OnlinePaymentService(responseUrl)

    class Paypal(responseUrl: String = Config.PAYPAL_URL) : OnlinePaymentService(responseUrl)

}