package com.example.emcall.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String, String, String, String) -> Unit) {
    val context = LocalContext.current

    var verificationCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EmCall",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD32F2F),
                fontSize = 36.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Vaša sigurnost na prvom mjestu",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dobrodošli nazad",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Unesite svoj tajni verifikacijski broj za pristup profilu.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = {
                            verificationCode = it
                            errorMessage = ""
                        },
                        label = { Text("Verifikacijski broj") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            cursorColor = Color(0xFF1976D2)
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (verificationCode.isBlank()) {
                                errorMessage = "Molimo unesite verifikacijski broj."
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

                                            val sharedPref = context.getSharedPreferences("EmCallPrefs", Context.MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putString("user_verification_code", verificationCode)
                                                apply()
                                            }
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
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            disabledContainerColor = Color(0xFF90CAF9)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("PRIJAVI SE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "EmCall je aplikacija koja može spasiti\nVaš i život ljudi oko Vas.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 18.sp
            )
        }
    }
}