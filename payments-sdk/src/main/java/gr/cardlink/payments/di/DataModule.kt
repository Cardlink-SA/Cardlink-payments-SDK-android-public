package gr.cardlink.payments.di

import gr.cardlink.payments.BuildConfig
import gr.cardlink.payments.config.Config
import gr.cardlink.payments.data.repository.*
import gr.cardlink.payments.data.repository.CardRepositoryImpl
import gr.cardlink.payments.data.repository.FileRepositoryImpl
import gr.cardlink.payments.data.repository.PaymentRepositoryImpl
import gr.cardlink.payments.data.repository.SessionRepositoryImpl
import gr.cardlink.payments.data.service.ApiService
import gr.cardlink.payments.data.validator.Validator
import gr.cardlink.payments.data.validator.WebValidator
import gr.cardlink.payments.domain.repository.CardRepository
import gr.cardlink.payments.domain.repository.FileRepository
import gr.cardlink.payments.domain.repository.SettingsRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

internal object DataModule {

    private const val TIMEOUT = 60L

    private var interceptor: Interceptor? = null

    fun decorateRequest(headers: Map<String, String>?) {
        val additionalHeaders = headers ?: return
        interceptor = Interceptor {
            val requestBuilder = it.request().newBuilder()
            additionalHeaders.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            val modifiedRequest = requestBuilder.build()
            it.proceed(modifiedRequest)
        }
    }

    private val httpLogger by lazy {
        HttpLoggingInterceptor().apply {
            val level = if (BuildConfig.HTTP_LOGGING_ENABLED) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            setLevel(level)
        }
    }

    private val httpClient by lazy {
        OkHttpClient.Builder().apply {
            connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            readTimeout(TIMEOUT, TimeUnit.SECONDS)
            addInterceptor(httpLogger)
            interceptor?.let {
                addInterceptor(it)
            }
        }
    }

    private val retrofit by lazy {
        Retrofit
            .Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(Config.BASE_URL)
            .client(httpClient.build())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val fileRepository: FileRepository by lazy {
        FileRepositoryImpl(apiService)
    }

    val sessionRepository by lazy {
        SessionRepositoryImpl()
    }

    val paymentRepository by lazy {
        PaymentRepositoryImpl(apiService)
    }

    val validator: Validator
        get() = WebValidator(LibraryModule.appContext)

    val cardRepository: CardRepository
        get() = CardRepositoryImpl(apiService)

    val settingsRepository: SettingsRepository
        get() = SettingsRepositoryImpl(apiService)

}
