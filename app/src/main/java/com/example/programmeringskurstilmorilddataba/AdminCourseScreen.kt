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
fun AdminDashboard(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courseName by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    isAdmin = document.getBoolean("isAdmin") ?: false
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
        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("Course Name") },
            modifier = Modifier.fillMaxWidth()
        )

        CourseInputDialog()

        Button(onClick = {
            if (courseName.isNotBlank()) {
                navController.navigate("courseScreen/$courseName")
            } else {
                Log.w("AdminCourseScreen", "Course name is empty.")
            }
        }) {
            Text("Get Course")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            auth.signOut()
            navController.navigate("login")
        }) {
            Text("Log Out")
        }
    }
}

@Composable
fun CourseInputDialog() {
    var showDialog by remember { mutableStateOf(false) }

    var courseId by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Add Course")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                courseId = ""
                courseDescription = ""
            },
            title = { Text("Add New Course") },
            text = {
                Column {
                    OutlinedTextField(
                        value = courseId,
                        onValueChange = { courseId = it },
                        label = { Text("Course ID *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = courseDescription,
                        onValueChange = { courseDescription = it },
                        label = { Text("Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        addCourse(courseId, courseDescription)
                        showDialog = false
                        courseId = ""
                        courseDescription = ""
                    },
                    enabled = courseId.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        courseId = ""
                        courseDescription = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CourseScreen(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    var course by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var newModule by remember { mutableStateOf("") }
    var moduleId by remember { mutableStateOf("") }
    var moduleNames by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseName) {
        getCourseByName(db, courseName) { fetchedCourse ->
            course = fetchedCourse
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (course == null) {
            Text("No course found for: $courseName", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navController.popBackStack()
            })
            {Text("Head Back")}
        } else {
            val name = course!!.getString("courseName")
            val description = course!!.getString("courseDescription")

            db.collection("courses")
                .document(courseName)
                .collection("modules")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val names = querySnapshot.documents.mapNotNull { it.getString("name") }
                    moduleNames = names
                }
                .addOnFailureListener { e ->
                    error = "Failed to load course: ${e.message}"
                    isLoading = false
                }

            Text(text = "$name",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp, vertical = 4.dp))
            Text(text = "$description",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp, vertical = 4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Modules:", style = MaterialTheme.typography.headlineSmall)
            moduleNames.forEach { module ->
                Text(text = "- $module",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {

                    OutlinedTextField(
                        value = moduleId,
                        onValueChange = { moduleId = it },
                        label = { Text("Module ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newModule,
                        onValueChange = { newModule = it },
                        label = { Text("New Module") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row {
                        Button(
                            onClick = {
                                course?.id?.let { id ->
                                    if (newModule.isNotBlank() && moduleId.isNotBlank()) {
                                        addModuleToCourse(db, id, newModule, moduleId)
                                    }
                                }
                            }
                        ) {
                            Text("Add Module")
                        }

                        Button(
                            onClick = {

                            }
                        ){
                            Text("Edit module",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding( ))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Button(
                        onClick = {
                            course?.id?.let { id ->
                                deleteCourse(db, id) {}
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text("Delete Course")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        navController.popBackStack()
                    })
                    {Text("Head Back")}
                }
            }

        }
    }