package com.example.emcall.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var notificationList by remember { mutableStateOf(listOf<NotificationData>()) }

    LaunchedEffect(Unit) {
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    notificationList = snapshot.toObjects(NotificationData::class.java)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikacije") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (notificationList.isEmpty()) {
            Text(
                "Nema novih notifikacija",
                modifier = Modifier.padding(paddingValues).padding(16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                items(notificationList) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationData) {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateString = sdf.format(notification.timestamp.toDate())

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (notification.type == "EMERGENCY") Color.Red else Color.Black
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}