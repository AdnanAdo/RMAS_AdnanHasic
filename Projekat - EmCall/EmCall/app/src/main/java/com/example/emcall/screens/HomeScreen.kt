package com.example.emcall.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            HeaderIcon(Icons.Default.LocationOn, "Lokacija") {
                navController.navigate("location")
            }
            HeaderIcon(Icons.Default.Help, "Prirucnik") {
                navController.navigate("handbook")
            }
            HeaderIcon(Icons.Default.Notifications, "Notifikacije") {
                navController.navigate("notifications")
            }
            HeaderIcon(Icons.Default.Person, "Profil") {
                navController.navigate("profile")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                navController.navigate("emergency")
            },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = MaterialTheme.shapes.large
        ) {
            Text("HITNA OBAVIJEST", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("detail")
            },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("DETALJNA OBAVIJEST", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Nazovi:", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            EmergencyButton("Policija", Color.Blue, Modifier.weight(1f)) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:122"))
                context.startActivity(intent)
            }
            EmergencyButton("Vatrogasci", Color.Red, Modifier.weight(1f)) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:123"))
                context.startActivity(intent)
            }
            EmergencyButton("Hitna pomoc", Color.Red, Modifier.weight(1f)) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:124"))
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun HeaderIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EmergencyButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit){
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.padding(4.dp).height(60.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(label, color = Color.White)
    }
}