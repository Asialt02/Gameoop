package com.example.programmeringskurstilmorilddataba.ui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ActiveCourseList(navController: NavController,
                     courses: List<Map<String,
                             Any>>
                     ) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Active Courses",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "View all",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(courses.size) { index ->
            val course = courses[index]
            ActiveCourseCard(
                navController = navController,
                courseName = course["courseName"] as? String ?: "Untitled",
                modulesComplete = (course["modulesComplete"] as? Int) ?: 5,
                numberOfModules = (course["numberOfModules"] as? Int) ?: 10
            )
        }
    }
}

@Composable
fun ActiveCourseCard(
    navController: NavController,
    courseName: String,
    modulesComplete: Int,
    numberOfModules: Int
) {
    Card(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .height(300.dp)
            .width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB084E8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = {
            navController.navigate("courseModules/$courseName")
        }
    ) {

        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = courseName,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Modules completed",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$modulesComplete/$numberOfModules",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

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

