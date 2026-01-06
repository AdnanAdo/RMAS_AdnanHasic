package com.example.emcall.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(onLoginSuccess: (String, String, String, String) -> Unit) {
    var verificationCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EmCall",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 40.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Unesite Vaš verifikacijski broj",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = verificationCode,
            onValueChange = {
                verificationCode = it
                errorMessage = ""
            },
            label = { Text("Verifikacijski broj") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (verificationCode.isBlank()) {
                    errorMessage = "Molimo unesite kod."
                } else {
                    isLoading = true
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(verificationCode).get()
                        .addOnSuccessListener { document ->
                            isLoading = false
                            if (document != null && document.exists()) {
                                val name = document.getString("name") ?: ""
                                val surname = document.getString("surname") ?: ""
                                val dob = document.getString("date_of_birth") ?: ""
                                val phone = document.getString("phone") ?: ""

                                onLoginSuccess(name, surname, dob, phone)
                            } else {
                                errorMessage = "Korisnik sa ovim kodom ne postoji."
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = "Greška pri povezivanju: ${e.localizedMessage}"
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Potvrdi", color = Color.White, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "EmCall je aplikacija koja može spasiti Vaš i život ljudi oko Vas...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}