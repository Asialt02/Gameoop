package com.example.programmeringskurstilmorilddataba

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.programmeringskurstilmorilddataba.ui.theme.AdvancedDiffColor
import com.example.programmeringskurstilmorilddataba.ui.theme.BeginnerDiffColor
import com.example.programmeringskurstilmorilddataba.ui.theme.InfoCardColor
import com.example.programmeringskurstilmorilddataba.ui.theme.IntermediateDiffColor
import com.example.programmeringskurstilmorilddataba.ui.theme.PlaceholderDiffColor
import com.example.programmeringskurstilmorilddataba.ui.theme.PrimaryPurple


@Composable
fun CoursesScreen(
    navController: NavController,
    onCourseClick: (Int) -> Unit = {}
) {

    val allCourses = DummyCourse().loadMultiCoursesInformation()

    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogContent by remember { mutableStateOf(CourseInformation()) }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(PrimaryPurple)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.all_courses),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.SansSerif
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(0.dp)
            ) {
                for (course in allCourses) {
                    item {
                        CourseListItem(
                            course = course,
                            onCourseClick = { onCourseClick(course.id) },
                            showCourseInfo = {
                                infoDialogContent = course
                                showInfoDialog = true
                            },
                            overlay = {
                                if (!course.unlocked) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(32.dp))
                                            .background(color = Color(0.5f, 0.5f, 0.5f, 0.5f))
                                            .matchParentSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "+ Unlock",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (showInfoDialog) {
                Dialog(onDismissRequest = { showInfoDialog = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(InfoCardColor)
                            .padding(16.dp)
                    ) {
                        StandardText(infoDialogContent.name)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = infoDialogContent.description,
                            color = PrimaryPurple
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = {
                                showInfoDialog = false; onCourseClick(
                                infoDialogContent.id
                            )
                            }) {
                                Text("Start Course")
                            }
                            OutlinedButton(onClick = { showInfoDialog = false }) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
        }

        BottomNavBar(navController)
    }
}

@Composable
fun CourseListItem(
    course: CourseInformation = DummyCourse().loadFirstCourseInformation(),
    onCourseClick: () -> Unit = {},
    showCourseInfo: () -> Unit = {},
    overlay: @Composable() (BoxScope.() -> Unit)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(InfoCardColor)
            .padding(16.dp)

    ) {
        Column (
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryPurple,
                modifier = Modifier.clickable(onClick = onCourseClick)
            )

            Row {
                Text(stringResource(R.string.modules_completed))
                Text(" ${course.completedModules} / ${course.noOfModules}")
            }

            OutlinedButton(
                onClick = showCourseInfo,
            ) {
                Text(stringResource(R.string.read_more))
            }
        }

        overlay()
    }
}

@Composable
fun ModulesScreen(
    navController: NavController,
    course: CourseInformation,
    onModuleClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(PrimaryPurple)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.all_modules),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.SansSerif
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(4.dp)
            ) {
                for (module in course.containedModules) {
                    item { ModuleListItem(module) }
                }
            }
        }

        BottomNavBar(navController)
    }
}


@Composable
fun ModuleListItem(module: ModuleInformation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(InfoCardColor)
            .padding(16.dp)

    ) {

        val difficultyColors = mapOf<String, Color>(
            "Beginner" to BeginnerDiffColor,
            "Intermediate" to IntermediateDiffColor,
            "Advanced" to AdvancedDiffColor,
            "Placeholder" to PlaceholderDiffColor
        )

        Column (
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = module.name,
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryPurple
            )

            Row {
                Text(
                    text = stringResource(R.string.chapters_completed)
                )
                Text(" ${module.completedChapters} / ${module.noOfChapters}")
            }

            Row {
                difficultyColors[module.difficulty]?.let { Text(text = "●", color = it) }
                Text(text = module.difficulty)
            }



        }
    }
}

@Composable
@Preview
fun CourseScreenPreview() {
    CoursesScreen(rememberNavController())
}

@Composable
@Preview
fun ModuleScreenPreview() {
    val exampleCourse = DummyCourse().loadFirstCourseInformation()

    ModulesScreen(rememberNavController(), exampleCourse)
}