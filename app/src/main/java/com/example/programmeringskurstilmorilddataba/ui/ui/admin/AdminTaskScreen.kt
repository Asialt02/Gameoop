package com.example.programmeringskurstilmorilddataba.ui.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.data.saveOptions
import com.example.programmeringskurstilmorilddataba.data.saveDropDownOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskOptionsScreen(
    navController: NavController,
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String
) {
    val db = FirebaseFirestore.getInstance()
    var options by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var newOptionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var taskQuestion by remember { mutableStateOf("") }
    var showEditOptionDialog by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var editedOptionText by remember { mutableStateOf("") }

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

            val taskData = doc.get(taskId) as? Map<String, Any>
            taskQuestion = taskData?.get("question") as? String ?: ""

            val optionsMap = taskData?.get("options") as? Map<String, Map<String, Any>> ?: emptyMap()
            options = optionsMap.values.mapNotNull {
                val text = it["text"] as? String ?: return@mapNotNull null
                val isCorrect = it["isCorrect"] as? Boolean ?: false
                text to isCorrect
            }.sortedBy { it.first }

            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load options: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Options") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = newOptionText,
                    onValueChange = { newOptionText = it },
                    label = { Text("New option text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newOptionText.isNotBlank()) {
                            val updatedOptions = options + (newOptionText to false)
                            scope.launch {
                                try {
                                    saveOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        options = updatedOptions
                                    )
                                    options = updatedOptions
                                    newOptionText = ""
                                } catch (e: Exception) {
                                    error = "Failed to add option: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newOptionText.isNotBlank()
                ) {
                    Text("Add Option")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = taskQuestion,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(options) { index, (option, isCorrect) ->
                        OptionItem(
                            option = option,
                            isCorrect = isCorrect,
                            onToggleCorrect = {
                                val updatedOptions = options.toMutableList().apply {
                                    this[index] = option to !isCorrect
                                }
                                scope.launch {
                                    saveOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        options = updatedOptions
                                    )
                                    options = updatedOptions
                                }
                            },
                            onDelete = {
                                val updatedOptions = options.toMutableList().apply {
                                    removeAt(index)
                                }
                                scope.launch {
                                    saveOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        options = updatedOptions
                                    )
                                    options = updatedOptions
                                }
                            },
                            onEdit = {
                                editedOptionText = option
                                showEditOptionDialog = index to option
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditOptionDialog != null) {
        AlertDialog(
            onDismissRequest = { showEditOptionDialog = null },
            title = { Text("Edit Option") },
            text = {
                OutlinedTextField(
                    value = editedOptionText,
                    onValueChange = { editedOptionText = it },
                    label = { Text("Option Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditOptionDialog?.let { (index, _) ->
                            val updatedOptions = options.toMutableList().apply {
                                this[index] = editedOptionText to this[index].second
                            }
                            scope.launch {
                                saveOptions(
                                    db = db,
                                    courseId = courseId,
                                    moduleId = moduleId,
                                    chapterId = chapterId,
                                    taskId = taskId,
                                    taskQuestion = taskQuestion,
                                    options = updatedOptions
                                )
                                options = updatedOptions
                                showEditOptionDialog = null
                            }
                        }
                    },
                    enabled = editedOptionText.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOptionDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownTaskOptionsScreen(
    navController: NavController,
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String
) {
    val db = FirebaseFirestore.getInstance()
    var optionSets by remember { mutableStateOf<List<List<Pair<String, Boolean>>>>(emptyList()) }


    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var taskQuestion by remember { mutableStateOf("") }
    var showEditOptionDialog by remember { mutableStateOf<Pair<Pair<Int, Int>, String>?>(null) }
    var editedOptionText by remember { mutableStateOf("") }

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


            val taskData = doc.get(taskId) as? Map<String, Any>
            taskQuestion = taskData?.get("question") as? String ?: ""

            val optionSetsMap = taskData?.get("optionSets") as? Map<String, Map<String, Map<String, Any>>> ?: emptyMap()
            val optionSets1 = optionSetsMap.toSortedMap().values.toList()


            optionSets1.forEach {
                val tempList = it.values.mapNotNull {
                    val text = it["text"] as? String ?: return@mapNotNull null
                    val isCorrect = it["isCorrect"] as? Boolean ?: false
                    text to isCorrect
                }.sortedBy { it.first }


                optionSets = optionSets.plus(listOf(tempList))
            }



            isLoading = false
        } catch (e: Exception) {
            error = "Failed to load options: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Option Sets") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val newOptionSet = listOf(listOf("Option" to false))
                        val updatedOptionSets = optionSets.plus(newOptionSet)


                        scope.launch {
                            try {
                                saveDropDownOptions(
                                    db = db,
                                    courseId = courseId,
                                    moduleId = moduleId,
                                    chapterId = chapterId,
                                    taskId = taskId,
                                    taskQuestion = taskQuestion,
                                    optionSets = updatedOptionSets
                                )
                            } catch (e: Exception) {
                                error = "Failed to add option: ${e.message}"
                            }
                        }

                        optionSets = updatedOptionSets

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Option Set")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = taskQuestion,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {

                    itemsIndexed(optionSets) { setIndex, optionSet ->
                        OptionSetItem(
                            name = "Option Set $setIndex",
                            onDelete = {
                                val updatedOptionSets = optionSets.toMutableList().apply {
                                    removeAt(setIndex)
                                }
                                scope.launch {
                                    saveDropDownOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        optionSets = updatedOptionSets
                                    )
                                    optionSets = updatedOptionSets
                                }
                            },
                            addOptionToSet = {
                                val updatedOptionSets = optionSets.toMutableList()
                                updatedOptionSets[setIndex] = updatedOptionSets[setIndex].toMutableList().plus(listOf("Option" to false))
                                scope.launch {
                                    saveDropDownOptions(
                                        db = db,
                                        courseId = courseId,
                                        moduleId = moduleId,
                                        chapterId = chapterId,
                                        taskId = taskId,
                                        taskQuestion = taskQuestion,
                                        optionSets = updatedOptionSets
                                    )
                                    optionSets = updatedOptionSets
                                }
                            }
                        ) {
                            optionSet.forEachIndexed() { optionIndex, (option, isCorrect) ->
                                OptionItem(
                                    option = option,
                                    isCorrect = isCorrect,
                                    onToggleCorrect = {
                                        val updatedOptionSets = optionSets.toMutableList()
                                        updatedOptionSets[setIndex] = updatedOptionSets[setIndex].toMutableList().apply {
                                            this[optionIndex] = option to !isCorrect
                                        }
                                        scope.launch {
                                            saveDropDownOptions(
                                                db = db,
                                                courseId = courseId,
                                                moduleId = moduleId,
                                                chapterId = chapterId,
                                                taskId = taskId,
                                                taskQuestion = taskQuestion,
                                                optionSets = updatedOptionSets
                                            )
                                            optionSets = updatedOptionSets
                                        }
                                    },
                                    onDelete = {
                                        val updatedOptionSets = optionSets.toMutableList()
                                        updatedOptionSets[setIndex] = updatedOptionSets[setIndex].toMutableList().apply {
                                            removeAt(optionIndex)
                                        }
                                        scope.launch {
                                            saveDropDownOptions(
                                                db = db,
                                                courseId = courseId,
                                                moduleId = moduleId,
                                                chapterId = chapterId,
                                                taskId = taskId,
                                                taskQuestion = taskQuestion,
                                                optionSets = updatedOptionSets
                                            )
                                            optionSets = updatedOptionSets
                                        }
                                    },
                                    onEdit = {
                                        editedOptionText = option
                                        showEditOptionDialog = (setIndex to optionIndex) to option
                                    }
                                )
                            }
                        }
                    }



                }
            }
        }
    }

    if (showEditOptionDialog != null) {
        AlertDialog(
            onDismissRequest = { showEditOptionDialog = null },
            title = { Text("Edit Option") },
            text = {
                OutlinedTextField(
                    value = editedOptionText,
                    onValueChange = { editedOptionText = it },
                    label = { Text("Option Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditOptionDialog?.let { (indices, _) ->
                            val setIndex = indices.first
                            val optionIndex = indices.second

                            val updatedOptionSets = optionSets.toMutableList()
                            updatedOptionSets[setIndex] = updatedOptionSets[setIndex].toMutableList().apply {
                                this[optionIndex] = editedOptionText to this[optionIndex].second
                            }
                            scope.launch {
                                saveDropDownOptions(
                                    db = db,
                                    courseId = courseId,
                                    moduleId = moduleId,
                                    chapterId = chapterId,
                                    taskId = taskId,
                                    taskQuestion = taskQuestion,
                                    optionSets = updatedOptionSets
                                )
                                optionSets = updatedOptionSets
                                showEditOptionDialog = null
                            }
                        }
                    },
                    enabled = editedOptionText.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditOptionDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OptionItem(
    option: String,
    isCorrect: Boolean,
    onToggleCorrect: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCorrect,
                onCheckedChange = { onToggleCorrect() }
            )
            Text(
                text = option,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {  // Add edit button
                Icon(Icons.Default.Edit, "Edit option")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}

@Composable
fun OptionSetItem(
    name: String,
    onDelete: () -> Unit = {},
    addOptionToSet: () -> Unit = {},
    content: @Composable (ColumnScope.() -> Unit) = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column() {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = addOptionToSet) {
                    Icon(Icons.Default.Add, "Add option to set")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }

            content()
        }
    }
}