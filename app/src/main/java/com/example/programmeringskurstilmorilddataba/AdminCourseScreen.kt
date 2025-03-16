package com.example.programmeringskurstilmorilddataba

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

@Composable
fun AdminCourseScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courses by remember { mutableStateOf(listOf<DocumentSnapshot>()) }
    var courseName by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    isAdmin = document.getBoolean("isAdmin") ?: false
                }
        }
    }

    if (isAdmin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = courseDescription,
                onValueChange = { courseDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                addCourse(db, courseName, courseDescription)
            }) {
                Text("Add Course")
            }
            Button(onClick = {
                getCourses(db) { courses = it }
            }) {
                Text("Get Course")
            }
            courses.forEach { doc ->
                Text("${doc["courseName"]}: ${doc["description"]}")
            }

            // Logout Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                auth.signOut()
                navController.navigate("login") // Navigate back to the login screen
            }) {
                Text("Log Out")
            }
        }
    } else {
        Text("You do not have permission to access this screen.", modifier = Modifier.padding(16.dp))
    }
}

fun addCourse(db: FirebaseFirestore, courseName: String, description: String) {
    val course = hashMapOf(
        "courseName" to courseName,
        "description" to description
    )
    db.collection("courses").add(course)
        .addOnSuccessListener { Log.d("Firestore", "Course Added") }
        .addOnFailureListener { e -> Log.w("Firestore", "Error while saving Course", e) }
}
