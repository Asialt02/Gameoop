package com.example.programmeringskurstilmorilddataba.ui.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.navigation.BottomNavBar
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CourseModules(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    val moduleNames = remember { mutableStateOf<List<String>>(emptyList()) }
    val isPassed = remember { mutableStateOf(true) }

    LaunchedEffect(courseName) {
        db.collection("courses")
            .document(courseName)
            .collection("modules")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val names = querySnapshot.documents.map { it.getString("name") ?: "" }
                moduleNames.value = names
            }
    }
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "All modules",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
                )
            }

            if (moduleNames.value.isEmpty()) {
                item {
                    Text(
                        text = "No modules found for this course",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
// Do not remove, Amir will
                item { ModuleCard(
                    navController = navController,
                    moduleName = "Object Oriented Programming",
                    courseName = "",
                    chaptersComplete = 2,
                    numberOfChapters = 5,
                    taskComplete = 10,
                    numberOfTasks = 20
                ) }
//***************************************************************
                if (isPassed.value) {
                    itemsIndexed(moduleNames.value) { index, name ->
                        ModuleCard(
                            navController = navController,
                            moduleName = name,
                            courseName = courseName,
                            chaptersComplete = 2,
                            numberOfChapters = 5,
                            taskComplete = 10,
                            numberOfTasks = 20
                        )
                    }
                }
                else{
                    itemsIndexed(moduleNames.value) { index, name ->
                        ModuleCardUnlock(
                            moduleName = name,
                            chaptersComplete = 0,
                            numberOfChapters = 5,
                            taskComplete = 0,
                            numberOfTasks = 20)
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(
    navController: NavController,
    courseName: String,
    moduleName: String,
    chaptersComplete: Int,
    numberOfChapters: Int,
    taskComplete: Int,
    numberOfTasks: Int)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB084E8)),
        onClick = {navController.navigate(Screen.UserChapters.createRoute(courseName, moduleName))}

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moduleName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Chapters complete $chaptersComplete/$numberOfChapters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text="Task complete $taskComplete/$numberOfTasks",
                )
            }

            Box {
                CircularProgressIndicator(
                    progress = { chaptersComplete.toFloat() / numberOfChapters.toFloat() },
                    color = Color(0xFF6A0DAD),
                    trackColor = Color(0xFFE0B0FF),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "${((chaptersComplete.toFloat()/numberOfChapters.toFloat()) * 100).toInt()}%",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ModuleCardUnlock(moduleName: String, chaptersComplete: Int, numberOfChapters: Int,
                     taskComplete: Int, numberOfTasks: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.2f) // Make background content semi-transparent
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = moduleName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Chapters complete $chaptersComplete/$numberOfChapters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text="Task complete $taskComplete/$numberOfTasks",
                        )
                    }

                    Box {
                        CircularProgressIndicator(
                            progress = { chaptersComplete.toFloat() / numberOfChapters.toFloat() },
                            color = Color(0xFF6A0DAD),
                            trackColor = Color(0xFFE0B0FF),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "${((chaptersComplete.toFloat()/numberOfChapters.toFloat()) * 100).toInt()}%",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Foreground content (centered)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unlock?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}