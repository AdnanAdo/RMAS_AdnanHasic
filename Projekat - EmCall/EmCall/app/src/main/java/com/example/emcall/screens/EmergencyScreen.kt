package com.example.emcall.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HITNA POMOĆ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SOS SIGNAL POSLAN!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Red,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Vaša trenutna lokacija i lični podaci su proslijeđeni nadležnim službama.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color.Red,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(64.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
            ) {
                Text(
                    "OTKAŽI (Greška)",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
    }
}