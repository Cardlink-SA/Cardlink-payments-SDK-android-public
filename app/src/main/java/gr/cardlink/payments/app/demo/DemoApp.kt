package gr.cardlink.payments.app.demo

import android.app.Application
import gr.cardlink.payments.api.PaymentsSdk

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PaymentsSdk.init(
            appContext = this,
            baseUrl = "https://in-app-payments.codesign-dev.com/wp-json/app-payments/"
        )
    }

}
