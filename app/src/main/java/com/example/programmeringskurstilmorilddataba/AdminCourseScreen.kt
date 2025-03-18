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
    var courses by remember { mutableStateOf(listOf<DocumentSnapshot>()) }
    var courseName by remember { mutableStateOf("") }  // For course name
    var courseDescription by remember { mutableStateOf("") } // For course description
    var courseIdToUpdate by remember { mutableStateOf("") } // To store the ID of the course to update
    var newModule by remember { mutableStateOf("") } // To store the new module
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

    // If the user is an admin, show the admin UI
    if (isAdmin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Input field for course name
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Input field for course description
            OutlinedTextField(
                value = courseDescription,
                onValueChange = { courseDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Button to add a course
            Button(onClick = {
                if (courseName.isNotBlank() && courseDescription.isNotBlank()) {
                    addCourse(db, courseName, courseDescription)
                } else {
                    Log.w("AdminCourseScreen", "Course name or description is empty.")
                }
            }) {
                Text("Add Course")
            }

            // Button to get and display the specific course by name
            Button(onClick = {
                if (courseName.isNotBlank()) {
                    getCourseByName(db, courseName) { course ->
                        if (course != null) {
                            courses = listOf(course)
                        } else {
                            Log.w("AdminCourseScreen", "No course found with the name: $courseName")
                        }
                    }
                } else {
                    Log.w("AdminCourseScreen", "Course name is empty.")
                }
            }) {
                Text("Get Course")
            }

            // Displaying the selected course
            courses.forEach { doc ->
                val courseId = doc.id // Get the document ID for each course
                val name = doc.getString("courseName") ?: "Unknown"
                val description = doc.getString("description") ?: "No Description"
                val modules = doc.get("modules") as? List<String> ?: emptyList()

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("$name: $description")

                    // Show modules (if available)
                    if (modules.isNotEmpty()) {
                        Text("Modules: ${modules.joinToString(", ")}")
                    } else {
                        Text("No modules added yet.")
                    }

                    // Adding a module
                    OutlinedTextField(
                        value = newModule,
                        onValueChange = { newModule = it },
                        label = { Text("New Module") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (newModule.isNotBlank()) {
                                addModuleToCourse(db, courseId, newModule)
                            }
                        }
                    ) {
                        Text("Add Module")
                    }

                    // Button to update the course
                    Button(
                        onClick = {
                            courseIdToUpdate = courseId
                            courseName = name
                            courseDescription = description
                        }
                    ) {
                        Text("Update Course")
                    }

                    // Button to delete the course
                    Button(
                        onClick = {
                            deleteCourse(db, courseId) {}
                        }
                    ) {
                        Text("Delete Course")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // If a course is selected for update, display update form
            if (courseIdToUpdate.isNotEmpty()) {
                Text("Updating Course")

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Updated Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = courseDescription,
                    onValueChange = { courseDescription = it },
                    label = { Text("Updated Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    updateCourse(db, courseIdToUpdate, courseName, courseDescription)
                    courseIdToUpdate = "" // Reset update course
                }) {
                    Text("Update Course")
                }
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

// Function to get a course by its name
fun getCourseByName(db: FirebaseFirestore, courseName: String, onResult: (DocumentSnapshot?) -> Unit) {
    db.collection("courses")
        .whereEqualTo("courseName", courseName)  // Query for the course by its name
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Get the first document (assuming course name is unique)
                onResult(querySnapshot.documents.first())
            } else {
                onResult(null)  // No course found
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching course by name", e)
            onResult(null)  // Handle error and return null
        }
}

fun addCourse(db: FirebaseFirestore, courseName: String, description: String) {
    val course = hashMapOf(
        "courseName" to courseName,
        "description" to description,
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

fun updateCourse(db: FirebaseFirestore, courseId: String, courseName: String, description: String) {
    val updatedCourse = mapOf(
        "courseName" to courseName,
        "description" to description
    )
    db.collection("courses").document(courseId).update(updatedCourse)
        .addOnSuccessListener { Log.d("Firestore", "Course Updated") }
        .addOnFailureListener { e -> Log.w("Firestore", "Error while updating Course", e) }
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

fun getCourses(db: FirebaseFirestore, onResult: (List<DocumentSnapshot>) -> Unit) {
    db.collection("courses").get()
        .addOnSuccessListener { documents ->
            onResult(documents.documents)
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching courses", e)
        }
}

data class Course(
    val courseName: String = "",
    val description: String = "",
    val modules: MutableList<String> = mutableListOf()  // Mutable list of modules
)