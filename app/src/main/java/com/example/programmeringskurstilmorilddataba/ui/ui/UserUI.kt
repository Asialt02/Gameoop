package com.example.programmeringskurstilmorilddataba.ui.ui

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

fun updateUser(db: FirebaseFirestore, userId: String, userName: String, userEmail: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val updatedUser = mapOf(
        "name" to userName,
        "email" to userEmail,
    )
    currentUser?.verifyBeforeUpdateEmail(userEmail)
    db.collection("users").document(userId).update(updatedUser)
        .addOnSuccessListener { Log.d("Firestore", "User Updated Successfully") }
        .addOnFailureListener { e -> Log.w("Firestore", "Error while updating user", e) }
}

fun getCoursesUser(db: FirebaseFirestore, userId: String, onResult: (List<DocumentSnapshot>) -> Unit) {
    db.collection("users").document(userId).collection("usercourses") // Target the subcollection
        .get()
        .addOnSuccessListener { documents ->
            onResult(documents.documents)
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error fetching courses", e)
        }
}