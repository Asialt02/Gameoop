package com.example.programmeringskurstilmorilddataba.ui.ui.admin

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.data.Chapter
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
    var error by remember { mutableStateOf<String?>(null) }

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