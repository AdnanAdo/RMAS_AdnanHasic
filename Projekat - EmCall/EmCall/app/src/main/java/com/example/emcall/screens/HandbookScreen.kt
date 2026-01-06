package com.example.emcall.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandbookScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Priručnik") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    "PRVA POMOĆ",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                )
            }

            val prvaPomoc = listOf("Reanimacija", "Gušenje", "Jako krvarenje")
            items(prvaPomoc) { stavka ->
                ListItem(headlineContent = { Text(stavka) })
                HorizontalDivider()
            }

            item {
                Text(
                    "NESREĆE I INCIDENTI",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                )
            }
            item { ListItem(headlineContent = { Text("POŽAR") }) }
        }
    }
}