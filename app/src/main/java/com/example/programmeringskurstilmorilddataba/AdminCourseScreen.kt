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
import com.google.firebase.firestore.FieldValue

@Composable
fun AdminCourseScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courseName by remember { mutableStateOf("") }  // For course name
    var isAdmin by remember { mutableStateOf(false) }

    // Checking if the current user is an admin
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

            Button(onClick = {
                if (courseName.isNotBlank()) {
                    addCourse(db, courseName)
                } else {
                    Log.w("AdminCourseScreen", "Course name or description is empty.")
                }
            }) {
                Text("Add Course")
            }

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
    } else {
        Text("You do not have permission to access this screen.", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun CourseScreen(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    var course by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var newModule by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var courseIdToUpdate by remember { mutableStateOf("") }

    LaunchedEffect(courseName) {
        getCourseByName(db, courseName) { fetchedCourse ->
            course = fetchedCourse
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
            val name = course!!.getString("courseName") ?: "Unknown"
            val description = course!!.getString("description") ?: "No Description"
            val modules = course!!.get("modules") as? List<String> ?: emptyList()

            Text(text = "Course Name: $name", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Description: $description", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Modules:", style = MaterialTheme.typography.headlineSmall)
            modules.forEach { module ->
                Text(text = "- $module", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("$name: $description")

                    if (modules.isNotEmpty()) {
                        Text("Modules: ${modules.joinToString(", ")}")
                    } else {
                        Text("No modules added yet.")
                    }

                    OutlinedTextField(
                        value = newModule,
                        onValueChange = { newModule = it },
                        label = { Text("New Module") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            course?.id?.let { id ->
                                if (newModule.isNotBlank()) {
                                    addModuleToCourse(db, id, newModule)
                                }
                            }
                        }
                    ) {
                        Text("Add Module")
                    }

                    Button(
                        onClick = {
                            course?.id?.let { id ->
                                courseIdToUpdate = id
                                courseDescription = course?.getString("description") ?: ""
                            }
                        }
                    ) {
                        Text("Update Course")
                    }

                    // Button to delete the course
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


// Function to get a course by its name
fun getCourseByName(db: FirebaseFirestore, courseName: String, onResult: (DocumentSnapshot?) -> Unit) {
    db.collection("courses")
        .whereEqualTo("courseName", courseName)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                onResult(querySnapshot.documents.first())
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching course by name", e)
            onResult(null)
        }
}

fun addCourse(db: FirebaseFirestore, courseName: String) {
    val course = hashMapOf(
        "courseName" to courseName,
        "modules" to listOf<String>() // Initialize an empty list for modules
    ) as MutableMap<String, Any>

    db.collection("courses").add(course)
        .addOnSuccessListener {
            Log.d("Firestore", "Course Added")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error while saving Course", e)
        }
}

fun addModuleToCourse(db: FirebaseFirestore, courseId: String, newModule: String) {
    db.collection("courses").document(courseId).update(
        "modules", FieldValue.arrayUnion(newModule) // Add module to the "modules" array
    )
        .addOnSuccessListener { Log.d("Firestore", "Module Added") }
        .addOnFailureListener { e -> Log.w("Firestore", "Error while adding module", e) }
}

fun deleteCourse(db: FirebaseFirestore, courseId: String, onSuccess: () -> Unit) {
    db.collection("courses").document(courseId).delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Course Deleted")
            onSuccess()  // Call the success handler to navigate
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error while deleting Course", e)
        }
}