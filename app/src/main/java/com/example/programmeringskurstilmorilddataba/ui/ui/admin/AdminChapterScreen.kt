package com.example.programmeringskurstilmorilddataba.ui.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.data.Chapter
import com.example.programmeringskurstilmorilddataba.data.Task
import com.example.programmeringskurstilmorilddataba.data.TaskType
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

//Line 153, missing dialog logic?

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
    var showEditTaskDialog by remember { mutableStateOf<Task?>(null) }
    var editedTaskQuestion by remember { mutableStateOf("") }

    var showTaskTypeDialog by remember { mutableStateOf(false) }
    var newTaskType by remember { mutableStateOf("Multiple Choice") }

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
                        type = TaskType.fromString(doc.getString("type") ?: "Multiple Choice"),
                        options = emptyList() // You might want to load options here
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
                                onEdit = { taskToEdit ->
                                    editedTaskQuestion = taskToEdit.question
                                    showEditTaskDialog = taskToEdit
                                },
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
                    Text(
                        text = "Task type: $newTaskType"
                    )
                    Row {
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
                                        .set(
                                            mapOf(
                                                "question" to newTaskQuestion,
                                                "type" to newTaskType
                                            )
                                        )
                                        .addOnSuccessListener {
                                            newTaskQuestion = ""
                                        }
                                }
                            }
                        ) {
                            Text("Add Task")
                        }

                        Button(
                            onClick = { showTaskTypeDialog = true }
                        ) {
                            Text("Set task type")
                        }
                    }
                }
            }
        }
    }

    if (showEditTaskDialog != null) {
        AlertDialog(
            onDismissRequest = { showEditTaskDialog = null },
            title = { Text("Edit Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedTaskQuestion,
                        onValueChange = { editedTaskQuestion = it },
                        label = { Text("Task Question") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditTaskDialog?.let { task ->
                            db.collection("courses")
                                .document(courseId)
                                .collection("modules")
                                .document(moduleId)
                                .collection("chapters")
                                .document(chapterId)
                                .collection("tasks")
                                .document(task.id)
                                .update("question", editedTaskQuestion)
                                .addOnSuccessListener {
                                    showEditTaskDialog = null
                                }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTaskDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTaskTypeDialog) {
        Dialog(
            onDismissRequest = { showTaskTypeDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text("Select task type")
                TaskTypeItem(
                    text = "Multiple Choice",
                    onClick = { newTaskType = "Multiple Choice"; showTaskTypeDialog = false })
                TaskTypeItem(
                    text = "Drop Down",
                    onClick = { newTaskType = "Drop Down"; showTaskTypeDialog = false })
                TaskTypeItem(
                    text = "Yes / No",
                    onClick = { newTaskType = "Yes/No"; showTaskTypeDialog = false })
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onEdit: (Task) -> Unit,
    onDelete: () -> Unit,
    onNavigateToOptions: () -> Unit
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
            Column (
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onNavigateToOptions)
            ) {
                Text(
                    text = task.question,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = task.type.typeName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { onEdit(task) }) {
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
fun TaskTypeItem(
    text: String,
    onClick: () -> Unit,
) {
    Row (
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Text(text)
    }
}