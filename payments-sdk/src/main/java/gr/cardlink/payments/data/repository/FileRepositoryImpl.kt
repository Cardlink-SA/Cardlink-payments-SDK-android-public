package gr.cardlink.payments.data.repository

import gr.cardlink.payments.config.Config
import gr.cardlink.payments.data.model.ApiResponse
import gr.cardlink.payments.data.service.ApiService
import gr.cardlink.payments.domain.repository.FileRepository
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.HttpURLConnection.HTTP_OK

internal class FileRepositoryImpl(
    private val apiService: ApiService
) : FileRepository {

    override fun getRemoteValidator(): Single<String> {
        return apiService
            .downloadFile(Config.JS_URL)
            .map { handleResponse(it) }
            .subscribeOn(Schedulers.io())
    }

    private fun handleResponse(apiResponse: ApiResponse): String {
        return if (apiResponse.status == HTTP_OK && !apiResponse.body.isNullOrEmpty()) {
            apiResponse.body
        } else {
            throw RuntimeException(apiResponse.message ?: "Could not get remote JS validator")
        }
    }

}