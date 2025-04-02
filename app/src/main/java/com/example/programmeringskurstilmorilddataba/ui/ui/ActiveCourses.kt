package com.example.programmeringskurstilmorilddataba.ui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.programmeringskurstilmorilddataba.data.Course
import com.example.programmeringskurstilmorilddataba.data.getActiveCoursesForCurrentUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ActiveCourseList(db: FirebaseFirestore) {
    var courses by remember { mutableStateOf(listOf<Course>()) }
    Column(modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(textAlign = TextAlign.Center,
                text = "Active Courses:",
                style = MaterialTheme.typography.titleLarge,
            )
            TextButton(onClick = { /* Handle view all button click */ }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "View All")
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "View All"
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            getActiveCoursesForCurrentUser(db) { fetchedCourses ->
                courses = fetchedCourses
            }
        }

        if (courses.isNotEmpty()) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .height(260.dp),
                horizontalArrangement = Arrangement.Center,
                //verticalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(courses) { course ->
                    CourseCard(course = course)
                }
            }
        } else {
            Text(text = "No active courses",
                textAlign = TextAlign.Center,)
        }
    }
}

@Composable
fun CourseCard(course: Course) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .height(15.dp)
            .width(200.dp),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = course.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(text = "Completed Modules: ${course.completedModules}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center)
        }
    }
}


































/*
data class Course(val title: String = "", val description: String = "", val completedModules: Int = 0)


@Composable
fun ActiveCourses(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    //var documentData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var course by remember { mutableStateOf<Course?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val docRef = db.collection("users").document("$userId").collection("course").document("oop")
    docRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val title = document.getString("title")
                val description = document.getString("desc")
                val completedModules = document.getString("completed_mod")
                course = Course(title ?: "", description ?: "", completedModules?.toInt() ?: 0)
                Log.d("Firestore", "DocumentSnapshot data: ${document.data}")
            } else {
                errorMessage = "No such document"
                Log.d(TAG, "No such document")
            }
        }
        .addOnFailureListener { exception ->
            errorMessage = "get failed with ${exception.message}"
            Log.d(TAG, "get failed with ", exception)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text(text = "Error: $errorMessage")
        } else if (course != null) {
            HorizontalCardList(course = listOf(course!!))


        } else {
            Text(text = "Loading...")
        }
    }
}


@Composable
fun HorizontalCardList(course: List<Course>) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1), // We want a single row for horizontal scrolling
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(300.dp),
        contentPadding = PaddingValues(horizontal = 16.dp) // Add padding to the start and end
    ) {
        items(course) { cardData ->
            CardItem(cardData = cardData)
        }
    }
}

@Composable
fun CardItem(cardData: Course) {
    Card(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cardData.title)
            Text(text = cardData.description)
            Text(text = "Completed Modules: ${cardData.completedModules} / 5")
        }
    }
}










*/







/*
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var courseName by remember { mutableStateOf("") }
    val courseInformation by remember { mutableStateOf("") }
    val information = currentUser?.uid

    db.collection("oopcourse").document(courseInformation).get()
        .addOnSuccessListener { document ->
            courseName = document.getString("name") ?: ""

        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Failed to fetch user name", e)
        }
    Column() {
        Text(text = courseName)
    }
*/


