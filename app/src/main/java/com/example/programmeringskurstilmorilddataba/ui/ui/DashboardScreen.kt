package com.example.programmeringskurstilmorilddataba.ui.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    var friends by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    val userId = currentUser?.uid

    if (userId != null) {
        LaunchedEffect(userId) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                    Log.d("Firestore", "User Name: $userName")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to fetch user name", e)
                }
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            db.collection("users")
                .document(userId)
                .collection("activeCourses")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    courses = querySnapshot.documents.map { document ->
                        document.data?.toMutableMap() ?: mutableMapOf<String, Any>().apply {
                            put("courseName", document.getString("courseName") ?: "Untitled")
                            put("modulesComplete", document.getLong("modulesComplete")?.toInt() ?: 0)
                            put("numberOfModules", document.getLong("numberOfModules")?.toInt() ?: 1)
                        }
                    }
                }
        }
    }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("friends")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    friends = querySnapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val points = doc.getLong("points")?.toInt() ?: 0
                        name to points
                    }.sortedByDescending { it.second }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to fetch friends", e)
                }
        }
    }

    Scaffold (
        bottomBar = { BottomNavBar(navController) }
    ){ innerPadding ->
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
                Spacer(modifier = Modifier.height(16.dp))

                LeaderboardSection(friends = friends)
            }
        }
    }
}

@Composable
fun LeaderboardItem(position: Int, name: String, points: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$position.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "$points points",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LeaderboardSection(friends: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        friends.forEachIndexed { index, (name, points) ->
            LeaderboardItem(
                position = index + 1,
                name = name,
                points = points,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Text(
            text = "View all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
