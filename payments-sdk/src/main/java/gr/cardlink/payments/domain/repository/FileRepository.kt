package gr.cardlink.payments.domain.repository

import io.reactivex.rxjava3.core.Single

internal interface FileRepository {

    fun getRemoteValidator(): Single<String>

}