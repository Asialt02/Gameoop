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
    val batch = db.batch()

    val courseRef = db.collection("courses").document(courseId)
    batch.set(courseRef, mapOf(
        "courseName" to courseId,
        "courseDescription" to courseDescription
    ))

    batch.commit()
        .addOnSuccessListener {
            Log.d("Firestore", "Course created")
            onComplete(null)
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Create failed", e)
            onComplete(e)
        }
}

fun addModuleToCourse(
    db: FirebaseFirestore,
    courseId: String,
    moduleName: String,
    moduleId: String,
) {
    db.collection("courses")
        .document(courseId)
        .collection("modules")
        .document(moduleId)
        .set(mapOf(
            "name" to moduleName,
        ))
        .addOnSuccessListener {
            Log.d("Firestore", "Module $moduleId added")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Module add failed", e)
        }
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