@file:Suppress("DEPRECATION")

package com.example.programmeringskurstilmorilddataba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.programmeringskurstilmorilddataba.data.BottomNavItem
import com.example.programmeringskurstilmorilddataba.data.checkEmailValidity
import com.example.programmeringskurstilmorilddataba.data.sendPasswordResetEmail
import com.example.programmeringskurstilmorilddataba.navigation.AppNavigation
import com.example.programmeringskurstilmorilddataba.ui.theme.ProgrammeringskursTilMorildDataBATheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.example.programmeringskurstilmorilddataba.ui.ui.AdminDashboard
import com.example.programmeringskurstilmorilddataba.ui.ui.ChapterViewScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.CourseModules
import com.example.programmeringskurstilmorilddataba.ui.ui.ModuleEditorScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.TaskOptionsScreen
import com.example.programmeringskurstilmorilddataba.ui.ui.UserCourses
import com.example.programmeringskurstilmorilddataba.ui.ui.UserProfile
import com.example.programmeringskurstilmorilddataba.ui.ui.UserUIScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            ProgrammeringskursTilMorildDataBATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    AppNavigation()
                }

            }
        }
    }
}






///San kode
/*
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

            ChapterCard(
                chapterName = chapterName,
                level = level,
                progress = progress,
                tasks = tasks
            )
        }
    }
}

@Composable
fun ChapterCard(chapterName: String, level: String, progress: String, tasks: List<String>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD6B9FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = chapterName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFA084E8),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = level,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
                Text(
                    text = progress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Toggle tasks",
                        tint = Color.Gray
                    )
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, bottom = 8.dp)
                ) {
                    tasks.forEachIndexed { index, task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = index % 2 == 0,
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB084E8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            CircularProgressIndicator(
                progress = { 0.5f },
                color = Color(0xFF6A0DAD),
                trackColor = Color(0xFFE0B0FF),
                strokeWidth = 6.dp,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}
*/
@Preview(showBackground = true)
@Composable
fun Preview() {
    ProgrammeringskursTilMorildDataBATheme {
        AppNavigation()
    }
}