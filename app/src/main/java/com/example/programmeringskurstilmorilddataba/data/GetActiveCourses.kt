package com.example.programmeringskurstilmorilddataba.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Course(
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val completedModules: String = "",
    val active: Boolean = false
)


fun getActiveCoursesForCurrentUser(
    db: FirebaseFirestore,
    callback: (List<Course>) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser != null) {
        val userId = currentUser.uid
        val courseCollectionRef = db.collection("users").document(userId).collection("course")

        courseCollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                val courses = mutableListOf<Course>()
                for (document in querySnapshot.documents) {
                    val courseId = document.id
                    val title = document.getString("title") ?: ""
                    val description = document.getString("desc") ?: ""
                    val completedModules = document.getString("completed_mod")?: ""
                    val active = document.getBoolean("active") ?: false

                    if (active) {
                        val course = Course(courseId, title, description, completedModules, active)
                        courses.add(course)
                    }
                }
                callback(courses)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting courses: $exception")
                callback(emptyList())
            }
    } else {
        Log.d("Firestore", "No user logged in")
        callback(emptyList())
    }
}