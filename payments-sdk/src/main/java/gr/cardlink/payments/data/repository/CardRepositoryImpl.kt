package gr.cardlink.payments.data.repository

import gr.cardlink.payments.data.mapper.toCard
import gr.cardlink.payments.data.model.card.ApiCardDeleteRequest
import gr.cardlink.payments.data.service.ApiService
import gr.cardlink.payments.domain.model.Card
import gr.cardlink.payments.domain.repository.CardRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.HttpURLConnection.HTTP_OK

internal class CardRepositoryImpl(
    private val apiService: ApiService
) : CardRepository {

    override fun getUserCards(): Single<List<Card>> {
        return apiService
            .getUserCards()
            .subscribeOn(Schedulers.io())
            .map { _response ->
                if (_response.status == HTTP_OK) {
                    _response.cards.mapNotNull { it.toCard() }
                } else {
                    emptyList()
                }
            }
    }

    override fun deleteCard(token: String): Completable {
        val request = ApiCardDeleteRequest(cardToken = token)

        return apiService
            .deleteCard(request)
            .subscribeOn(Schedulers.io())
            .ignoreElement()
    }

}