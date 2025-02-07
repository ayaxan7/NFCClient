package com.hustle.nfcclient

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.hustle.nfcclient.network.NFCCardInfo
import com.hustle.nfcclient.network.NFCViewModel

@Composable
fun NFCReaderApp(viewModel: NFCViewModel) {
    val scanResult by viewModel.scanResult.collectAsState()
    val context = LocalContext.current // Get the current Context

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NFC Reader",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Manually trigger an NFC scan with test data
                    val testCardInfo = NFCCardInfo(
                        cardId = "12345678",
                        cardType = "MIFARE Classic 1K",
                        features = "UID size: single, Regular frame, 106 kbit/s"
                    )

                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        viewModel.processNFCTag(testCardInfo, location)
                    }
                }
            ) {
                Text("Simulate NFC Scan")
            }

            Spacer(modifier = Modifier.height(16.dp))

            scanResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Card ID: ${result.cardInfo.cardId}")
                        Text("Card Type: ${result.cardInfo.cardType}")
                        Text("Features: ${result.cardInfo.features}")
                        Text("Timestamp: ${result.timestamp}")
                        Text("Location: ${result.location?.latitude}, ${result.location?.longitude}")
                        Text(
                            text = result.message,
                            color = if (result.success)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            } ?: Text("Tap an NFC card to scan or press the button")
        }
    }
}
