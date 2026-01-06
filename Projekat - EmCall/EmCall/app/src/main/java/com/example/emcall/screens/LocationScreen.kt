package com.example.emcall.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("Tražim lokaciju...") }
    var currentCoordinates by remember { mutableStateOf("Nije dostupno") }

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationText = "Dozvola za GPS nije odobrena"
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val coords = "${location.latitude}, ${location.longitude}"
                locationText = "Lokacija uspješno pronađena"
                currentCoordinates = coords
            } else {
                locationText = "Lokacija nije dostupna (Uključite GPS)"
            }
        }.addOnFailureListener {
            locationText = "Greška pri dohvatu lokacije: ${it.message}"
        }
    }

    LaunchedEffect(Unit) {
        getLocation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moja Lokacija") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Red
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = locationText, fontSize = 18.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentCoordinates,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = {
                        if (currentCoordinates != "Nije dostupno") {
                            val clip = android.content.ClipData.newPlainText("Lokacija", currentCoordinates)
                            clipboardManager.setPrimaryClip(clip)
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { getLocation() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(text = "OSVJEŽI LOKACIJU")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(text = "PRONAĐI najbliže interventne službe")
            }
        }
    }
}