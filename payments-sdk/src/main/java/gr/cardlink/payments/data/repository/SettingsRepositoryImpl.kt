package gr.cardlink.payments.data.repository

import gr.cardlink.payments.data.mapper.toSettings
import gr.cardlink.payments.data.service.ApiService
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.domain.repository.SettingsRepository
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

internal class SettingsRepositoryImpl(
    private val apiService: ApiService
) : SettingsRepository {

    override fun getSettings(): Single<Settings> {
        return apiService
            .getSettings()
            .subscribeOn(Schedulers.io())
            .map { it.toSettings() }
    }

}