package com.example.programmeringskurstilmorilddataba.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.tooling.preview.Preview


// NOT FINISHED


@Composable
fun TaskScreen (
    onContinueClick: () -> Unit = {},
    task: Task,
    content: @Composable (ColumnScope.() -> Unit) = {}
) {
    val taskOptions = task.options
    var selectedAnswer by remember { mutableStateOf<Option>(taskOptions[0]) }

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
        modifier = Modifier.fillMaxWidth(),
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
    task: Task
) {
    val taskText = task.question
    val taskOptions = task.options

    val splitText = taskText.split("[option]")

    Column (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FlowRow {
            splitText.forEachIndexed { index, content ->
                content.split(" ").forEach {
                    Text("$it ")
                }
                if (index < taskOptions.size) {
                    Button(onClick = {}) { Text(taskOptions[index].text) }
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

    val testDropTask = Task(
        id = "0",
        question = "A class that inherits is called [option], and the class it inherits from is called [option].",
        type = TaskType.MultipleChoice,
        options = listOf(
            Option(id = "0", text = "Yes", isCorrect = false),
            Option(id = "1", text = "No", isCorrect = false),
        )
    )

    TaskScreen(task = testMultTask) {
        DropDownTask(testDropTask)
    }
}