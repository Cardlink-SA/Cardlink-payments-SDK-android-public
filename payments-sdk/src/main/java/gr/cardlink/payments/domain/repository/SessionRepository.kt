package gr.cardlink.payments.domain.repository

internal interface SessionRepository {

    enum class Key {
        PAYMENT_INFO,
        INSTALLMENTS,
        COLOR_RES,
        ACQUIRER_RES,
        INSTALLMENTS_LIST
    }

    fun <T : Any> get(key: Key, shouldRemove: Boolean = false): T?
    fun set(key: Key, value: Any?)
    fun remove(key: Key)
    fun clear()

}