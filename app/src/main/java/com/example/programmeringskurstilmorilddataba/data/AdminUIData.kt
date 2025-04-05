package com.example.programmeringskurstilmorilddataba.data

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
    val introduction: String
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