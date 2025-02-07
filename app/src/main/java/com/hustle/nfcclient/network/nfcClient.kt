package com.hustle.nfcclient.network

import android.nfc.Tag
import android.nfc.tech.NfcA


class nfcClient {

    companion object {
        const val MIFARE_CLASSIC_1K = 0x08
        const val MIFARE_CLASSIC_4K = 0x18
        const val MIFARE_ULTRALIGHT = 0x00
    }

    fun readNFCTag(tag: Tag): String {
        val nfcA = NfcA.get(tag)

        return try {
            nfcA.connect()
            val atqa = nfcA.atqa
            val sak = nfcA.sak
            val cardType = determineCardType(sak)
            val cardFeatures = analyzeATQA(atqa)
            // Create detailed card information
            val cardId = bytesToHexString(tag.id)
            val cardInfo = buildString {
                append("Card ID: $cardId\n")
                append("Card Type: $cardType\n")
                append("Features: $cardFeatures")
            }
            nfcA.close()
            cardInfo

        } catch (e: Exception) {
            e.printStackTrace()
            "Error reading card: ${e.message}"
        }
    }

    private fun determineCardType(sak: Short): String {
        return when (sak.toInt()) {
            MIFARE_CLASSIC_1K -> "MIFARE Classic 1K"
            MIFARE_CLASSIC_4K -> "MIFARE Classic 4K"
            MIFARE_ULTRALIGHT -> "MIFARE Ultralight"
            else -> "Unknown card type (SAK: ${String.format("0x%02X", sak)})"
        }
    }

    private fun analyzeATQA(atqa: ByteArray): String {
        val features = mutableListOf<String>()

        // Analyze first byte of ATQA
        when (atqa[0].toInt() and 0x1F) {
            0x00 -> features.add("Regular frame, 106 kbit/s")
            0x01 -> features.add("Enhanced frame, 106 kbit/s")
            else -> features.add("Unknown frame format")
        }

        // Analyze second byte of ATQA
        if ((atqa[1].toInt() and 0x04) != 0) {
            features.add("RFU")
        }
        if ((atqa[1].toInt() and 0x02) != 0) {
            features.add("UID size: double")
        } else {
            features.add("UID size: single")
        }

        return features.joinToString(", ")
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}