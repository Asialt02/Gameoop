package com.example.programmeringskurstilmorilddataba

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField

@Composable
fun UserUIScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courses by remember { mutableStateOf(listOf<DocumentSnapshot>()) }
    var userName by remember { mutableStateOf("") }
    val userId = currentUser?.uid
    if (userId != null) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                userName = document.getString("name") ?: ""
                Log.d("Firestore", "User Name: $userName")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch user name", e)
            }
    }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            getCoursesUser(db, userId) { fetchedCourses ->
                courses = fetchedCourses
                fetchedCourses.forEach { doc ->
                    Log.d("Firestore", "Fetched Course: ${doc.data}")
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome, ${userName}!",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Available Courses:",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (courses.isEmpty()) {
                    Text("No courses available.")
                } else {
                    courses.forEach { doc ->
                        Text(
                            text = "${doc["courseName"]}: ${doc["description"]}",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        BottomNavBar(navController)
    }
}


@Composable
fun UserProfile(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var errorMessage by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userIsAdmin by remember { mutableStateOf(false) }
    var userPassword by remember { mutableStateOf("") }

    currentUser?.let {
        db.collection("users").document(it.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userName = document.getString("name") ?: ""
                    userEmail = document.getString("email") ?: ""
                    userIsAdmin = document.getBoolean("isAdmin") ?: false
                    userPassword = document.getString("password") ?: ""
                } else {
                    errorMessage = "User data not found."
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to fetch user data: ${e.message}"
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userEmail,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        BottomNavBar(navController)
    }
}

fun updateUser(db: FirebaseFirestore, userId: String, userName: String, userEmail: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val updatedUser = mapOf(
        "name" to userName,
        "email" to userEmail,
    )
    currentUser?.verifyBeforeUpdateEmail(userEmail)
    db.collection("users").document(userId).update(updatedUser)
        .addOnSuccessListener { Log.d("Firestore", "User Updated Successfully") }
        .addOnFailureListener { e -> Log.w("Firestore", "Error while updating user", e) }
}

fun getCoursesUser(db: FirebaseFirestore, userId: String, onResult: (List<DocumentSnapshot>) -> Unit) {
    db.collection("users").document(userId).collection("usercourses") // Target the subcollection
        .get()
        .addOnSuccessListener { documents ->
            onResult(documents.documents)
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching courses", e)
        }
}