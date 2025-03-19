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

@Composable
fun UserUIScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courses by remember { mutableStateOf(listOf<DocumentSnapshot>()) }

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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, User!",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }
    }
}

fun getCoursesUser(db: FirebaseFirestore, userId: String, onResult: (List<DocumentSnapshot>) -> Unit) {
    db.collection("users").document(userId).collection("usercourses") // Target the subcollection
        .get()
        .addOnSuccessListener { documents ->
            onResult(documents.documents) // Return the list of courses
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching courses", e)
        }
}