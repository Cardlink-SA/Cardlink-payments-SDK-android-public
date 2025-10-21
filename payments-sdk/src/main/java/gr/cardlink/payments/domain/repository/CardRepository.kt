package gr.cardlink.payments.domain.repository

import gr.cardlink.payments.domain.model.Card
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal interface CardRepository {
    fun getUserCards(): Single<List<Card>>
    fun deleteCard(token: String): Completable
}