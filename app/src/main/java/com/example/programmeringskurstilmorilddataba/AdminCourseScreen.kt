package com.example.programmeringskurstilmorilddataba

import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminDashboard(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courseName by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp, top = 32.dp)
        )

        // Search Course Section
        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("Search Course") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (courseName.isNotBlank()) {
                    navController.navigate(Screen.CourseScreen.createRoute(courseName))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("View Course")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Admin Actions Section
        if (isAdmin) {
            Button(
                onClick = { showAddCourseDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add New Course")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                auth.signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { courseId, description ->
                addCourse(courseId, description)
                showAddCourseDialog = false
            }
        )
    }
}

@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var courseId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(courseId, description) },
                enabled = courseId.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

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
                                        navController.navigate(
                                            Screen.ModuleEditorScreen.createRoute(
                                                courseName = courseName,
                                                moduleId = moduleName
                                            )
                                        )
                                    },
                                    onDelete = {
                                        db.collection("courses")
                                            .document(courseName)
                                            .collection("modules")
                                            .document(moduleName)
                                            .delete()
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

    Column(modifier = Modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = newModuleName,
            onValueChange = { newModuleName = it },
            label = { Text("New Module Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (newModuleName.isNotBlank()) {
                    db.collection("courses")
                        .document(courseName)
                        .collection("modules")
                        .document(newModuleName)
                        .set(mapOf("name" to newModuleName))
                    newModuleName = ""
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleEditorScreen(
    navController: NavController,
    courseName: String,
    moduleId: String,
) {
    val db = FirebaseFirestore.getInstance()
    var moduleTitle by remember { mutableStateOf("") }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var expandedChapterId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null)}

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
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }
                chapters = snapshots?.documents?.map { doc ->
                    Chapter(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        introduction = doc.getString("introduction") ?: ""
                    )
                } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(moduleTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            AddChapterSection(courseName, moduleId)


            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(chapters) { index, chapter ->
                    ChapterItem(
                        chapter = chapter,
                        isExpanded = expandedChapterId == chapter.id,
                        onExpandToggle = {
                            expandedChapterId = if (expandedChapterId == chapter.id) null else chapter.id
                        },
                        onDeleteChapter = {
                            showDeleteDialog = chapter.id
                        },
                        onNavigateToView = {
                            navController.navigate(
                                Screen.ChapterViewScreen.createRoute(
                                    courseId = courseName,
                                    moduleId = moduleId,
                                    chapterId = chapter.id
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { chapterId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Chapter") },
            text = { Text("Are you sure you want to delete this chapter?") },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("courses")
                            .document(courseName)
                            .collection("modules")
                            .document(moduleId)
                            .collection("chapters")
                            .document(chapterId)
                            .delete()
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddChapterSection(courseName: String, moduleId: String) {
    val db = FirebaseFirestore.getInstance()
    var newChapterTitle by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
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
                        "introduction" to "",
                        "createdAt" to FieldValue.serverTimestamp()
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
                .padding(top = 8.dp),
            enabled = newChapterTitle.isNotBlank()
        ) {
            Text("Add Chapter")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterViewScreen(
    navController: NavController,
    courseId: String,
    moduleId: String,
    chapterId: String
) {
    val db = FirebaseFirestore.getInstance()
    var chapter by remember { mutableStateOf<Chapter?>(null) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var showEditIntroDialog by remember { mutableStateOf(false) }
    var editedIntro by remember { mutableStateOf("") }
    var newTaskQuestion by remember { mutableStateOf("") }

    LaunchedEffect(chapterId) {
        db.collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)
            .collection("chapters")
            .document(chapterId)
            .addSnapshotListener { doc, _ ->
                doc?.let {
                    chapter = Chapter(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        introduction = doc.getString("introduction") ?: ""
                    )
                }
            }

        db.collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)
            .collection("chapters")
            .document(chapterId)
            .collection("tasks")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                tasks = snapshots?.documents?.map { doc ->
                    Task(
                        id = doc.id,
                        question = doc.getString("question") ?: "",
                        options = emptyList()
                    )
                } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chapter?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                chapter == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Introduction",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        editedIntro = chapter?.introduction ?: ""
                                        showEditIntroDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, "Edit introduction")
                                }
                            }
                            Text(
                                chapter?.introduction ?: "No introduction yet",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(tasks) { index, task ->
                            TaskItem(
                                task = task,
                                onEdit = { /* Edit functionality */ },
                                onDelete = {
                                    db.collection("courses")
                                        .document(courseId)
                                        .collection("modules")
                                        .document(moduleId)
                                        .collection("chapters")
                                        .document(chapterId)
                                        .collection("tasks")
                                        .document(task.id)
                                        .delete()
                                },
                                onNavigateToOptions = {
                                    navController.navigate(
                                        Screen.TaskOptionsScreen.createRoute(
                                            courseId = courseId,
                                            moduleId = moduleId,
                                            chapterId = chapterId,
                                            taskId = task.id
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Add Task Section
                    OutlinedTextField(
                        value = newTaskQuestion,
                        onValueChange = { newTaskQuestion = it },
                        label = { Text("New Task Question") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newTaskQuestion.isNotBlank()) {
                                val newTaskId = "task${System.currentTimeMillis()}"
                                db.collection("courses")
                                    .document(courseId)
                                    .collection("modules")
                                    .document(moduleId)
                                    .collection("chapters")
                                    .document(chapterId)
                                    .collection("tasks")
                                    .document(newTaskId)
                                    .set(mapOf(
                                        "question" to newTaskQuestion
                                    ))
                                    .addOnSuccessListener {
                                        newTaskQuestion = ""
                                    }
                            }
                        }
                    ) {
                        Text("Add Task")
                    }
                }
            }
        }
    }

    if (showEditIntroDialog) {
        AlertDialog(
            onDismissRequest = { showEditIntroDialog = false },
            title = { Text("Edit Introduction") },
            text = {
                OutlinedTextField(
                    value = editedIntro,
                    onValueChange = { editedIntro = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Introduction") }
                )
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
                        showEditIntroDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditIntroDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToOptions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onNavigateToOptions)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit task")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error
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
    onDeleteChapter: () -> Unit,
    onNavigateToView: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = onNavigateToView
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = chapter.introduction.ifEmpty { "No introduction available" },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )


                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    IconButton(
                        onClick = onDeleteChapter,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete chapter",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))


                    Button(
                        onClick = onNavigateToView,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("View Chapter")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskOptionsScreen(
    navController: NavController,
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String
) {
    val db = FirebaseFirestore.getInstance()
    var options by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var newOptionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var taskQuestion by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        try {
            val doc = db.collection("courses")
                .document(courseId)
                .collection("modules")
                .document(moduleId)
                .collection("chapters")
                .document(chapterId)
                .get()
                .await()

            val taskData = doc.get(taskId) as? Map<String, Any>
            taskQuestion = taskData?.get("question") as? String ?: ""

            val optionsMap = taskData?.get("options") as? Map<String, Map<String, Any>> ?: emptyMap()
            options = optionsMap.values.mapNotNull {
                val text = it["text"] as? String ?: return@mapNotNull null
                val isCorrect = it["isCorrect"] as? Boolean ?: false
                text to isCorrect
            }.sortedBy { it.first }

            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load options: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Options") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = taskQuestion,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(options) { index, (option, isCorrect) ->
                        OptionItem(
                            option = option,
                            isCorrect = isCorrect,
                            onToggleCorrect = {
                                val updatedOptions = options.toMutableList().apply {
                                    this[index] = option to !isCorrect
                                }
                                scope.launch {
                                    saveOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        options = updatedOptions
                                    )
                                    options = updatedOptions
                                }
                            },
                            onDelete = {
                                val updatedOptions = options.toMutableList().apply {
                                    removeAt(index)
                                }
                                scope.launch {
                                    saveOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        options = updatedOptions
                                    )
                                    options = updatedOptions
                                }
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = newOptionText,
                    onValueChange = { newOptionText = it },
                    label = { Text("New option text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (newOptionText.isNotBlank()) {
                            val updatedOptions = options + (newOptionText to false)
                            scope.launch {
                                try {
                                    saveOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        options = updatedOptions
                                    )
                                    options = updatedOptions
                                    newOptionText = ""
                                } catch (e: Exception) {
                                    error = "Failed to add option: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Add Option")
                }
            }
        }
    }
}

@Composable
fun OptionItem(
    option: String,
    isCorrect: Boolean,
    onToggleCorrect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCorrect,
                onCheckedChange = { onToggleCorrect() }
            )
            Text(
                text = option,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}

fun saveOptions(
    db: FirebaseFirestore,
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String,
    taskQuestion: String,
    options: List<Pair<String, Boolean>>
) {
    val optionsMap = options.mapIndexed { index, (text, isCorrect) ->
        "option$index" to mapOf(
            "text" to text,
            "isCorrect" to isCorrect
        )
    }.toMap()

    val updateData = hashMapOf<String, Any>(
        "$taskId.question" to taskQuestion,
        "$taskId.options" to optionsMap
    )

    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .collection("chapters")
        .document(chapterId)
        .update(updateData)
        .addOnFailureListener { e ->
            Log.e("SaveOptions", "Error saving options", e)
        }
}

data class Chapter(
    val id: String,
    val title: String,
    val introduction: String
)

data class Task(
    val id: String,
    val question: String,
    val options: List<Option> = emptyList()
)

data class Option(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

fun addCourse(
    courseId: String,
    description: String,
    onComplete: (Exception?) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("courses")
        .document(courseId)
        .set(mapOf(
            "courseName" to courseId,
            "courseDescription" to description,
            "createdAt" to FieldValue.serverTimestamp()
        ))
        .addOnCompleteListener { onComplete(it.exception) }
}

fun deleteCourse(
    db: FirebaseFirestore,
    courseId: String,
    onComplete: () -> Unit
) {
    db.collection("courses")
        .document(courseId)
        .delete()
        .addOnSuccessListener { onComplete() }
}

fun addModuleToCourse(
    db: FirebaseFirestore,
    courseId: String,
    moduleName: String,
    moduleId: String,
    onComplete: (Exception?) -> Unit = {}
) {
    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .set(mapOf(
            "name" to moduleName,
            "createdAt" to FieldValue.serverTimestamp()
        ))
        .addOnCompleteListener { onComplete(it.exception) }
}