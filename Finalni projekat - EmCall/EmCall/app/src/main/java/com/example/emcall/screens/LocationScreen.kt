package com.example.emcall.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

data class EmergencyVenue(
    val name: String = "",
    val type: String = "",
    val city: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val phone: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val db = FirebaseFirestore.getInstance()

    var locationText by remember { mutableStateOf("Tražim lokaciju...") }
    var currentCoordinates by remember { mutableStateOf("Nije dostupno") }
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }
    var showNearestServices by remember { mutableStateOf(false) }
    var firebaseVenues by remember { mutableStateOf<List<EmergencyVenue>>(emptyList()) }
    var isFetchingData by remember { mutableStateOf(false) }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000
    }

    fun fetchVenuesFromFirebase() {
        isFetchingData = true
        db.collection("emergency_venues").get()
            .addOnSuccessListener { documents ->
                val fetchedList = mutableListOf<EmergencyVenue>()
                for (document in documents) {
                    val name = document.getString("name") ?: "Nepoznato"
                    val type = document.getString("type") ?: "Nepoznato"
                    val city = document.getString("city") ?: "Nepoznato"
                    val phone = document.getString("phone") ?: ""
                    val locationGeoPoint = document.getGeoPoint("location")

                    if (locationGeoPoint != null) {
                        fetchedList.add(
                            EmergencyVenue(
                                name = name,
                                type = type,
                                city = city,
                                lat = locationGeoPoint.latitude,
                                lng = locationGeoPoint.longitude,
                                phone = phone
                            )
                        )
                    }
                }
                firebaseVenues = fetchedList
                isFetchingData = false
            }
            .addOnFailureListener { e ->
                isFetchingData = false
                Toast.makeText(context, "Greška pri učitavanju službi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

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
                userLat = location.latitude
                userLng = location.longitude
                val coords = "${location.latitude}, ${location.longitude}"
                locationText = "Vaša trenutna GPS lokacija"
                currentCoordinates = coords
                fetchVenuesFromFirebase()
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Moja Lokacija", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = locationText,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Koordinate:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCoordinates,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (currentCoordinates != "Nije dostupno") {
                                    val clip = android.content.ClipData.newPlainText("Lokacija", currentCoordinates)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Koordinate kopirane!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFF0F0F0))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopiraj", tint = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { getLocation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "OSVJEŽI LOKACIJU", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (userLat != null && userLng != null) {
                            showNearestServices = !showNearestServices
                        } else {
                            Toast.makeText(context, "Sačekajte da se pronađe vaša lokacija!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isFetchingData,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD32F2F)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    if (isFetchingData) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFD32F2F), strokeWidth = 3.dp)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showNearestServices) "SAKRIJ SLUŽBE" else "PRONAĐI NAJBLIŽE SLUŽBE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (showNearestServices && userLat != null && userLng != null) {
                    if (firebaseVenues.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Trenutno nema unesenih službi u bazi.", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        Text(
                            text = "Najbliže službe u vašoj okolini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val sortedVenues = firebaseVenues.sortedBy { venue ->
                            calculateDistance(userLat!!, userLng!!, venue.lat, venue.lng)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sortedVenues) { venue ->
                                val distance = calculateDistance(userLat!!, userLng!!, venue.lat, venue.lng)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = venue.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "${venue.type} • ${venue.city}", color = Color.DarkGray, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Udaljenost: ${String.format("%.2f", distance)} km",
                                                color = Color(0xFFD32F2F),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (venue.phone.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${venue.phone}"))
                                                        context.startActivity(intent)
                                                    },
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFE8F5E9))
                                                ) {
                                                    Icon(Icons.Default.Call, contentDescription = "Nazovi", tint = Color(0xFF2E7D32))
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    val gmmIntentUri = Uri.parse("google.navigation:q=${venue.lat},${venue.lng}")
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                    mapIntent.setPackage("com.google.android.apps.maps")
                                                    context.startActivity(mapIntent)
                                                },
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE3F2FD))
                                            ) {
                                                Icon(Icons.Default.Directions, contentDescription = "Navigacija", tint = Color(0xFF1976D2))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
