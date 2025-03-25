package com.example.programmeringskurstilmorilddataba

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight

@Composable
fun UserUIScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var courses by remember { mutableStateOf(listOf<DocumentSnapshot>()) }
    var userName by remember { mutableStateOf("") }
    val userId = currentUser?.uid
    if (userId != null) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                userName = document.getString("name") ?: ""
                Log.d("Firestore", "User Name: $userName")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch user name", e)
            }
    }
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            getCoursesUser(db, userId) { fetchedCourses ->
                courses = fetchedCourses
                fetchedCourses.forEach { doc ->
                    Log.d("Firestore", "Fetched Course: ${doc.data}")
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Welcome, ${userName}!",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                ActiveCourseList(db)


                Spacer(modifier = Modifier.height(8.dp))
            }
        BottomNavBar(navController)
    }
}

/*
@Composable
fun Data() {
    val myCardData = listOf(
        CardData("Card 1", "Description 1", R.drawable.ic_launcher_foreground),
        CardData("Card 2", "Description 2", R.drawable.ic_launcher_foreground),
        CardData("Card 3", "Description 3", R.drawable.ic_launcher_foreground),
        CardData("Card 4", "Description 4", R.drawable.ic_launcher_foreground),
        CardData("Card 5", "Description 5", R.drawable.ic_launcher_foreground),
        CardData("Card 6", "Description 6", R.drawable.ic_launcher_foreground),
        CardData("Card 7", "Description 7", R.drawable.ic_launcher_foreground),
        CardData("Card 8", "Description 8", R.drawable.ic_launcher_foreground),
    )
    HorizontalCardList(cardDataList = myCardData)
}
*/

@Composable
fun UserProfile(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var errorMessage by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userIsAdmin by remember { mutableStateOf(false) }
    var userPassword by remember { mutableStateOf("") }
    currentUser?.let {
        db.collection("users").document(it.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userName = document.getString("name") ?: ""
                    userEmail = document.getString("email") ?: ""
                    userIsAdmin = document.getBoolean("isAdmin") ?: false
                    userPassword = document.getString("password") ?: ""
                } else {
                    errorMessage = "User data not found."
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to fetch user data: ${e.message}"
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userEmail,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        BottomNavBar(navController)
    }
}

@Composable
fun UserCourses(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val coursesMap = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        db.collection("courses")
            .get()
            .addOnSuccessListener { result ->
                val map = mutableMapOf<String, String>()
                for (document in result) {
                    val courseName = document.getString("courseName") ?: "No name"
                    val courseDescription = document.getString("courseDescription") ?: "No description"
                    map[courseName] = courseDescription
                }
                coursesMap.value = map
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "All Courses",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Convert map entries to list with index for LazyColumn
            val courseList = coursesMap.value.entries.toList()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(courseList) { index, (courseName, courseDescription) ->
                    CourseCard(
                        navController = navController,
                        courseName = courseName,
                        modulesComplete = 1,
                        numberOfModules = 4,
                        courseDescription = courseDescription
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCard(navController: NavController, courseName: String, modulesComplete: Int, numberOfModules: Int, courseDescription: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
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
                    text = courseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Modules complete $modulesComplete/$numberOfModules",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                ReadMoreButton(navController, courseName, courseDescription)
            }

            Box {
                CircularProgressIndicator(
                    progress = { modulesComplete.toFloat() / numberOfModules.toFloat() },
                    color = Color(0xFF6A0DAD),
                    trackColor = Color(0xFFE0B0FF),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "${((modulesComplete.toFloat()/numberOfModules.toFloat()) * 100).toInt()}%",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ReadMoreButton(navController: NavController, courseName: String, courseDescription: String) {
    var showDialog by remember { mutableStateOf(false) }

    Button(onClick = { showDialog = true }) {
        Text("Read More")
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFEDE7F6), shape = RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = courseDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "To pass any given workload, you require 80% passed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {
                                navController.navigate("courseModules/$courseName")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                        ) {
                            Text("Start course", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        TextButton(onClick = { showDialog = false }) {
                            Text("Back", color = Color(0xFF6A1B9A))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseModules(navController: NavController, courseName: String) {
    val db = FirebaseFirestore.getInstance()
    val moduleNames = remember { mutableStateOf<List<String>>(emptyList()) }
    val isPassed = remember { mutableStateOf(false) }

    LaunchedEffect(courseName) {
        db.collection("courses")
            .document(courseName)
            .collection("modules")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val names = querySnapshot.documents.map { it.getString("name") ?: "" }
                moduleNames.value = names
            }
    }
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "All modules",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
            )
        }

        if (moduleNames.value.isEmpty()) {
            item {
                Text(
                    text = "No modules found for this course",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
// Do not remove, Amir will
            item { ModuleCard(
                moduleName = "Object Oriented Programming",
                chaptersComplete = 2,
                numberOfChapters = 5,
                taskComplete = 10,
                numberOfTasks = 20
            ) }
//***************************************************************
            if (isPassed.value) {
            itemsIndexed(moduleNames.value) { index, name ->
                ModuleCard(
                    moduleName = name,
                    chaptersComplete = 2,
                    numberOfChapters = 5,
                    taskComplete = 10,
                    numberOfTasks = 20
                )
            }
            }
            else{
                itemsIndexed(moduleNames.value) { index, name ->
                    ModuleCardUnlock(
                        moduleName = name,
                        chaptersComplete = 2,
                        numberOfChapters = 5,
                        taskComplete = 10,
                        numberOfTasks = 20)
                }
            }
        }
    }
    }
}

@Composable
fun ModuleCard(moduleName: String, chaptersComplete: Int, numberOfChapters: Int,
               taskComplete: Int, numberOfTasks: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB084E8))

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moduleName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Chapters complete $chaptersComplete/$numberOfChapters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text="Task complete $taskComplete/$numberOfTasks",
                )
            }

            Box {
                CircularProgressIndicator(
                    progress = { chaptersComplete.toFloat() / numberOfChapters.toFloat() },
                    color = Color(0xFF6A0DAD),
                    trackColor = Color(0xFFE0B0FF),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "${((chaptersComplete.toFloat()/numberOfChapters.toFloat()) * 100).toInt()}%",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ModuleCardUnlock(moduleName: String, chaptersComplete: Int, numberOfChapters: Int,
                     taskComplete: Int, numberOfTasks: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.2f) // Make background content semi-transparent
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = moduleName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Chapters complete $chaptersComplete/$numberOfChapters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text="Task complete $taskComplete/$numberOfTasks",
                        )
                    }

                    Box {
                        CircularProgressIndicator(
                            progress = { chaptersComplete.toFloat() / numberOfChapters.toFloat() },
                            color = Color(0xFF6A0DAD),
                            trackColor = Color(0xFFE0B0FF),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "${((chaptersComplete.toFloat()/numberOfChapters.toFloat()) * 100).toInt()}%",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Foreground content (centered)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unlock?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun updateUser(db: FirebaseFirestore, userId: String, userName: String, userEmail: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val updatedUser = mapOf(
        "name" to userName,
        "email" to userEmail,
    )
    currentUser?.verifyBeforeUpdateEmail(userEmail)
    db.collection("users").document(userId).update(updatedUser)
        .addOnSuccessListener { Log.d("Firestore", "User Updated Successfully") }
        .addOnFailureListener { e -> Log.w("Firestore", "Error while updating user", e) }
}

fun getCoursesUser(db: FirebaseFirestore, userId: String, onResult: (List<DocumentSnapshot>) -> Unit) {
    db.collection("users").document(userId).collection("usercourses") // Target the subcollection
        .get()
        .addOnSuccessListener { documents ->
            onResult(documents.documents)
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching courses", e)
        }
}