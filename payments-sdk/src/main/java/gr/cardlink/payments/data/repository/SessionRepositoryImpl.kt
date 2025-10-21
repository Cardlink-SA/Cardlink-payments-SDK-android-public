package gr.cardlink.payments.data.repository

import gr.cardlink.payments.domain.repository.SessionRepository

internal class SessionRepositoryImpl : SessionRepository {

    private val map = hashMapOf<SessionRepository.Key, Any?>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: SessionRepository.Key, shouldRemove: Boolean): T? {
        val value = map[key] as? T
        if (shouldRemove) {
            remove(key)
        }
        return value
    }

    @Synchronized
    override fun set(key: SessionRepository.Key, value: Any?) {
        map[key] = value
    }

    @Synchronized
    override fun remove(key: SessionRepository.Key) {
        map.remove(key)
    }

    /**
     * Clears sessions except for [SessionRepository.Key.COLOR_RES] that should be retained to prevent
     * color discrepancies between screens.
     * */
    @Synchronized
    override fun clear() {
        map.keys
            .filterNot { it == SessionRepository.Key.COLOR_RES || it == SessionRepository.Key.ACQUIRER_RES }
            .forEach { map.remove(it) }
    }

}