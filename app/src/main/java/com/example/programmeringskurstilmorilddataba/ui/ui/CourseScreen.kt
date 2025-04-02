package com.example.programmeringskurstilmorilddataba.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.navigation.BottomNavBar
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserCourses(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val coursesMap = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        db.collection("courses")
            .get()
            .addOnSuccessListener { result ->
                val map = mutableMapOf<String, String>()
                for (document in result) {
                    val courseName = document.getString("courseName") ?: "No name"
                    val courseDescription = document.getString("courseDescription") ?: "No description"
                    map[courseName] = courseDescription
                }
                coursesMap.value = map
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "All Courses",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Convert map entries to list with index for LazyColumn
            val courseList = coursesMap.value.entries.toList()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(courseList) { index, (courseName, courseDescription) ->
                    CourseCard(
                        navController = navController,
                        courseName = courseName,
                        modulesComplete = 1,
                        numberOfModules = 4,
                        courseDescription = courseDescription
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCard(navController: NavController, courseName: String, modulesComplete: Int, numberOfModules: Int, courseDescription: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB084E8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Modules complete $modulesComplete/$numberOfModules",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                ReadMoreButton(navController, courseName, courseDescription)
            }

            Box {
                CircularProgressIndicator(
                    progress = { modulesComplete.toFloat() / numberOfModules.toFloat() },
                    color = Color(0xFF6A0DAD),
                    trackColor = Color(0xFFE0B0FF),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "${((modulesComplete.toFloat()/numberOfModules.toFloat()) * 100).toInt()}%",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ReadMoreButton(navController: NavController, courseName: String, courseDescription: String) {
    var showDialog by remember { mutableStateOf(false) }

    Button(onClick = { showDialog = true }) {
        Text("Read More")
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFEDE7F6), shape = RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = courseDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "To pass any given workload, you require 80% passed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {
                                navController.navigate("courseModules/$courseName")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                        ) {
                            Text("Start course", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        TextButton(onClick = { showDialog = false }) {
                            Text("Back", color = Color(0xFF6A1B9A))
                        }
                    }
                }
            }
        }
    }
}