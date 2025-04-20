package com.example.programmeringskurstilmorilddataba.ui.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.programmeringskurstilmorilddataba.data.DropDownTask
import com.example.programmeringskurstilmorilddataba.data.Option
import com.example.programmeringskurstilmorilddataba.data.Task
import com.example.programmeringskurstilmorilddataba.data.TaskType


// NOT FINISHED


@Composable
fun TaskScreen (
    onContinueClick: () -> Unit = {},
    task: Any,
    content: @Composable (ColumnScope.() -> Unit) = {}
) {
    //val taskOptions = task.options
    //var selectedAnswer by remember { mutableStateOf<Option>(taskOptions[0]) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        content()

        Button(onClick = onContinueClick) {
            Text("Continue")
        }
    }
}

@Composable
fun MultipleChoiceTask (
    task: Task
) {
    val taskOptions = task.options

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card {
            Text(text = task.question)
        }

        Row {
            Button(
                onClick = {},
                modifier = Modifier.weight(0.4f)
            ) {
                Text(taskOptions[0].text)
            }
            Spacer(modifier = Modifier.weight(0.2f))
            Button(
                onClick = {},
                modifier = Modifier.weight(0.4f)
            ) {
                Text(taskOptions[1].text)
            }
        }
        Row {
            Button(
                onClick = {},
                modifier = Modifier.weight(0.4f)
            ) {
                Text(taskOptions[2].text)
            }
            Spacer(modifier = Modifier.weight(0.2f))
            Button(
                onClick = {},
                modifier = Modifier.weight(0.4f)
            ) {
                Text(taskOptions[3].text)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DropDownTask (
    task: DropDownTask
) {
    val taskText = task.question
    val taskOptions = task.options

    val splitText = taskText.split("[option]")

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        FlowRow (
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            splitText.forEachIndexed { index, content ->
                content.split(" ").forEach {
                    Text(
                        text = "$it ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (index < taskOptions.size) {
                    DropDownTaskButton(taskOptions[index])
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
    var chosenOption by remember { mutableStateOf(0) }
    Button(
        shape = CutCornerShape(0.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.height((MaterialTheme.typography.bodyLarge.fontSize.value+8).dp),
        onClick = { showOptionDialog = true }
    ) {
        Text(options[chosenOption].text)
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


@Preview
@Composable
fun TaskPreview() {
    val testMultTask = Task(
        id = "0",
        question = "What is the",
        type = TaskType.MultipleChoice,
        options = listOf(
            Option(id = "0", text = "Yes", isCorrect = false),
            Option(id = "1", text = "No", isCorrect = false),
            Option(id = "2", text = "Maybe", isCorrect = true),
            Option(id = "3", text = "Definitely", isCorrect = false),
        )
    )

//    val testDropTask = DropDownTask(
//        id = "0",
//        question = "A class that inherits is called [option], and the class it inherits from is called [option].",
//        type = TaskType.MultipleChoice,
//        optionSets = listOf(
//            listOf(
//                Option(id = "0", text = "Subclass", isCorrect = false),
//                Option(id = "1", text = "Superclass", isCorrect = false),
//                Option(id = "2", text = "Class", isCorrect = false),
//                ),
//            listOf(
//                Option(id = "3", text = "Class", isCorrect = false),
//                Option(id = "4", text = "Superclass", isCorrect = false),
//                Option(id = "5", text = "Subclass", isCorrect = false),
//            )
//        )
//    )
//
//    TaskScreen(task = testMultTask) {
//        DropDownTask(testDropTask)
//    }
}

/*

@Composable
fun TaskScreen(
    chapter: Chapter,
    navController: NavController,
) {
    if (chapter.tasks.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No tasks available in this chapter")
        }
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    val taskKeys = chapter.tasks!!.keys.toList()
    var showSummary by remember { mutableStateOf(false) }
    val answers = remember { mutableStateListOf<Boolean>() }
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }

    if (showSummary) {
        SummaryScreen(correctCount, incorrectCount, navController =navController )
    } else {
        val taskMap = chapter.tasks!![taskKeys[currentIndex]] as? Map<*, *> ?: emptyMap<Any, Any>()
        val optionsMap = taskMap["options"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val questionText = taskMap["question"]?.toString() ?: "No Question"

        val options = optionsMap.mapNotNull { entry ->
            val key = entry.key as? String ?: return@mapNotNull null
            val opt = entry.value as? Map<*, *> ?: return@mapNotNull null
            key to Option(
                text = opt["text"].toString(),
                isCorrect = opt["isCorrect"] as? Boolean ?: false
            )
        }.toMap()

        val task = Task(
            question = questionText,
            options = options
        )

        TaskDetailScreen(
            task = task,
            onContinue = { isCorrect ->
                answers.add(isCorrect)
                if (isCorrect) {
                    correctCount++
                } else {
                    incorrectCount++
                }
                if (currentIndex < taskKeys.size - 1) {
                    currentIndex++
                } else {
                    showSummary = true
                }
            }
        )
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
            .background(BackgroundColor),
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
                        text = "LVL 2, XP ${correctCount * 5} / ${incorrectCount + correctCount * 5}",
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