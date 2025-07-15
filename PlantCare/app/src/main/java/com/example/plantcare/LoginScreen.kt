package com.example.plantcare

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (isLogin) "Logowanie" else "Rejestracja", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isLogin) {
                    loginUser(auth, email, password, context, navController)
                } else {
                    registerUser(auth, email, password, context)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLogin) "Zaloguj" else "Zarejestruj")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "Nie masz konta? Zarejestruj się" else "Masz już konto? Zaloguj się")
        }
    }
}

fun loginUser(auth: FirebaseAuth, email: String, password: String, context: android.content.Context, navController: NavController) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Zalogowano!", Toast.LENGTH_SHORT).show()
                navController.navigate("plants")
            } else {
                Toast.makeText(context, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

fun registerUser(auth: FirebaseAuth, email: String, password: String, context: android.content.Context) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    val userData = hashMapOf(
                        "uid" to it.uid,
                        "email" to it.email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .document(it.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Rejestracja zakończona!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Błąd dodawania do bazy: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Błąd rejestracji: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}
