package gr.cardlink.payments.presentation.extension

internal inline fun <T: Any> safeLet(vararg elements: T?, closure: (List<T>) -> Unit) {
    if (elements.all { it != null }) {
        closure(elements.filterNotNull())
    }
}

internal fun String.removeWhiteSpaces() = replace("[\\D]".toRegex(), "")