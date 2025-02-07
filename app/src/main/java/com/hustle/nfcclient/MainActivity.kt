package com.hustle.nfcclient

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.hustle.nfcclient.network.NFCCardInfo
import com.hustle.nfcclient.network.NFCViewModel
import com.hustle.nfcclient.network.nfcClient

class MainActivity : ComponentActivity() {
    private val viewModel: NFCViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val nfcClient:nfcClient = nfcClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // Request permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )

        setContent {
            NFCReaderApp(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let { processTag(it) }
        }
    }

    private fun processTag(tag: Tag) {
        val cardInfoString = if (tag.id.isEmpty()) {
            intent.getStringExtra("card_data") ?: "Error: No tag data"
        } else {
            nfcClient.readNFCTag(tag)
        }
        // Parse the card info string into structured data
        val lines = cardInfoString.split("\n")
        val cardInfo = NFCCardInfo(
            cardId = lines[0].substringAfter("Card ID: "),
            cardType = lines[1].substringAfter("Card Type: "),
            features = lines[2].substringAfter("Features: ")
        )

        // Get current location and process
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            viewModel.processNFCTag(cardInfo, location)
        }
    }
}