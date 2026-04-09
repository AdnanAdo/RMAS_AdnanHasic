package com.example.emcall.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var notificationList by remember { mutableStateOf(listOf<Pair<String, NotificationData>>()) }
    var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }

    fun deleteNotification(documentId: String) {
        db.collection("notifications").document(documentId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Notifikacija obrisana", Toast.LENGTH_SHORT).show()
                if (selectedNotification != null) {
                    selectedNotification = null
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Greška pri brisanju: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(Unit) {
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.toObject(NotificationData::class.java)
                        if (data != null) Pair(doc.id, data) else null
                    }
                    notificationList = list
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
                    title = { Text("Notifikacije", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
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
            if (notificationList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color(0xFF1976D2)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nemate novih notifikacija",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ovdje će se pojaviti vaša historija prijava.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notificationList) { (docId, notification) ->
                        NotificationItem(
                            notification = notification,
                            onDelete = { deleteNotification(docId) },
                            onClick = { selectedNotification = notification }
                        )
                    }
                }
            }
        }

        if (selectedNotification != null) {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val dateString = selectedNotification?.timestamp?.toDate()?.let { sdf.format(it) } ?: "Nepoznat datum"
            val isEmergency = selectedNotification?.type == "EMERGENCY" || selectedNotification?.title?.contains("SOS") == true

            AlertDialog(
                onDismissRequest = { selectedNotification = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isEmergency) Color(0xFFFFEBEE) else Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEmergency) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isEmergency) Color(0xFFD32F2F) else Color(0xFF1976D2),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedNotification?.title ?: "",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Datum i vrijeme", fontSize = 12.sp, color = Color.Gray)
                                Text(text = dateString, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                                Spacer(modifier = Modifier.height(8.dp))

                                if (!selectedNotification?.address.isNullOrEmpty()) {
                                    Text(text = "Lokacija incidenta", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = selectedNotification?.address ?: "", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedNotification?.services_needed?.isNotEmpty() == true) {
                            Text(text = "OBAVIJEŠTENE SLUŽBE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedNotification?.services_needed?.forEach { service ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = service,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (!selectedNotification?.description.isNullOrEmpty()) {
                            Text(text = "OPIS DOGAĐAJA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedNotification?.description ?: "",
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (selectedNotification?.imageUrls?.isNotEmpty() == true) {
                            Text(text = "PRILOŽENE FOTOGRAFIJE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(selectedNotification!!.imageUrls) { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Slika incidenta",
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedNotification = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("ZATVORI", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationData, onDelete: () -> Unit, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateString = notification.timestamp?.toDate()?.let { sdf.format(it) } ?: "Nepoznat datum"
    val isEmergency = notification.type == "EMERGENCY" || notification.title.contains("SOS")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isEmergency) Color(0xFFFFEBEE) else Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEmergency) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isEmergency) Color(0xFFD32F2F) else Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (notification.imageUrls.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "📷 ${notification.imageUrls.size} slika",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = dateString,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFAFAFA), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Obriši",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}