package com.example.plantcare

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.plantcare.functions.PlantRepository
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun DescPlantScreen(navController: NavController, plantId: String, onPlantUpdated: () -> Unit) {
    val plantRepository = remember { PlantRepository() }
    var plant by remember { mutableStateOf<Plants?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(plantId) {
        coroutineScope.launch {
            plant = plantRepository.getPlantbyID(plantId)
        }
    }

    if (plant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val imageResId = getDrawableId(plant!!.imageUri)
    val formattedDate = formatTimestamp(getNextWateringDate(plant!!.lastWatered, plant!!.waterInterval))


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFD9F7D6))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = plant?.name,
            modifier = Modifier.size(150.dp)
        )
        Text(text = plant?.name ?: "Brak nazwy", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Gatunek: ${plant?.species ?: "Nieznany"}")
        Text(text = "Notatka: ${plant?.note ?: "Brak notatki"}")
        Text(text = "Podlewanie co: ${plant?.waterInterval ?: "??"} dni")
        Text(text = "Data następnego podlania: $formattedDate")

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                coroutineScope.launch {
                    plantRepository.deletePlant(plantId)
                    navController.navigate("plants")
                }
            }, modifier = Modifier.weight(1f)) {
                Text("Usuń")
            }
            Button(
                onClick = { navController.navigate("plants") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {

                Text("Wróć")
            }
        }

        Button(onClick = {
            coroutineScope.launch {
                waterPlant(plant!!, plantRepository)
                onPlantUpdated()
                navController.navigate("plants")
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Podlej")
        }

    }
}

fun getNextWateringDate(lastWatered: Timestamp, interval: Int): Timestamp {
    val calendar = Calendar.getInstance()
    calendar.time = lastWatered.toDate()
    calendar.add(Calendar.DAY_OF_YEAR, interval)
    return Timestamp(calendar.time)
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    return sdf.format(timestamp.toDate())
}

suspend fun waterPlant(plant: Plants, plantRepository: PlantRepository) {
    val lastWateredDate = Timestamp.now()
    plantRepository.updateWateringDate(plant.plantID, lastWateredDate)
}


