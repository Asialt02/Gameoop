package com.example.programmeringskurstilmorilddataba.ui.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.data.deleteCourse
import com.example.programmeringskurstilmorilddataba.data.difficultyColors
import com.example.programmeringskurstilmorilddataba.data.navigateToModuleEditorByName
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    var course by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var moduleNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseName) {
        db.collection("courses").document(courseName)
            .get()
            .addOnSuccessListener { document ->
                course = document
                isLoading = false
            }
            .addOnFailureListener { e ->
                error = e.message
                isLoading = false
            }

        db.collection("courses").document(courseName)
            .collection("modules")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }
                moduleNames = snapshots?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.getString("courseName") ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            course?.id?.let { id ->
                                deleteCourse(db, id) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, "Delete Course")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            course == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Course not found", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back to Dashboard")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Text(
                        course?.getString("courseDescription") ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        "Modules",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (moduleNames.isEmpty()) {
                        Text(
                            "No modules yet",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(moduleNames) { index, moduleName ->
                                ModuleCard(
                                    moduleName = moduleName,
                                    onClick = {
                                        navigateToModuleEditorByName(
                                            navController = navController,
                                            courseName = courseName,
                                            moduleName = moduleName // The current name of the module
                                        )

                                        /*
                                        navController.navigate(
                                            Screen.ModuleEditorScreen.createRoute(
                                                courseName = courseName,
                                                moduleId = moduleName
                                            )
                                        )*/
                                    },
                                    onDelete = {
                                        db.collection("courses")
                                            .document(courseName)
                                            .collection("modules")
                                            .whereEqualTo("name", moduleName) // Query by name
                                            .get()
                                            .addOnSuccessListener { querySnapshot ->
                                                if (!querySnapshot.isEmpty) {
                                                    val document = querySnapshot.documents[0]
                                                    val moduleId = document.id
                                                    db.collection("courses")
                                                        .document(courseName)
                                                        .collection("modules")
                                                        .document(moduleId)
                                                        .delete()
                                                } else {
                                                    // Handle case where module with that name is not found
                                                    println("Module with name '$moduleName' not found for deletion.")
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                // Handle query failure
                                                println("Error fetching module ID for deletion: $exception")
                                            }

                                        /*
                                        db.collection("courses")
                                            .document(courseName)
                                            .collection("modules")
                                            .document(moduleName)
                                            .delete()

                                         */
                                    }
                                )
                            }
                        }
                    }

                    AddModuleSection(courseName)
                }
            }
        }
    }
}

@Composable
fun ModuleCard(
    moduleName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = moduleName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete module",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddModuleSection(courseName: String) {
    var newModuleName by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    var selectedDifficulty by remember { mutableStateOf("beginner") }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = newModuleName,
            onValueChange = { newModuleName = it },
            label = { Text("New Module Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Select Difficulty:")
        DifficultySelector { difficulty ->
            selectedDifficulty = difficulty
        }

        Button(
            onClick = {
                if (newModuleName.isNotBlank()) {
                    val selectedColor = difficultyColors[selectedDifficulty]
                    val moduleData = mapOf(
                        "name" to newModuleName,
                        "difficulty" to selectedDifficulty,
                        "difficultyColor" to selectedColor
                    )
                    db.collection("courses")
                        .document(courseName)
                        .collection("modules")
                        .document(newModuleName)
                        .set(moduleData)
                    newModuleName = ""
                    selectedDifficulty = "beginner"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = newModuleName.isNotBlank()
        ) {
            Text("Add Module")
        }
    }
}

@Composable
fun DifficultySelector(onDifficultySelected: (String) -> Unit) {
    var selectedDifficulty by remember { mutableStateOf("") }

    Column {
        difficultyColors.forEach { (difficulty, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedDifficulty == difficulty,
                    onClick = {
                        selectedDifficulty = difficulty
                        onDifficultySelected(difficulty)
                    }
                )
                Text(text = difficulty.replaceFirstChar { it.uppercase() })
            }
        }
    }
}