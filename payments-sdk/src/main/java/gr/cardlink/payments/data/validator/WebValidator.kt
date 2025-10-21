package gr.cardlink.payments.data.validator

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.model.CardValidation
import gr.cardlink.payments.domain.utils.CreditCardUtils
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

@SuppressLint("SetJavaScriptEnabled")
internal class WebValidator(context: Context) : Validator {

    private val cardTypeEmitter = PublishProcessor.create<Card.Type>()
    private val cardValidationEmitter = PublishProcessor.create<CardValidation>()
    private val gson = Gson()

    private val webView by lazy {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
        }
    }

    override fun onCartTypeEvent(jsScript: String, pan: String): String {
        webView.evaluateJavascript(jsScript) {
            webView.evaluateJavascript("getCardType('$pan');") {
                try {
                    val type = gson.fromJson(it, String::class.java)
                    cardTypeEmitter.onNext(CreditCardUtils.getCardType(type))
                } catch (ex: Exception) {
                    cardTypeEmitter.onError(ex)
                }
            }
        }
        return pan
    }

    override fun getCardType(): Flowable<Card.Type> = cardTypeEmitter

    override fun onEncryptCardEvent(jsScript: String, card: Card): String {
        webView.evaluateJavascript(jsScript) {
            val encryptScript = "encryptCardData('${card.pan}', '${card.expirationYear}', '${card.expirationMonth}', '${card.cvv}', '${card.cardholder}', true, false);"
            webView.evaluateJavascript(encryptScript) {
                try {
                    val validation = gson
                        .fromJson(it, CardValidation::class.java)
                        .copy(card = card)

                    cardValidationEmitter.onNext(validation)
                } catch (ex: Exception) {
                    cardValidationEmitter.onError(ex)
                }
            }
        }
        return card.pan
    }

    override fun getCardValidation(): Flowable<CardValidation> = cardValidationEmitter

    override fun clear() {
        webView.apply {
            clearHistory()
            clearCache(true)
            settings.javaScriptEnabled = false
        }
    }
}