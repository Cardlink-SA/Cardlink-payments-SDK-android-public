package gr.cardlink.payments.data.extension

import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.lang.reflect.Type

internal fun InputStream.readFile(): String? {
    return try {
        bufferedReader().use { it.readText() }
    } catch (ex: Exception) {
        null
    }

}

internal inline fun <reified T> genericTypeToken(): Type = object: TypeToken<T>() {}.type
