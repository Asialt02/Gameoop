package com.example.programmeringskurstilmorilddataba

import android.graphics.fonts.FontStyle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
<<<<<<< Updated upstream
import com.google.firebase.firestore.FieldValue
=======
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
>>>>>>> Stashed changes

@Composable
fun AdminCourseScreen(navController: NavController) {
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
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("Course Name") },
            modifier = Modifier.fillMaxWidth()
        )

<<<<<<< Updated upstream
        Button(onClick = {
            if (courseName.isNotBlank()) {
                addCourse(db, courseName)
            } else {
                Log.w("AdminCourseScreen", "Course name or description is empty.")
            }
        }) {
            Text("Add Course")
=======
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=24.dp)
        ){
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

            Button(onClick = {
                auth.signOut()
                navController.navigate("login")
            }) {
                Text("Log Out")
            }
>>>>>>> Stashed changes
        }

        Button(onClick = {

        },
            modifier = Modifier
                .padding(top = 36.dp)
                .fillMaxWidth(0.8f)
        ){
            Text("Moderate Users")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {

        },
            modifier = Modifier.fillMaxWidth(0.8f)
        ){
            Text("View analytics")
        }
    }
}

@Composable
<<<<<<< Updated upstream
fun CourseScreen(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    var course by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var newModule by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var courseIdToUpdate by remember { mutableStateOf("") }
=======
fun CourseInputDialog() {
    var showDialog by remember { mutableStateOf(false) }

    var courseId by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }

    Button(
        onClick = { showDialog = true },
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
    var moduleNames by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
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
=======
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 50.dp)) {

                    Button(onClick = {
                        if (courseName.isNotBlank()) {
                            navController.navigate("updateCourse/$courseName")
                        } else {
                            Log.w("AdminCourseScreen", "Course name is empty.")
>>>>>>> Stashed changes
                        }
                    ) {
                        Text("Add Module")
                    }
<<<<<<< Updated upstream

                    Button(
                        onClick = {
                            course?.id?.let { id ->
                                courseIdToUpdate = id
                                courseDescription = course?.getString("description") ?: ""
                            }
                        }
=======
>>>>>>> Stashed changes
                    ) {
                        Text("Update Course")
                    }

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
                    {
                        Text("Get Back")
                    }
                }
            }

        }
    }

<<<<<<< Updated upstream

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
=======
@Composable
fun UpdateCourse(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    var newModule by remember { mutableStateOf("") }
    var moduleId by remember { mutableStateOf("") }
    var courseDescription by remember { mutableStateOf("") }
    var moduleNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseName) {
        db.collection("courses")
            .document(courseName)
            .get()
            .addOnSuccessListener { document ->
                courseDescription = document.getString("courseDescription") ?: ""
            }

        db.collection("courses")
            .document(courseName)
            .collection("modules")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    error = "Error listening to modules: ${e.message}"
                    return@addSnapshotListener
                }

                moduleNames = querySnapshot?.documents
                    ?.mapNotNull { it.getString("name") }
                    ?: emptyList()
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center

    ) {
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = courseDescription,
            onValueChange = { courseDescription = it },
            label = { Text("Course Description") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top=150.dp)
        )

        Button(
            onClick = {
                if (courseDescription.isNotBlank()) {
                    db.collection("courses")
                        .document(courseName)
                        .update("courseDescription", courseDescription)
                        .addOnFailureListener { e ->
                            error = "Failed to update description: ${e.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Description")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Modules:",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (moduleNames.isEmpty()) {
                Text("No modules yet", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(moduleNames.size) { index ->
                        Text(
                            text = "- ${moduleNames[index]}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = moduleId,
            onValueChange = { moduleId = it },
            label = { Text("Module ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newModule,
            onValueChange = { newModule = it },
            label = { Text("Module Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (newModule.isNotBlank() && moduleId.isNotBlank()) {
                        addModuleToCourse(db, courseName, newModule, moduleId) { success, e ->
                            if (success) {
                                newModule = ""
                                moduleId = ""
                                error = null
                            } else {
                                error = e?.message ?: "Failed to add module"
                            }
                        }
                    } else {
                        error = "Please enter both module ID and name"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Module")
            }

            Button(
                onClick = {
                    if (moduleId.isNotBlank()) {
                        deleteModuleFromCourse(db, courseName, moduleId) { success, e ->
                            if (success) {
                                error = null
                            } else {
                                error = e?.message ?: "Failed to delete module"
                            }
                        }
                    } else {
                        error = "Please enter module ID to delete"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Delete Module")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (moduleId.isNotBlank()) {
                    navController.navigate("moduleEditorScreen/$courseName/$moduleId")
                } else {
                    error = "Please select a module first"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Module")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Courses")
        }
    }
}

@Composable
fun ModuleEditorScreen(
    navController: NavController,
    courseName: String,
    moduleId: String
) {
    val db = FirebaseFirestore.getInstance()
    var moduleTitle by remember { mutableStateOf("") }
    var newChapterTitle by remember { mutableStateOf("") }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var expandedChapterId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch module details and chapters
    LaunchedEffect(moduleId) {
        db.collection("courses")
            .document(courseName)
            .collection("modules")
            .document(moduleId)
            .get()
            .addOnSuccessListener { doc ->
                moduleTitle = doc.getString("name") ?: ""
            }

        db.collection("courses")
            .document(courseName)
            .collection("modules")
            .document(moduleId)
            .collection("chapters")
            .addSnapshotListener { snapshots, _ ->
                chapters = snapshots?.documents?.map { doc ->
                    Chapter(
                        id = doc.id,
                        title = doc.getString("title") ?: "",

                        )
                } ?: emptyList()
            }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Chapter") },
            text = { Text("Are you sure you want to delete this chapter?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.let { chapterId ->
                            deleteChapterFromModule(
                                db = db,
                                courseId = courseName,
                                moduleId = moduleId,
                                chapterId = chapterId,
                                onComplete = { success, e ->
                                    if (!success) {
                                        error = e?.message ?: "Failed to delete chapter"
                                    }
                                    showDeleteDialog = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Module header
        Text(
            text = moduleTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
        )
        Text(
            text = "Difficulty: Hard",
            style = MaterialTheme.typography.bodyMedium
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Add Chapter section
        OutlinedTextField(
            value = newChapterTitle,
            onValueChange = { newChapterTitle = it },
            label = { Text("New Chapter Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (newChapterTitle.isNotBlank()) {
                    val chapterData = hashMapOf(
                        "title" to newChapterTitle,
                        "introduction" to "", // Initialize empty introduction
                    )

                    db.collection("courses")
                        .document(courseName)
                        .collection("modules")
                        .document(moduleId)
                        .collection("chapters")
                        .add(chapterData)
                        .addOnSuccessListener {
                            newChapterTitle = ""
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Add Chapter")
        }

        // Chapters list
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(chapters.size) { index ->
                val chapter = chapters[index]
                ChapterItem(
                    chapter = chapter,
                    isExpanded = expandedChapterId == chapter.id,
                    onExpandToggle = {
                        expandedChapterId = if (expandedChapterId == chapter.id) null else chapter.id
                    },
                    onAddTask = { taskType, question ->
                        addTaskToChapter(db, courseName, moduleId, chapter.id, taskType, question)
                    },
                    onDeleteChapter = {
                        showDeleteDialog = chapter.id
                    },
                    onNavigateToView = {
                        navController.navigate("chapterViewScreen/$courseName/$moduleId/${chapter.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onAddTask: (String, String) -> Unit,
    onDeleteChapter: () -> Unit,
    onNavigateToView: () -> Unit
) {
    var taskType by remember { mutableStateOf("") }
    var taskQuestion by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDeleteChapter) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete chapter",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(onClick = onExpandToggle) {

                }
            }

            if (isExpanded) {
                // Task creation form
                OutlinedTextField(
                    value = taskType,
                    onValueChange = { taskType = it },
                    label = { Text("Task Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = taskQuestion,
                    onValueChange = { taskQuestion = it },
                    label = { Text("Question") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Button(
                    onClick = {
                        if (taskType.isNotBlank() && taskQuestion.isNotBlank()) {
                            onAddTask(taskType, taskQuestion)
                            taskType = ""
                            taskQuestion = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Add Task")
                }

                Button(
                    onClick = onNavigateToView,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("View Chapter Tasks")
                }
            }
        }
    }
}

@Composable
fun ChapterViewScreen(
    navController: NavController,
    courseId: String,
    moduleId: String,
    chapterId: String
) {
    val db = FirebaseFirestore.getInstance()
    var chapterTitle by remember { mutableStateOf("") }
    var introduction by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var newTaskQuestion by remember { mutableStateOf("") }
    var showEditIntro by remember { mutableStateOf(false) }
    var editedIntro by remember { mutableStateOf(introduction) }

    LaunchedEffect(chapterId) {
        // Single document listener for chapter data
        db.collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)
            .collection("chapters")
            .document(chapterId)
            .addSnapshotListener { doc, _ ->
                doc?.let {
                    chapterTitle = it.getString("title") ?: ""
                    introduction = it.getString("introduction") ?: ""

                    // Extract all task fields (assuming they start with "task")
                    tasks = it.data?.filterKeys { key ->
                        key.startsWith("task")
                    }?.mapValues { it.value.toString() } ?: emptyMap()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (showEditIntro) {
            AlertDialog(
                onDismissRequest = { showEditIntro = false },
                title = { Text("Edit Introduction") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editedIntro,
                            onValueChange = { editedIntro = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Chapter Introduction") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            db.collection("courses")
                                .document(courseId)
                                .collection("modules")
                                .document(moduleId)
                                .collection("chapters")
                                .document(chapterId)
                                .update("introduction", editedIntro)
                            showEditIntro = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditIntro = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        // Chapter header
        Text(
            text = chapterTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Introduction section
        Text(
            text = "Introduction",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = introduction.ifEmpty { "No introduction yet" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    editedIntro = introduction
                    showEditIntro = true
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit introduction"
                )
            }
        }

        // Tasks section
        Text(
            text = "Tasks",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Add new task
        OutlinedTextField(
            value = newTaskQuestion,
            onValueChange = { newTaskQuestion = it },
            label = { Text("New Task Question") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (newTaskQuestion.isNotBlank()) {
                    // Generate new task ID (task1, task2, etc.)
                    val newTaskId = "task${tasks.size + 1}"

                    db.collection("courses")
                        .document(courseId)
                        .collection("modules")
                        .document(moduleId)
                        .collection("chapters")
                        .document(chapterId)
                        .update(newTaskId, newTaskQuestion)
                        .addOnSuccessListener {
                            newTaskQuestion = ""
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }

        // Tasks list
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(tasks.entries.toList()) { index, (taskId, question) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Display task number using index
                        Text(
                            text = "Task ${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Edit/Delete buttons
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { /* Handle edit */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit task"
                                )
                            }
                            IconButton(
                                onClick = { /* Handle delete */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Module")
        }
    }
}

@Composable
fun TaskItem(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.type,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = task.question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
>>>>>>> Stashed changes
