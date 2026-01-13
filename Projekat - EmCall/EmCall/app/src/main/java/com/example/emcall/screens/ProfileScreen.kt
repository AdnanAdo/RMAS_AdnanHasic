package com.example.emcall.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String,
    surname: String,
    date_of_birth: String,
    phone: String,
    onBack: () -> Unit
 ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moj Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profilna slika",
                modifier = Modifier.size(120.dp),
                tint = Color.Gray
            )

            Text(
                text = "$name $surname",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Korisnički profil",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileDetailItem(Icons.Default.Place, "Datum rođenja", date_of_birth)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    ProfileDetailItem(Icons.Default.Phone, "Telefon", phone)
                }
            }
        }
    }
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}