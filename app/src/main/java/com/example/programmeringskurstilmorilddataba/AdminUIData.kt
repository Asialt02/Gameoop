package com.example.programmeringskurstilmorilddataba

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

fun getCourseByName(db: FirebaseFirestore, courseName: String, onResult: (DocumentSnapshot?) -> Unit) {
    db.collection("courses")
        .whereEqualTo("courseName", courseName)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                onResult(querySnapshot.documents.first())
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching course by name", e)
            onResult(null)
        }
}

fun addCourse(
    courseId: String,
    courseDescription: String,
    onComplete: (Exception?) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val courseRef = db.collection("courses").document(courseId)

    courseRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                onComplete(IllegalStateException("Course with ID '$courseId' already exists"))
            } else {
                val batch = db.batch()
                batch.set(courseRef, mapOf(
                    "courseName" to courseId,
                    "courseDescription" to courseDescription,
                ))

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("Firestore", "Course created successfully")
                        onComplete(null)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Course creation failed", e)
                        onComplete(e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error checking course existence", e)
            onComplete(e)
        }
}

fun addModuleToCourse(
    db: FirebaseFirestore,
    courseId: String,
    moduleName: String,
    moduleId: String,
    onComplete: (Boolean, Exception?) -> Unit = { _, _ -> }
) {
    // Input validation
    if (courseId.isBlank() || moduleId.isBlank()) {
        onComplete(false, IllegalArgumentException("Course ID and Module ID cannot be blank"))
        return
    }

    val moduleRef = db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)

    moduleRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                Log.w("Firestore", "Module $moduleId already exists in course $courseId")
                onComplete(false, IllegalStateException("Module already exists"))
            } else {
                moduleRef.set(
                    mapOf(
                        "name" to moduleName,
                    )
                )
                    .addOnSuccessListener {
                        Log.d("Firestore", "Module $moduleId added successfully to course $courseId")
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Failed to add module $moduleId", e)
                        onComplete(false, e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error checking module existence", e)
            onComplete(false, e)
        }
}

fun deleteModuleFromCourse(
    db: FirebaseFirestore,
    courseId: String,
    moduleId: String,
    onComplete: (Boolean, Exception?) -> Unit
) {
    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .delete()
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e) }
}

fun deleteCourse(db: FirebaseFirestore, courseId: String, onSuccess: () -> Unit) {
    db.collection("courses").document(courseId).delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Course Deleted")
            onSuccess()  // Call the success handler to navigate
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error while deleting Course", e)
        }
}

fun updateCourseDescription(
    db: FirebaseFirestore,
    courseId: String,
    newDescription: String,
    onComplete: (Boolean, Exception?) -> Unit = { _, _ -> }
) {
    db.collection("courses")
        .document(courseId)
        .update("courseDescription", newDescription)
        .addOnSuccessListener {
            Log.d("Firestore", "Course description updated successfully")
            onComplete(true, null)
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error updating course description", e)
            onComplete(false, e)
        }
}

data class Chapter(
    val id: String,
    val title: String,
)

data class Task(
    val id: String,
    val type: String,
    val question: String
)

fun addTaskToChapter(
    db: FirebaseFirestore,
    courseId: String,
    moduleId: String,
    chapterId: String,
    taskType: String,
    question: String
) {
    val taskData = hashMapOf(
        "type" to taskType,
        "question" to question,
    )

    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .collection("chapters")
        .document(chapterId)
        .collection("tasks")
        .add(taskData)
}

fun deleteChapterFromModule(
    db: FirebaseFirestore,
    courseId: String,
    moduleId: String,
    chapterId: String,
    onComplete: (Boolean, Exception?) -> Unit
) {
    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .collection("chapters")
        .document(chapterId)
        .delete()
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e) }
}