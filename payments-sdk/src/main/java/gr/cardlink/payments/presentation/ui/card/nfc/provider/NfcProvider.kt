package gr.cardlink.payments.presentation.ui.card.nfc.provider

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.exception.CommunicationException
import com.github.devnied.emvnfccard.parser.IProvider

internal class NfcProvider(private val isoDep: IsoDep) : IProvider {

    override fun transceive(pCommand: ByteArray?): ByteArray? {
        return try {
            if (isoDep.isConnected) {
                isoDep.transceive(pCommand)
            } else {
                null
            }
        } catch (ex: Exception) {
            throw CommunicationException(ex.message)
        }
    }

    override fun getAt(): ByteArray = isoDep.historicalBytes ?: isoDep.hiLayerResponse
}