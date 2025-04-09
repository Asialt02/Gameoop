package com.example.programmeringskurstilmorilddataba.data

import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.get
import androidx.navigation.NavController
import com.example.programmeringskurstilmorilddataba.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

fun saveOptions(
    db: FirebaseFirestore,
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskId: String,
    taskQuestion: String,
    options: List<Pair<String, Boolean>>
) {
    val optionsMap = options.mapIndexed { index, (text, isCorrect) ->
        "option$index" to mapOf(
            "text" to text,
            "isCorrect" to isCorrect
        )
    }.toMap()

    val updateData = hashMapOf<String, Any>(
        "$taskId.question" to taskQuestion,
        "$taskId.options" to optionsMap
    )

    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .collection("chapters")
        .document(chapterId)
        .update(updateData)
        .addOnFailureListener { e ->
            Log.e("SaveOptions", "Error saving options", e)
        }
}

data class Chapter(
    val id: String,
    val title: String,
    val introduction: String,
    val level: Int
)

data class Task(
    val id: String,
    val question: String,
    val type: TaskType = TaskType.MultipleChoice,
    val options: List<Option> = emptyList()
)

data class Option(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

data class Module (
    val id: String,
    val name: String,
    val difficulty: String,
    val difficultyColor: String,

)

fun addCourse(
    courseId: String,
    description: String,
    onComplete: (Exception?) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("courses")
        .document(courseId)
        .set(mapOf(
            "courseName" to courseId,
            "courseDescription" to description,
            "createdAt" to FieldValue.serverTimestamp()
        ))
        .addOnCompleteListener { onComplete(it.exception) }
}

fun deleteCourse(
    db: FirebaseFirestore,
    courseId: String,
    onComplete: () -> Unit
) {
    db.collection("courses")
        .document(courseId)
        .delete()
        .addOnSuccessListener { onComplete() }
}

fun addModuleToCourse(
    db: FirebaseFirestore,
    courseId: String,
    moduleName: String,
    moduleId: String,
    onComplete: (Exception?) -> Unit = {}
) {
    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .set(mapOf(
            "name" to moduleName,
            "createdAt" to FieldValue.serverTimestamp()
        ))
        .addOnCompleteListener { onComplete(it.exception) }
}

sealed class TaskType(val typeName: String) {
    object MultipleChoice : TaskType("Multiple Choice")
    object DropDown : TaskType("Drop Down")
    object YesNo : TaskType("Yes/No")

    companion object {
        fun fromString(type: String): TaskType {
            return when(type) {
                "Multiple Choice" -> MultipleChoice
                "Drop Down" -> DropDown
                "Yes/No" -> YesNo
                else -> MultipleChoice
            }
        }
    }
}

fun navigateToModuleEditorByName(
    navController: NavController,
    courseName: String,
    moduleName: String // The *current* name of the module
) {
    val db = Firebase.firestore
    db.collection("courses")
        .document(courseName)
        .collection("modules")
        .whereEqualTo("name", moduleName) // Query for the module with the given name
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Assuming there's only one module with that name (or take the first one)
                val document = querySnapshot.documents[0]
                val moduleId = document.id // Get the document ID (which is your module ID)


                navController.navigate(
                    Screen.ModuleEditorScreen.createRoute(courseName, moduleId)
                )
            } else {
                // Handle the case where no module with that name is found
                // (e.g., show an error message, navigate to a different screen)
                println("No module found with name: $moduleName")
            }
        }
        .addOnFailureListener { exception ->
            // Handle query failure (e.g., network error)
            println("Error querying for module: $exception")
        }
}

val difficultyColors = mapOf(
    "beginner" to "#73BD6C",
    "intermediate" to "#BDBA6C",
    "advanced" to "#BD886C",
    "expert" to "#BD6C6C"
)