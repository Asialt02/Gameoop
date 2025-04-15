package com.example.programmeringskurstilmorilddataba.ui.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.example.programmeringskurstilmorilddataba.navigation.BottomNavBar

@Composable
fun UserUIScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courses by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
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
            db.collection("users")
                .document(userId)
                .collection("activeCourses")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Convert DocumentSnapshots to List<Map<String, Any>>
                    courses = querySnapshot.documents.map { document ->
                        document.data?.toMutableMap() ?: mutableMapOf<String, Any>().apply {
                            // Ensure required fields exist with default values
                            put("courseName", document.getString("courseName") ?: "Untitled")
                            put("modulesComplete", document.getLong("modulesComplete")?.toInt() ?: 0)
                            put("numberOfModules", document.getLong("numberOfModules")?.toInt() ?: 1)
                        }
                    }
                }
        }
    }
    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Welcome, ${userName}!",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                ActiveCourseList(navController = navController, courses = courses)
                Spacer(modifier = Modifier.height(8.dp))
            }
            BottomNavBar(navController)
        }
    }
}
