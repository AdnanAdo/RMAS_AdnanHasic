package com.example.emcall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emcall.screens.DetailScreen
import com.example.emcall.screens.EmergencyScreen
import com.example.emcall.screens.HandbookScreen
import com.example.emcall.screens.HomeScreen
import com.example.emcall.screens.LocationScreen
import com.example.emcall.screens.LoginScreen
import com.example.emcall.screens.NotificationScreen
import com.example.emcall.screens.ProfileScreen
import com.example.emcall.ui.theme.EmCallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmCallTheme {
                val navController = rememberNavController()

                var userData by remember { mutableStateOf(mapOf<String, String>()) }

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen ( onLoginSuccess = { name, surname, date_of_birth, phone ->
                            userData = mapOf("name" to name,"surname" to surname, "date_of_birth" to date_of_birth, "phone" to phone)
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }

                    composable("home") {
                        HomeScreen(navController = navController)
                    }

                    composable("location") {
                        LocationScreen(onBack = { navController.popBackStack() })
                    }

                    composable("handbook") {
                        HandbookScreen(onBack = { navController.popBackStack() })
                    }

                    composable("notifications") {
                        NotificationScreen(onBack = { navController.popBackStack() })
                    }

                    composable("emergency") {
                        EmergencyScreen(onBack = { navController.popBackStack() })
                    }

                    composable("detail") {
                        val code = userData["verification_code"] ?: "Nepoznat kod"
                        DetailScreen(
                            verificationCode = code,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            name = userData["name"]!!,
                            surname = userData["surname"]!!,
                            date_of_birth = userData["date_of_birth"]!!,
                            phone = userData["phone"]!!,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
