package gr.cardlink.payments.domain.repository

import gr.cardlink.payments.domain.model.Settings
import io.reactivex.rxjava3.core.Single

internal interface SettingsRepository {
    fun getSettings(): Single<Settings>
}