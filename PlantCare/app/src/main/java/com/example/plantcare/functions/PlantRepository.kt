package com.example.plantcare.functions

import com.example.plantcare.Plants
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("plants")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    suspend fun addPlant(plant: Plants) {
        if (currentUser == null) return
        val document = collection.document()
        val plantWithId = plant.copy(plantID = document.id, ownerID = currentUser.uid)
        document.set(plantWithId).await()
    }

    suspend fun getPlant(): List<Plants> {
        if (currentUser == null) return emptyList()
        val snapshot = collection.whereEqualTo("ownerID", currentUser.uid).get().await()
        return snapshot.documents.mapNotNull { document ->
            document.toObject(Plants::class.java)?.copy(
                lastWatered = document.getTimestamp("lastWatered") ?: Timestamp.now()
            )
        }
    }

    suspend fun getPlantbyID(plantId: String): Plants? {
        if (currentUser == null) return null
        val snapshot = collection.document(plantId).get().await()
        return snapshot.toObject(Plants::class.java)?.copy(
            lastWatered = snapshot.getTimestamp("lastWatered") ?: Timestamp.now()
        )
    }

    suspend fun deletePlant(plantId: String) {
        collection.document(plantId).delete().await()
    }

    suspend fun updatePlant(plant: Plants) {
        collection.document(plant.plantID).set(plant).await()
    }

    suspend fun updateWateringDate(plantId: String, lastWateredDate: Timestamp) {
        val updates = hashMapOf<String, Any>(
            "lastWatered" to lastWateredDate
        )
        collection.document(plantId).update(updates).await()
    }

}
