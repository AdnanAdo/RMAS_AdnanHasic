package com.example.emcall.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EmergencyReport(
    val verification_Code: String = "",
    val description: String = "",
    val image_urls: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val services_needed: List<String> = emptyList()
)

data class NotificationData(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val type: String = "EMERGENCY",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val services_needed: List<String> = emptyList(),
    val address: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storage = FirebaseStorage.getInstance("gs://emcall-9d09a.firebasestorage.app").reference
    val db = FirebaseFirestore.getInstance()
    val sharedPref = context.getSharedPreferences("EmCallPrefs", Context.MODE_PRIVATE)
    val savedVerificationCode = sharedPref.getString("user_verification_code", "Nepoznato") ?: "Nepoznato"
    val selectedServices = remember { mutableStateListOf<String>() }
    val availableServices = listOf("Policija", "Hitna pomoc", "Vatrogasci")
    var description by remember { mutableStateOf("") }

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(LatLng(44.5375, 18.6735)) }
    var locationAddress by remember { mutableStateOf("Tuzla, BiH") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }

    fun createImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) storageDir.mkdirs()

        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = selectedImageUris + uris
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUris = selectedImageUris + tempCameraUri!!
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempCameraUri = createImageUri(context)
            cameraLauncher.launch(tempCameraUri!!)
        } else {
            Toast.makeText(context, "Dozvola za kameru je odbijena", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(query, 1)

                if (!results.isNullOrEmpty()) {
                    val location = results[0]
                    val latLng = LatLng(location.latitude, location.longitude)

                    withContext(Dispatchers.Main) {
                        selectedLocation = latLng
                        locationAddress = location.getAddressLine(0) ?: query
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lokacija nije pronađena", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Greška u pretrazi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveToFirestore(imageUrls: List<String>) {
        if (selectedServices.isEmpty()) {
            Toast.makeText(context, "Odaberite bar jednu sluzbu!", Toast.LENGTH_SHORT).show()
            isUploading = false
            return
        }
        val batch = db.batch()
        val reportRef = db.collection("reports").document()
        val notificationRef = db.collection("notifications").document()

        val report = EmergencyReport(
            verification_Code = savedVerificationCode,
            description = description,
            image_urls = imageUrls,
            latitude = selectedLocation.latitude,
            longitude = selectedLocation.longitude,
            address = locationAddress,
            timestamp = Timestamp.now(),
            services_needed = selectedServices.toList()
        )

        val notification = NotificationData(
            title = "Prijava incidenta",
            message = "Kod: $savedVerificationCode na lokaciji: $locationAddress",
            timestamp = Timestamp.now(),
            isRead = false,
            description = description,
            imageUrls = imageUrls,
            services_needed = selectedServices.toList(),
            address = locationAddress
        )

        batch.set(reportRef, report)
        batch.set(notificationRef, notification)

        batch.commit()
            .addOnSuccessListener {
                isUploading = false
                Toast.makeText(context, "Izvještaj uspješno poslan!", Toast.LENGTH_LONG).show()
                onBack()
            }
            .addOnFailureListener { e ->
                isUploading = false
                Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun submitReport() {
        if (description.isBlank()) {
            Toast.makeText(context, "Molimo unesite opis", Toast.LENGTH_SHORT).show()
            return
        }
        isUploading = true

        if (selectedImageUris.isNotEmpty()) {
            val uploadedUrls = mutableListOf<String>()
            var completedUploads = 0

            for ((index, uri) in selectedImageUris.withIndex()) {
                val fileName = "report_${System.currentTimeMillis()}_$index.jpg"
                val imageRef = storage.child("report_images/$fileName")

                imageRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) { task.exception?.let { throw it } }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uploadedUrls.add(task.result.toString())
                    }
                    completedUploads++

                    if (completedUploads == selectedImageUris.size) {
                        saveToFirestore(uploadedUrls)
                    }
                }
            }
        } else {
            saveToFirestore(emptyList())
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detaljna prijava", fontWeight = FontWeight.Bold) },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Lokacija incidenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pretraži grad, ulicu...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { searchLocation(searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = "Traži", tint = Color.Gray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFFD32F2F)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                            locationAddress = "Odabrana na mapi (${String.format("%.4f", latLng.latitude)}, ${String.format("%.4f", latLng.longitude)})"
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = selectedLocation),
                            title = "Mjesto incidenta"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Odabrana adresa: $locationAddress",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Opis i fotografije",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    placeholder = { Text("Opišite detaljno šta se dogodilo...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFFD32F2F)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { showImageSourceDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1976D2)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedImageUris.isEmpty()) "Dodaj fotografije" else "Dodano slika: ${selectedImageUris.size}")
                    }

                    if (selectedImageUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { selectedImageUris = emptyList() },
                            modifier = Modifier
                                .background(Color(0xFFFFEBEE), CircleShape)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Poništi", tint = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Službe koje su potrebne",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableServices.forEach { service ->
                        val isSelected = selectedServices.contains(service)

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clickable {
                                    if (isSelected) selectedServices.remove(service)
                                    else selectedServices.add(service)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF4CAF50) else Color.LightGray
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (service == "Hitna pomoc") "Hitna" else service,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF2E7D32) else Color.DarkGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { submitReport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        disabledContainerColor = Color(0xFFEF9A9A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Slanje u toku...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("POŠALJI PRIJAVU", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Dodaj fotografije", fontWeight = FontWeight.Bold) },
                    text = { Text("Odaberite izvor odakle želite dodati fotografije incidenta.") },
                    shape = RoundedCornerShape(16.dp),
                    confirmButton = {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                tempCameraUri = createImageUri(context)
                                cameraLauncher.launch(tempCameraUri!!)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Text("Snimi kamerom", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }) {
                            Text("Iz galerije", color = Color(0xFF1976D2))
                        }
                    }
                )
            }
        }
    }
}