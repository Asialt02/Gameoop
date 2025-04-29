package com.example.programmeringskurstilmorilddataba.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.data.Chapter
import com.example.programmeringskurstilmorilddataba.data.Module
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ChaptersScreen(
    navController: NavController,
    courseName: String,
    moduleId: String
) {
    val db = FirebaseFirestore.getInstance()
    var module by remember { mutableStateOf<Module?>(null) }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(moduleId) {
        db.collection("courses")
            .document(courseName)
            .collection("modules")
            .document(moduleId)
            .addSnapshotListener { doc, _ ->
                doc?.let {
                    module = Module(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        difficulty = doc.getString("difficulty") ?: "",
                        difficultyColor = doc.getString("difficultyColor") ?: ""
                    )
                }
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
                        introduction = doc.getString("introduction") ?: "",
                        level = doc.getLong("level")?.toInt() ?: 0
                    )
                } ?: emptyList()
            }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress Card at the Top
        ProgressCard()

        LazyColumn {
            itemsIndexed(chapters) { index, chapter ->
                ChapterCard(
                    chapterName = chapter.title,
                    level = chapter.level.toString(),
                    progress = "0 of 3",
                    tasks = listOf("Placeholder"),
                    onChapterClick = {
                        navController.navigate(Screen.UserTasks.createRoute(courseName, moduleId, chapter.id))
                    }
                )
            }
        }
    }
}


@Composable
fun CourseScreen(courses: List<DocumentSnapshot>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ProgressCard() // Can later be made dynamic

        courses.forEachIndexed { index, courseDoc ->
            val chapterName = courseDoc.getString("chapterName") ?: "Chapter ${index + 1}"
            val level = courseDoc.getString("level") ?: "LVL ${index + 1}"
            val progress = courseDoc.getString("progress") ?: "0 of 5"
            val tasks = (courseDoc.get("tasks") as? List<String>) ?: listOf("Task1", "Task2", "Task3")

//            ChapterCard(
//                chapterName = chapterName,
//                level = level,
//                progress = progress,
//                tasks = tasks
//            )
        }
    }
}

@Composable
fun ChapterCard(
    chapterName: String,
    level: String,
    progress: String,
    tasks: List<String>,
    onChapterClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD6B9FF)), // Light purple
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onChapterClick
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter Name
                Text(
                    text = chapterName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                // Level Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFA084E8), // Slightly darker purple
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "LVL $level",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }

                // Progress
                Text(
                    text = progress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(end = 8.dp)
                )

//                IconButton(onClick = { expanded = !expanded }) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
//                        contentDescription = "Go to chapter",
//                        tint = Color.Gray
//                    )
//                }
            }
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, bottom = 8.dp) // Indent tasks
                ) {
                    tasks.forEachIndexed { index, task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = index % 2 == 0, // Example: Mark every other task as completed
                                onCheckedChange = {}
                            )
                            Text(text = task, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB084E8)), // Purple
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Variables and Datatypes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Chapters complete 2/4\nTasks complete 10/20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Green, shape = CircleShape)
                    )
                    Text(
                        text = " Beginner",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Circular Progress Indicator (50% Complete)
            CircularProgressIndicator(
                progress = {
                    0.5f // 50% Completion
                },
                color = Color(0xFF6A0DAD),
                trackColor = Color(0xFFE0B0FF),
                strokeWidth = 6.dp,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}
