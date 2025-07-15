package com.example.plantcare

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plantcare.functions.PlantRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

@Composable
fun AddPlantScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val plantRepository = PlantRepository()

    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var waterFrequency by remember { mutableStateOf("") }
    val selectedIcon = remember { mutableStateOf("aloes") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Dodaj swoją roślinę",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nazwa rośliny") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = species,
            onValueChange = { species = it },
            label = { Text("Gatunek") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Krótka notatka") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = waterFrequency,
            onValueChange = { waterFrequency = it },
            label = { Text("Podlewanie (dni)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = getDrawableId(selectedIcon.value)),
            contentDescription = "Wybrana ikona",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        PlantIconPicker(selectedIcon)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null && name.isNotEmpty() && species.isNotEmpty() && waterFrequency.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                val lastWateredDate = Timestamp.now()
                                val plant = Plants(
                                    plantID = "",
                                    ownerID = userId,
                                    name = name,
                                    note = note,
                                    species = species,
                                    waterInterval = waterFrequency.toInt(),
                                    lastWatered = lastWateredDate,
                                    imageUri = selectedIcon.value.toString()
                                )
                                plantRepository.addPlant(plant)
                                navController.navigate("plants")
                                Toast.makeText(context, "Dodano roślinę!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Błąd dodawania", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Dodaj roślinę")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { navController.navigate("plants") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Wróć")
            }
        }
    }
}

@Composable
fun PlantIconPicker(selectedIcon: MutableState<String>) {
    val icons = listOf(
        "aloes", "alokazja", "bazylia",
        "bonsai", "dracena", "epipremnum",
        "filodendron", "paproc", "pilea",
        "rhipsalis", "sansevieria", "senecio",
        "storczyk", "sukulenty", "tulipan",
        "watermelon"
    )

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { expanded = true }) {
            Text("Wybierz ikonę rośliny")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            icons.forEach { iconName ->
                DropdownMenuItem(
                    text = { Image(painter = painterResource(id = getDrawableId(iconName)), contentDescription = null, modifier = Modifier.size(40.dp)) },
                    onClick = {
                        selectedIcon.value = iconName
                        expanded = false
                    }
                )
            }
        }
    }
}

