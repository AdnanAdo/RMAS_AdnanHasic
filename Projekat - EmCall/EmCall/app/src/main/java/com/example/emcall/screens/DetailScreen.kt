package com.example.emcall.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class EmergencyReport(
    val verification_Code: String = "",
    val description: String = "",
    val image_url: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)

data class NotificationData(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val type: String = "EMERGENCY"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(verificationCode: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance("gs://emcall-9d09a.firebasestorage.app").reference
    val db = FirebaseFirestore.getInstance()

    var description by remember { mutableStateOf("") }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> capturedImageUri = uri }

    fun saveToFirestore(imageUrl: String?) {
        val batch = db.batch()
        val reportRef = db.collection("reports").document()
        val notificationRef = db.collection("notifications").document()

        val report = EmergencyReport(
            verification_Code = verificationCode,
            description = description,
            image_url = imageUrl,
            timestamp = Timestamp.now()
        )

        val notification = NotificationData(
            title = "Detaljna obavijest",
            message = "Kod: $verificationCode - $description",
            timestamp = Timestamp.now(),
            isRead = false
        )

        batch.set(reportRef, report)
        batch.set(notificationRef, notification)

        batch.commit()
            .addOnSuccessListener {
                isUploading = false
                Toast.makeText(context, "Izvještaj i notifikacija uspješno poslani!", Toast.LENGTH_LONG).show()
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

        if (capturedImageUri != null) {
            val fileName = "report_${System.currentTimeMillis()}.jpg"
            val imageRef = storage.child("report_images/$fileName")

            imageRef.putFile(capturedImageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveToFirestore(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    isUploading = false
                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveToFirestore(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detaljna obavijest") },
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
                .padding(16.dp)
        ) {
            Text("DETALJNA OBAVIJEST", style = MaterialTheme.typography.titleLarge)
            Text("FGM, Tuzla, 75000", color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text(if (capturedImageUri == null) "+ Dodajte fotografiju (opcionalno)" else "Fotografija dodana ✓")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                label = { Text("Opiši događaj") },
                placeholder = { Text("Unesite detalje incidenta...") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { submitReport() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Potvrdi obavijest", color = Color.White)
                }
            }
        }
    }
}