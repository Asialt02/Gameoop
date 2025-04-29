package com.example.programmeringskurstilmorilddataba.ui.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.data.Chapter
import com.example.programmeringskurstilmorilddataba.data.DropDownTask
import com.example.programmeringskurstilmorilddataba.data.Option
import com.example.programmeringskurstilmorilddataba.data.Task
import com.example.programmeringskurstilmorilddataba.data.TaskType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


// NOT FINISHED

@Composable
fun TaskScreen(
    navController: NavController,
    courseId: String,
    moduleId: String,
    chapterId: String
) {
    val db = FirebaseFirestore.getInstance()
    var chapter by remember { mutableStateOf<Chapter?>(null) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

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
                        introduction = doc.getString("introduction") ?: "",
                        level = doc.getLong("level")?.toInt() ?: 0
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


    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No tasks available in this chapter")
        }
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    var showIntroduction by remember { mutableStateOf(true) }
    var showSummary by remember { mutableStateOf(false) }
    var currentAnswer by remember { mutableStateOf<Boolean>(false) }
    var answers = remember { mutableStateListOf<Boolean>() }

    var currentTask by remember { mutableStateOf(tasks[currentIndex]) }

    if (showIntroduction) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            chapter?.let { IntroductionScreen(it.introduction) }
            Button(
                onClick = {showIntroduction = false}
            ) {
                Text("Continue")
            }
        }
    }
    else if (showSummary) {
        val correctCount = answers.count { it }
        val incorrectCount = answers.count { !it }
        SummaryScreen(correctCount, incorrectCount, navController = navController )
    } else {

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when(currentTask.type) {
                TaskType.DropDown ->
                    DropDownTaskScreen(
                        courseId = courseId,
                        moduleId = moduleId,
                        chapterId = chapterId,
                        taskId = currentTask.id,
                        onOptionChosen = { isCorrect ->
                            currentAnswer = isCorrect
                        }
                    )
                TaskType.MultipleChoice ->
                    MultipleChoiceTask(
                        courseId = courseId,
                        moduleId = moduleId,
                        chapterId = chapterId,
                        taskId = currentTask.id,
                        onOptionChosen = { isCorrect ->
                            currentAnswer = isCorrect
                        })
                TaskType.YesNo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                    ) {  }
            }
            Button(
                onClick = {
                    answers.add(currentAnswer)
                    currentAnswer = false
                    if (currentIndex < tasks.size - 1) {
                        currentIndex++
                        currentTask = tasks[currentIndex]
                    } else {
                        showSummary = true
                    }
                }
            ) {
                Text("Continue")
            }
        }

//        val taskMap = chapter.tasks!![taskKeys[currentIndex]] as? Map<*, *> ?: emptyMap<Any, Any>()
//        val optionsMap = taskMap["options"] as? Map<*, *> ?: emptyMap<Any, Any>()
//        val questionText = taskMap["question"]?.toString() ?: "No Question"
//
//        val options = optionsMap.mapNotNull { entry ->
//            val key = entry.key as? String ?: return@mapNotNull null
//            val opt = entry.value as? Map<*, *> ?: return@mapNotNull null
//            key to Option(
//                text = opt["text"].toString(),
//                isCorrect = opt["isCorrect"] as? Boolean ?: false
//            )
//        }.toMap()
//
//        val task = Task(
//            question = questionText,
//            options = options
//        )

//        TaskDetailScreen(
//            task = task,
//            onContinue = { isCorrect ->
//                answers.add(isCorrect)
//                if (isCorrect) {
//                    correctCount++
//                } else {
//                    incorrectCount++
//                }
//                if (currentIndex < taskKeys.size - 1) {
//                    currentIndex++
//                } else {
//                    showSummary = true
//                }
//            }
//        )
    }
}

@Composable
fun IntroductionScreen (
    introduction: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Spacer(modifier = Modifier.height(128.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text(
                        text = "Introduction",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = introduction,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceTask (
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String,
    onOptionChosen: (Boolean) -> Unit = {},
) {

    val db = FirebaseFirestore.getInstance()
    var taskQuestion by remember { mutableStateOf("") }
    var taskOptions by remember { mutableStateOf<List<Option>>(emptyList()) }
    var chosenOption by remember { mutableIntStateOf(-1) }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

            taskQuestion = db.collection("courses")
                .document(courseId)
                .collection("modules")
                .document(moduleId)
                .collection("chapters")
                .document(chapterId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()
                .get("question") as? String ?: ""

            val taskData = doc.get(taskId) as? Map<String, Any>

            val optionsMap = taskData?.get("options") as? Map<String, Map<String, Any>> ?: emptyMap()
            val tempList = optionsMap.mapNotNull {
                val id = it.key
                val text = it.value["text"] as? String ?: return@mapNotNull null
                val isCorrect = it.value["isCorrect"] as? Boolean ?: false
                id to (text to isCorrect)
            }.sortedBy { it.first }

            tempList.forEach { option ->
                taskOptions = taskOptions.plus(listOf(Option(id = option.first, text = option.second.first, isCorrect = option.second.second)))
            }

            println(taskOptions)

            chosenOption = -1

            isLoading = false

        } catch (e: Exception) {
            error = "Failed to load options: ${e.message}"
            isLoading = false
        }
    }


    Column (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Spacer(modifier = Modifier.height(128.dp))

        Card (
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = taskQuestion)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
        ) {
            taskOptions.forEachIndexed { index, option ->
                item {
                    Button(
                        onClick = { onOptionChosen(option.isCorrect); chosenOption = index },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (chosenOption == index) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(option.text)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DropDownTaskScreen (
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String,
    onOptionChosen: (Boolean) -> Unit = {},
) {
    val db = FirebaseFirestore.getInstance()
    var taskQuestion by remember { mutableStateOf("") }
    var taskOptions by remember { mutableStateOf<List<List<Option>>>(emptyList()) }

    var splitTaskQuestion by remember { mutableStateOf(emptyList<String>()) }

    var chosenOptions by remember { mutableStateOf(emptyList<Boolean>()) }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

            taskQuestion = db.collection("courses")
                .document(courseId)
                .collection("modules")
                .document(moduleId)
                .collection("chapters")
                .document(chapterId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()
                .get("question") as? String ?: ""

            splitTaskQuestion = taskQuestion.split("[OPTION]")


            val taskData = doc.get(taskId) as? Map<String, Any>

            val optionSetsMap = taskData?.get("optionSets") as? Map<String, Map<String, Map<String, Any>>> ?: emptyMap()
            val optionSets1 = optionSetsMap.toSortedMap().values.toList()

            taskOptions = emptyList()

            optionSets1.forEach { set ->
                val tempList = set.mapNotNull {
                    val id = it.key
                    val text = it.value["text"] as? String ?: return@mapNotNull null
                    val isCorrect = it.value["isCorrect"] as? Boolean ?: false
                    id to (text to isCorrect)
                }.sortedBy { it.first }

                var tempList1 = emptyList<Option>()
                tempList.forEach { option ->
                    tempList1 = tempList1.plus(listOf(Option(id = option.first, text = option.second.first, isCorrect = option.second.second)))
                }

                taskOptions = taskOptions.plus(listOf(tempList1.sortedBy { it.id }))

            }

            chosenOptions = taskOptions.map { it[0].isCorrect }

            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load options: ${e.message}"
            isLoading = false
        }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlowRow(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    splitTaskQuestion.forEachIndexed { index, content ->
                        content.split(" ").forEach {
                            Text(
                                text = "$it ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (index < taskOptions.size) {
                            DropDownTaskButton(
                                options = taskOptions[index],
                                onOptionChosen = { isCorrect ->
                                    chosenOptions = chosenOptions.toMutableList()
                                        .apply { this[index] = isCorrect }
                                    onOptionChosen(chosenOptions.all { it == true })
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DropDownTaskButton(
    options: List<Option>,
    onOptionChosen: (Boolean) -> Unit = {}
) {
    var showOptionDialog by remember { mutableStateOf(false) }
    var chosenOption by remember { mutableStateOf(-1) }

    LaunchedEffect(options) {
        chosenOption = -1
    }

    Button(
        shape = CutCornerShape(0.dp),
        contentPadding = PaddingValues(2.dp),
        modifier = Modifier
            .height((MaterialTheme.typography.bodyLarge.fontSize.value+8).dp),
        onClick = { showOptionDialog = true }
    ) {
        if (chosenOption == -1) {
            Text("Choose option")
        } else {
            Text(options[chosenOption].text)
        }
    }
    if (showOptionDialog) {
        Dialog(
            onDismissRequest = { showOptionDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text("Choose option")
                options.forEachIndexed() { index, option ->
                    Button(onClick = {
                        onOptionChosen(option.isCorrect)
                        chosenOption = index
                        showOptionDialog = false
                    }) {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(
    correctCount: Int,
    incorrectCount: Int,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(128.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "XP Gained: ${correctCount * 5}xp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "LVL 2, XP ${correctCount * 5} / ${(incorrectCount + correctCount) * 5}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Tasks failed: $incorrectCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )


                    Text(
                        text = "Tasks succeeded: $correctCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Achievements unlocked",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {navController.navigate("userUI")},
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB6A8F2)),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(bottom = 24.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Finish",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
@Composable
fun TaskDetailScreen(
    task: Task,
    onContinue: (Boolean) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<Boolean?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = task.question,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { selectedAnswer = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAnswer == true) Color.White else Color.LightGray
                    )
                ) {
                    Text(
                        "Yes",
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Button(
                    onClick = { selectedAnswer = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAnswer == false) Color.White else Color.LightGray
                    )
                ) {
                    Text("No", color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        Button(
            onClick = {
                selectedAnswer?.let { answer ->
                    val correctOption = task.options.values.firstOrNull { it.isCorrect }
                    val isCorrect = if (correctOption != null) {
                        (answer && correctOption.text.equals("Yes", true)) ||
                                (!answer && correctOption.text.equals("No", true))
                    } else false
                    onContinue(isCorrect)
                }
            },
            enabled = selectedAnswer != null,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedAnswer != null) Color.Black else Color(0xFF000000).copy(
                    alpha = 0.3f
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Continue",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
*/

