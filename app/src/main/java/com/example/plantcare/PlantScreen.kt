package com.example.plantcare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plantcare.functions.PlantRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun PlantScreen(navController: NavController) {
    val plantRepository = PlantRepository()
    val coroutineScope = rememberCoroutineScope()
    var plants by remember { mutableStateOf(listOf<Plants>()) }

    var searchQuery by remember { mutableStateOf("") }
    val filteredPlants = plants.filter { it.name.contains(searchQuery, ignoreCase = true) }

    fun refreshPlants() {
        coroutineScope.launch {
            plants = plantRepository.getPlant()
        }
    }

    LaunchedEffect(Unit) {
        refreshPlants()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Tło aplikacji",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Mój ogród",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Wyszukaj roślinę", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.heightIn(min = 200.dp)
            ) {
                items(filteredPlants) { plant ->
                    PlantItem(plant, navController)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("addPlants") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )

            ) {
                Text("Dodaj roślinę", color = Color.Black)
            }
        }
            val context = LocalContext.current
            Button(
                onClick = { testWaterReminder(context) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(40.dp)
                    .border(BorderStroke(2.dp, Color.Black), CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Wyślij",
                    tint = Color.Black
                )
            }

    }
}


@Composable
fun PlantItem(plant: Plants, navController: NavController) {
    val imageResId = getDrawableId(plant.imageUri)
    val timeRemaining = getDaysUntilNextWatering(plant.lastWatered, plant.waterInterval)

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Color.Black),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(140.dp)
            .clickable { navController.navigate(("descPlant/${plant.plantID}")) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9F7D6)) // Jasna zieleń
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = plant.name,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = plant.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
           // Text(text = plant.species, fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Do podlania: $timeRemaining", fontSize = 12.sp, color = Color.Black)
        }
    }
}

fun getDrawableId(imageName: String): Int {
    return when (imageName) {
        "aloes" -> R.drawable.aloes
        "alokazja" -> R.drawable.alokazja
        "bazylia" -> R.drawable.bazylia
        "bonsai" -> R.drawable.bonsai
        "dracena" -> R.drawable.dracena
        "epipremnum" -> R.drawable.epipremnum
        "filodendron" -> R.drawable.filodendron
        "paproc" -> R.drawable.paproc
        "pilea" -> R.drawable.pilea
        "rhipsalis" -> R.drawable.rhipsalis
        "sansevieria" -> R.drawable.sansevieria
        "senecio" -> R.drawable.senecio
        "storczyk" -> R.drawable.storczyk
        "sukulenty" -> R.drawable.sukulenty
        "tulipan" -> R.drawable.tulipan
        "watermelon" -> R.drawable.watermelon
        else -> R.drawable.aloes
    }
}

fun getDaysUntilNextWatering(lastWatered: Timestamp, interval: Int): String {
    val nextWateringMillis = lastWatered.toDate().time + (interval * 24 * 60 * 60 * 1000)
    val nowMillis = System.currentTimeMillis()
    val millisDifference = nextWateringMillis - nowMillis

    return if (millisDifference >= TimeUnit.DAYS.toMillis(1)) {
        val daysRemaining = kotlin.math.ceil(millisDifference / (24 * 60 * 60 * 1000.0)).toLong()
        "$daysRemaining dni"
    } else {
        "${TimeUnit.MILLISECONDS.toHours(millisDifference)} godzin"
    }
}


