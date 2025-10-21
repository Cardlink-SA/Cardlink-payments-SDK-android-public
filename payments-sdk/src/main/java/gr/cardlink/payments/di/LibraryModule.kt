package gr.cardlink.payments.di

import android.content.Context
import android.util.Log
import android.webkit.WebView
import gr.cardlink.payments.config.Config
import gr.cardlink.payments.domain.repository.SessionRepository
import gr.cardlink.payments.presentation.extension.toColorRes
import gr.cardlink.payments.presentation.extension.toDrawableRes
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.plugins.RxJavaPlugins

internal object LibraryModule {

    private const val TAG = "LibraryModule"

    private var disposable: Disposable? = null

    @Volatile
    lateinit var appContext: Context

    fun initializeDi(_appContext: Context, baseUrl: String, headers: Map<String, String>?) {
        if (!::appContext.isInitialized) {
            synchronized(this) {
                Config.BASE_URL = baseUrl
                DataModule.decorateRequest(headers)
                appContext = _appContext
                initializeWebView(appContext)
                addUncaughtErrorHandler()
                setupColors()
            }
        }
    }

    private fun initializeWebView(context: Context) = WebView(context)

    private fun addUncaughtErrorHandler() {
        RxJavaPlugins.setErrorHandler {
            Log.e(TAG, "Caught undeliverable error", it)
        }
    }

    private fun setupColors() {
        disposable = DataModule
            .settingsRepository
            .getSettings()
            .map { it.acquirer }
            .retry(3)
            .subscribeBy(
                onSuccess = {
                    val colorRes = it.toColorRes()
                    val logoRes = it.toDrawableRes()
                    with(DataModule.sessionRepository) {
                        set(SessionRepository.Key.COLOR_RES, colorRes)
                        set(SessionRepository.Key.ACQUIRER_RES, logoRes)
                    }
                },
                onError = { /* no-op */ }
            )
    }

    fun validateSdk() {
        check(::appContext.isInitialized) { "PaymentSdk.init() should be called before use." }
    }

    fun clear() {
        disposable?.dispose()
    }
}