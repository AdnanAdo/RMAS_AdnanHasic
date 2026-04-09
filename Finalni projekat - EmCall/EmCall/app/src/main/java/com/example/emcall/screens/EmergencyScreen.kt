package com.example.emcall.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val sharedPref = context.getSharedPreferences("EmCallPrefs", Context.MODE_PRIVATE)
    val savedVerificationCode = sharedPref.getString("user_verification_code", "Nepoznato") ?: "Nepoznato"

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var timeLeft by remember { mutableIntStateOf(5) }
    var isSent by remember { mutableStateOf(false) }
    var isCancelled by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = timeLeft / 5f,
        animationSpec = tween(durationMillis = 1000),
        label = "countdown_progress"
    )

    fun sendSosToFirebase(lat: Double, lng: Double) {
        val batch = db.batch()
        val reportRef = db.collection("reports").document()
        val notificationRef = db.collection("notifications").document()

        val sosReport = hashMapOf(
            "verification_Code" to savedVerificationCode,
            "report_type" to "HITNA",
            "description" to "HITAN SOS SIGNAL!",
            "latitude" to lat,
            "longitude" to lng,
            "address" to "",
            "image_url" to null,
            "services_needed" to listOf("Sve službe"),
            "timestamp" to Timestamp.now()
        )

        val sosNotification = hashMapOf(
            "title" to "KRITIČNO: SOS Signal!",
            "message" to "Korisnik $savedVerificationCode je aktivirao SOS na lokaciji $lat, $lng!",
            "timestamp" to Timestamp.now(),
            "isRead" to false,
            "type" to "SOS_ALERT"
        )

        batch.set(reportRef, sosReport)
        batch.set(notificationRef, sosNotification)

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "SOS signal uspješno emitovan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Greška pri slanju SOS-a!", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isCancelled) {
            delay(1000L)
            if (!isCancelled) {
                timeLeft -= 1
            }
        }

        if (!isCancelled && timeLeft == 0) {
            isSent = true

            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        sendSosToFirebase(location.latitude, location.longitude)
                    } else {
                        sendSosToFirebase(0.0, 0.0)
                        Toast.makeText(context, "SOS poslan bez precizne GPS lokacije.", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    sendSosToFirebase(0.0, 0.0)
                }
            } else {
                sendSosToFirebase(0.0, 0.0)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HITNA POMOĆ", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            isCancelled = true
                            onBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFD32F2F),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isSent) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SOS SE ŠALJE ZA:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFFD32F2F),
                            strokeWidth = 16.dp,
                            trackColor = Color(0xFFFFEBEE)
                        )

                        Text(
                            text = "$timeLeft",
                            fontSize = 90.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD32F2F),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Kliknite ispod ukoliko ste slučajno pritisnuli dugme za hitne slučajeve.",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF388E3C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Poslano",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "SOS SIGNAL POSLAN!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF388E3C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Vaš kod ($savedVerificationCode) i lokacija su uspješno proslijeđeni hitnim službama. Ostanite smireni.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        isCancelled = true
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSent) Color.White else Color(0xFF1976D2),
                        contentColor = if (!isSent) Color(0xFFD32F2F) else Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    border = if (!isSent) androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFD32F2F)) else null
                ) {
                    Text(
                        text = if (!isSent) "OTKAŽI SLANJE" else "VRATI SE NA POČETNU",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}