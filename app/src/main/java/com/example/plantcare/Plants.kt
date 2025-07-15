package com.example.plantcare

import com.google.firebase.Timestamp

data class Plants(
    val plantID: String = "",
    val ownerID: String = "",
    val name: String = "",
    val note: String = "",
    val species: String = "",
    val lastWatered: Timestamp = Timestamp.now(),
    val imageUri: String = "",
    val waterInterval: Int = 0
)
