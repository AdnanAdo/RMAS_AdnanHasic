package com.example.emcall.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HandbookItem(val title: String, val content: String, val icon: ImageVector, val iconTint: Color, val iconBg: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandbookScreen(onBack: () -> Unit) {
    var selectedItem by remember { mutableStateOf<HandbookItem?>(null) }

    val prvaPomoc = listOf(
        HandbookItem(
            title = "Reanimacija",
            content = "1. Provjerite sigurnost okoline.\n\n2. Provjerite svijest (zazovite i lagano protresite osobu).\n\n3. Pozovite hitnu pomoć.\n\n4. Otvorite dišni put (zabacite glavu unazad).\n\n5. Provjerite disanje.\n\n6. Započnite masažu srca: 30 pritisaka na sredinu prsnog koša, zatim 2 upuhivanja zraka.",
            icon = Icons.Default.Favorite,
            iconTint = Color(0xFFD32F2F),
            iconBg = Color(0xFFFFEBEE)
        ),
        HandbookItem(
            title = "Gušenje",
            content = "1. Potaknite osobu na kašalj.\n\n2. Ako ne može kašljati, primijenite do 5 udaraca u leđa (između lopatica).\n\n3. Ako to ne pomogne, primijenite Heimlichov zahvat (do 5 pritisaka u trbuh).\n\n4. Ponavljajte postupak i pozovite hitnu pomoć.",
            icon = Icons.Default.Favorite,
            iconTint = Color(0xFFD32F2F),
            iconBg = Color(0xFFFFEBEE)
        ),
        HandbookItem(
            title = "Jako krvarenje",
            content = "1. Primijenite direktan pritisak na ranu koristeći čistu tkaninu ili gazu.\n\n2. Ako je moguće, podignite povrijeđeni dio tijela iznad nivoa srca.\n\n3. Ne uklanjajte predmet ako je zaglavljen u rani.\n\n4. Pozovite hitnu pomoć.",
            icon = Icons.Default.Favorite,
            iconTint = Color(0xFFD32F2F),
            iconBg = Color(0xFFFFEBEE)
        )
    )

    val nesrece = listOf(
        HandbookItem(
            title = "Požar",
            content = "1. Ostanite smireni i aktivirajte protivpožarni alarm.\n\n2. Odmah pozovite vatrogasce.\n\n3. Ne koristite liftove, idite stepenicama.\n\n4. Ako je prostorija puna dima, spustite se nisko prema podu.\n\n5. Ako vam se zapali odjeća: STANI, LEZI i KOTRLJAJ SE.",
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFE65100),
            iconBg = Color(0xFFFFF3E0)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FA)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Priručnik za hitne slučajeve", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Nazad"
                            )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "PRVA POMOĆ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                    )
                }

                items(prvaPomoc) { stavka ->
                    HandbookCard(stavka) { selectedItem = stavka }
                }

                item {
                    Text(
                        text = "NESREĆE I INCIDENTI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                    )
                }

                items(nesrece) { stavka ->
                    HandbookCard(stavka) { selectedItem = stavka }
                }
            }

            if (selectedItem != null) {
                AlertDialog(
                    onDismissRequest = { selectedItem = null },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(selectedItem!!.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedItem!!.icon,
                                    contentDescription = null,
                                    tint = selectedItem!!.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedItem!!.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
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
                            Text(
                                text = selectedItem!!.content,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = Color.DarkGray
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { selectedItem = null },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RAZUMIJEM", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HandbookCard(item: HandbookItem, onClick: () -> Unit) {
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
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Otvori",
                tint = Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}