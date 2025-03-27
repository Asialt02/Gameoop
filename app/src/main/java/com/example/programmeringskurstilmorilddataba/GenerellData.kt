package com.example.programmeringskurstilmorilddataba

import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class BottomNavItem(
    val title: String,
    val route: String
)

fun checkEmailValidity(email: String, onResult: (Boolean) -> Unit) {
    val apiKey = "4a3503e6c3244594a00dd486d77126ac"
    val url = "https://emailvalidation.abstractapi.com/v1/?api_key=$apiKey&email=$email"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(false)
        }


        override fun onResponse(call: Call, response: Response) {
            response.use { res ->
                if (!res.isSuccessful) {
                    onResult(false)
                    return
                }

                val responseData = res.body?.string()
                val jsonObject = JSONObject(responseData ?: "{}")

                val deliverability = jsonObject.optString("deliverability", "UNDELIVERABLE")

                onResult(deliverability == "DELIVERABLE")
            }
        }
    })
}

fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Password reset email sent to $email")
            } else {
                onResult(false, task.exception?.message ?: "Failed to send reset email")
            }
        }
}