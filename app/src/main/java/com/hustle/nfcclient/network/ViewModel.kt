package com.hustle.nfcclient.network

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

data class NFCCardInfo(
    val cardId: String,
    val cardType: String,
    val features: String
)
data class ScanResult(
    val cardInfo: NFCCardInfo,
    val timestamp: Date,
    val location: Location?,
    val success: Boolean,
    val message: String
)

class NFCViewModel : ViewModel() {
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult

    private val firestore = FirebaseFirestore.getInstance()

    fun processNFCTag(cardInfo: NFCCardInfo, location: Location?) {
        val timestamp = Date()

        val scanData = hashMapOf(
            "cardId" to cardInfo.cardId,
            "cardType" to cardInfo.cardType,
            "features" to cardInfo.features,
            "timestamp" to timestamp,
            "latitude" to location?.latitude,
            "longitude" to location?.longitude
        )

        firestore.collection("scans")
            .add(scanData)
            .addOnSuccessListener {
                _scanResult.value = ScanResult(
                    cardInfo = cardInfo,
                    timestamp = timestamp,
                    location = location,
                    success = true,
                    message = "Scan successful"
                )
            }
            .addOnFailureListener { e ->
                _scanResult.value = ScanResult(
                    cardInfo= cardInfo,
                    timestamp = timestamp,
                    location = location,
                    success = false,
                    message = "Error: ${e.message}"
                )
            }
    }
}