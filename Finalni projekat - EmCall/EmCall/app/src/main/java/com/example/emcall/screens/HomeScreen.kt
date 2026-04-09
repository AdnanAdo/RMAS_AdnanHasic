package com.example.emcall.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderIcon(Icons.Default.LocationOn, "Lokacija") {
                    navController.navigate("location")
                }
                HeaderIcon(Icons.Default.Info, "Priručnik") {
                    navController.navigate("handbook")
                }
                HeaderIcon(Icons.Default.Notifications, "Obavijesti") {
                    navController.navigate("notifications")
                }
                HeaderIcon(Icons.Default.Person, "Profil") {
                    navController.navigate("profile")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("emergency") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hitno",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "HITNA OBAVIJEST",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { navController.navigate("detail") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD32F2F)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Detalji",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "DETALJNA PRIJAVA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Brzi poziv dežurnim službama:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyCallCard("Policija", "122", Color(0xFF1976D2), Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:122"))
                    context.startActivity(intent)
                }
                EmergencyCallCard("Vatrogasci", "123", Color(0xFFD32F2F), Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:123"))
                    context.startActivity(intent)
                }
                EmergencyCallCard("Hitna", "124", Color(0xFF388E3C), Modifier.weight(1f)) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:124"))
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun HeaderIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp), tint = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}

@Composable
fun EmergencyCallCard(label: String, number: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(4.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Phone, contentDescription = "Pozovi", modifier = Modifier.size(24.dp), tint = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(number, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Light)
        }
    }
}